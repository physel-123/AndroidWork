package com.example.jieqi.data

import com.example.jieqi.data.db.JieQiDatabase
import com.example.jieqi.data.db.UserEntity
import com.example.jieqi.data.remote.ApiClient
import com.example.jieqi.data.remote.JieQiApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/** 登录/注册结果。 */
sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Failure(val message: String) : AuthResult
}

/**
 * 账号仓库：注册、登录走后端 FastAPI（JWT），并把用户镜像写入本地 Room。
 *
 * 迁移策略（「先只迁账号」档位）：
 * - 注册/登录调用后端 /api/auth，密码校验在后端（bcrypt）；
 * - 成功后把后端返回的用户信息同步写入本地 Room 镜像，
 *   这样打卡广场、评论、点赞等仍依赖本地 UserEntity 的功能无需改动；
 * - SessionStore 同时保存本地 userId 与后端 JWT token。
 */
class UserRepository(
    private val db: JieQiDatabase,
    private val session: SessionStore,
    private val api: JieQiApi = ApiClient.api
) {

    private val dao = db.userDao()

    /** 当前登录用户（未登录时发射 null）。 */
    val currentUser: Flow<UserEntity?> = session.currentUserId.map { id ->
        if (id <= 0L) null else dao.findById(id)
    }

    /** 是否已登录。 */
    val isLoggedIn: Flow<Boolean> = session.currentUserId.map { it > 0L }

    /** 社区成员列表（用于「广场」页展示活跃用户）。 */
    fun members(limit: Int = 20): Flow<List<UserEntity>> = dao.observeUsers(limit)

    suspend fun register(
        username: String,
        nickname: String,
        password: String
    ): AuthResult {
        val name = username.trim()
        if (name.length < 3) return AuthResult.Failure("账号至少 3 个字符")
        if (!name.matches(Regex("^[A-Za-z0-9_\\u4e00-\\u9fa5]+$"))) {
            return AuthResult.Failure("账号只能包含中英文、数字和下划线")
        }
        if (password.length < 6) return AuthResult.Failure("密码至少 6 位")

        return try {
            val resp = api.register(
                com.example.jieqi.data.remote.RegisterRequest(
                    username = name,
                    password = password,
                    nickname = nickname.trim()
                )
            )
            // 写入本地镜像
            val local = upsertLocalMirror(resp)
            session.login(local.id, token = resp.token, username = resp.username)
            AuthResult.Success(local)
        } catch (e: HttpException) {
            AuthResult.Failure(parseHttpError(e, "注册失败"))
        } catch (e: IOException) {
            AuthResult.Failure("无法连接服务器，请检查网络后重试")
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "注册失败，请稍后重试")
        }
    }

    suspend fun login(username: String, password: String): AuthResult {
        return try {
            val resp = api.login(username.trim(), password)
            val local = upsertLocalMirror(resp)
            session.login(local.id, token = resp.token, username = resp.username)
            AuthResult.Success(local)
        } catch (e: HttpException) {
            AuthResult.Failure(parseHttpError(e, "登录失败"))
        } catch (e: IOException) {
            AuthResult.Failure("无法连接服务器，请检查网络后重试")
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "登录失败，请稍后重试")
        }
    }

    /** 一键登录演示账号（演示账号已由后端播种）。 */
    suspend fun loginAsDemo(userId: Long): AuthResult {
        val user = dao.findById(userId) ?: return AuthResult.Failure("演示账号不存在")
        session.login(user.id)
        return AuthResult.Success(user)
    }

    /** 按账号名一键登录演示账号。 */
    suspend fun loginAsDemoByUsername(username: String): AuthResult =
        login(username, DEMO_PASSWORD)

    suspend fun logout() = session.logout()

    suspend fun updateProfile(nickname: String, bio: String) {
        val id = session.currentUserIdOnce()
        if (id <= 0L) return
        val user = dao.findById(id) ?: return
        dao.insert(
            user.copy(
                nickname = nickname.trim().ifBlank { user.nickname },
                bio = bio.trim()
            )
        )
    }

    // ------------------------------------------------------------------
    // 工具方法
    // ------------------------------------------------------------------

    /** 把后端返回的用户信息同步到本地 Room（已存在则复用，否则新建镜像）。 */
    private suspend fun upsertLocalMirror(
        resp: com.example.jieqi.data.remote.AuthResponse
    ): UserEntity = withContext(Dispatchers.IO) {
        // 已存在本地镜像（如本地演示账号）则直接复用，保持 authorId 稳定
        dao.findByUsername(resp.username)?.let { return@withContext it }

        val entity = UserEntity(
            username = resp.username,
            nickname = resp.nickname.ifBlank { resp.username },
            avatarColor = resp.avatarColor,
            passwordHash = "remote",  // 密码由后端校验，本地不再存
            salt = "",
            bio = resp.bio,
            isDemo = resp.isDemo
        )
        val id = dao.insert(entity)
        dao.findById(id) ?: throw IllegalStateException("写入本地镜像失败")
    }

    /** 从 HttpException 提取后端 detail 错误信息。 */
    private fun parseHttpError(e: HttpException, fallback: String): String {
        return try {
            val body = e.response()?.errorBody()?.string()
            // 后端返回 {"detail": "..."}
            val detail = body?.let {
                Regex("\"detail\"\\s*:\\s*\"([^\"]+)\"").find(it)?.groupValues?.get(1)
            }
            detail ?: fallback
        } catch (_: Exception) {
            fallback
        }
    }

    companion object {
        /** 内置演示账号（密码统一为 123456，由后端播种，界面上可一键登录）。 */
        val DEMO_ACCOUNTS = listOf(
            DemoAccount("linxiaoman", "林小满", "在杭州读大二，喜欢拍节气里的花", 0),
            DemoAccount("ahe", "阿禾", "农学专业，跟着节气种地", 6),
            DemoAccount("qingtuan", "青团君", "爱吃也会做，节气食俗收集控", 5),
            DemoAccount("shiguang", "拾光", "中学语文老师，做班会素材", 15)
        )

        const val DEMO_PASSWORD = "123456"
    }
}

/** 演示账号定义。 */
data class DemoAccount(
    val username: String,
    val nickname: String,
    val bio: String,
    val avatarColor: Int
)
