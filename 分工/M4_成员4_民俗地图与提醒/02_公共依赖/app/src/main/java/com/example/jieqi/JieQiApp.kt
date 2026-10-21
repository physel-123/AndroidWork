package com.example.jieqi

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import com.example.jieqi.data.AppContainer

/**
 * 应用入口：持有全局依赖容器，并配置 Coil 图片加载器。
 */
class JieQiApp : Application(), SingletonImageLoader.Factory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    /**
     * 配置全局图片加载器。
     *
     * - 支持 GIF（[GifDecoder] / [AnimatedImageDecoder]）与视频首帧（[VideoFrameDecoder]），
     *   因此节气配图、动图与本地视频可以共用同一个组件加载；
     * - 支持 http(s)，为后续接入在线素材留好扩展点；
     * - 开启淡入淡出，图片出现更柔和。
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
                // AnimatedImageDecoder 依赖 API 28+ 的 ImageDecoder，低版本跳过
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory())
                }
                add(VideoFrameDecoder.Factory())
                add(OkHttpNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
}
