package com.example.jieqi.util

import android.content.Context
import android.content.Intent

/**
 * 分享工具：把文本内容通过系统分享面板发送给好友 / 群聊。
 *
 * 这是“文化宣传”最直接的传播链路——用户看到好内容可以一键分享出去。
 */
object ShareUtils {

    /**
     * 分享纯文本。
     *
     * @param text 分享正文
     * @param chooserTitle 分享面板标题
     */
    fun shareText(context: Context, text: String, chooserTitle: String = "分享节气民俗") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "二十四节气 · 传统民俗")
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
    }
}
