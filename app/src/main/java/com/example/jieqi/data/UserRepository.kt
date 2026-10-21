package com.example.jieqi.data

import android.util.Base64
import com.example.jieqi.data.db.JieQiDatabase
import com.example.jieqi.data.db.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

/** 登录/注册结果。 */
sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Failure(val message: String) : AuthResult
}

/**
 * 账号仓库：本地注册、登录、演示账号。
 *
 * 安全说明（课程项目范围内的合理做法）：
 * - 密码不存明文，使用「随机盐 + SHA-256」摘要后存储；
 * - 盐由 [SecureRandom] 生成，每个用户独立；
 * - 真实产品应使用 PBKDF2/bcrypt 并配合服务端，这里做了简化。
 */
class UserRepository(
    private val db: JieQiDatabase,
    private val session: SessionStore
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
        if (dao.findByUsername(name) != null) return AuthResult.Failure("该账号已被注册")

        val salt = randomSalt()
        val user = UserEntity(
            username = name,
            nickname = nickname.trim().ifBlank { name },
            avatarColor = (0..23).random(),
            passwordHash = hash(password, salt),
            salt = salt,
            bio = "刚刚加入节气民俗"
        )
        val id = dao.insert(user)
        if (id <= 0L) return AuthResult.Failure("注册失败，请换个账号试试")
        val saved = dao.findById(id) ?: return AuthResult.Failure("注册失败")
        session.login(id)
        return AuthResult.Success(saved)
    }

    suspend fun login(username: String, password: String): AuthResult {
        val user = dao.findByUsername(username.trim())
            ?: return AuthResult.Failure("账号不存在，请先注册")
        if (user.passwordHash != hash(password, user.salt)) {
            return AuthResult.Failure("密码不正确")
        }
        session.login(user.id)
        return AuthResult.Success(user)
    }

    /** 一键登录演示账号（用于课堂演示多人互动效果）。 */
    suspend fun loginAsDemo(userId: Long): AuthResult {
        val user = dao.findById(userId) ?: return AuthResult.Failure("演示账号不存在")
        session.login(user.id)
        return AuthResult.Success(user)
    }

    /** 按账号名一键登录演示账号。 */
    suspend fun loginAsDemoByUsername(username: String): AuthResult {
        val user = dao.findByUsername(username)
            ?: return AuthResult.Failure("演示账号尚未初始化，请稍后重试")
        session.login(user.id)
        return AuthResult.Success(user)
    }

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

    private fun randomSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val result = digest.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    companion object {
        /** 内置演示账号（密码统一为 123456，界面上可一键登录）。 */
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
