package com.example.jieqi.data.remote

import com.example.jieqi.data.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * 神秘文化仓库：封装运势/占卜/手相/面相的后端调用。
 * 统一返回 Result，UI 层据此展示加载/错误/成功状态。
 */
class MysticRepository(
    private val api: JieQiApi = ApiClient.api,
    private val session: SessionStore? = null
) {

    /** 当前登录用户名，未登录用 guest。 */
    private suspend fun currentUsername(): String =
        session?.currentUsername?.first()?.takeIf { it.isNotEmpty() } ?: "guest"

    suspend fun getFortune(zodiac: String): Result<FortuneResponse> = safeCall {
        api.fortune(zodiac)
    }

    suspend fun drawSign(): Result<SignResponse> = safeCall {
        api.sign(currentUsername())
    }

    suspend fun drawTarot(count: Int = 3): Result<TarotResponse> = safeCall {
        api.tarot(currentUsername(), count)
    }

    /** 获取当天已抽的灵签结果，没有则返回 null。 */
    suspend fun todaySign(): Result<SignResponse?> = safeCall {
        api.todaySign(currentUsername())
    }

    /** 获取当天已抽的塔罗结果，没有则返回 null。 */
    suspend fun todayTarot(): Result<TarotResponse?> = safeCall {
        api.todayTarot(currentUsername())
    }

    suspend fun analyzePalm(bytes: ByteArray): Result<VisionResponse> = safeCall {
        api.analyzePalm(ApiClient.imagePart(bytes))
    }

    suspend fun analyzeFace(bytes: ByteArray): Result<VisionResponse> = safeCall {
        api.analyzeFace(ApiClient.imagePart(bytes))
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(block())
            } catch (e: HttpException) {
                Result.failure(Exception("服务返回错误（${e.code()}），请稍后重试"))
            } catch (e: IOException) {
                Result.failure(Exception("网络连接失败，请检查网络后重试"))
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "请求失败，请稍后重试"))
            }
        }
}
