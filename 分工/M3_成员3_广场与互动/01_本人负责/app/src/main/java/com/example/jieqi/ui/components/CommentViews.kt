package com.example.jieqi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jieqi.data.CommentItem
import com.example.jieqi.data.UserRepository
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.LocalSnackbarHostState
import com.example.jieqi.util.UserAvatar
import com.example.jieqi.util.relativeTime
import kotlinx.coroutines.launch

/**
 * 点赞按钮：图标 + 数字，登录后才可点。
 *
 * @param type 互动对象类型（见 TargetType）
 * @param targetId 互动对象 id
 */
@Composable
fun LikeButton(
    type: String,
    targetId: Long,
    modifier: Modifier = Modifier,
    onRequireLogin: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val liked by remember(type, targetId) { container.socialRepository.liked(type, targetId) }
        .collectAsStateWithLifecycle(initialValue = false)
    val count by remember(type, targetId) { container.socialRepository.likeCount(type, targetId) }
        .collectAsStateWithLifecycle(initialValue = 0)
    val loggedIn by container.userRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier.clickable {
            if (!loggedIn) {
                onRequireLogin()
            } else {
                scope.launch { container.socialRepository.toggleLike(type, targetId) }
            }
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (liked) "取消点赞" else "点赞",
            tint = if (liked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            color = if (liked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 评论区（节气 / 民俗 / 动态共用）。
 *
 * - 支持一级评论与二级回复；
 * - 未登录时提示先登录；
 * - [collapsed] 为 true 时只显示标题栏与最新两条，点击展开。
 */
@Composable
fun CommentSection(
    type: String,
    targetId: Long,
    modifier: Modifier = Modifier,
    title: String = "评论",
    maxVisible: Int = Int.MAX_VALUE,
    onRequireLogin: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    val comments by remember(type, targetId) { container.socialRepository.comments(type, targetId) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val loggedIn by container.userRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val currentUser by container.userRepository.currentUser.collectAsStateWithLifecycle(initialValue = null)

    var input by remember { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<CommentItem?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val visible = remember(comments, maxVisible, expanded) {
        if (expanded || maxVisible == Int.MAX_VALUE) comments else comments.take(maxVisible)
    }
    val hiddenCount = comments.size - visible.size

    fun send() {
        val user = currentUser
        if (user == null) {
            onRequireLogin()
            return
        }
        val content = input.trim()
        if (content.isEmpty()) return
        scope.launch {
            val ok = container.socialRepository.comment(
                type = type,
                targetId = targetId,
                content = content,
                parentId = replyTo?.id ?: 0L,
                author = user
            )
            if (ok) {
                input = ""
                replyTo = null
                snackbar.showSnackbar("评论已发布")
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (comments.isEmpty()) title else "$title（${comments.size}）",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(10.dp))

        // 评论列表
        if (comments.isEmpty()) {
            Text(
                text = if (loggedIn) "还没有评论，来说两句吧" else "登录后可以参与讨论",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                visible.forEach { item ->
                    CommentRow(
                        item = item,
                        onReply = { replyTo = it },
                        onDelete = {
                            scope.launch {
                                container.socialRepository.deleteComment(
                                    item.id, item.targetType, item.targetId
                                )
                                snackbar.showSnackbar("已删除评论")
                            }
                        }
                    )
                }
            }
            if (hiddenCount > 0) {
                TextButton(onClick = { expanded = true }) {
                    Text("查看全部 ${comments.size} 条评论")
                }
            } else if (expanded && comments.size > 6) {
                TextButton(onClick = { expanded = false }) { Text("收起评论") }
            }
        }

        Spacer(Modifier.height(12.dp))

        // 输入区
        if (loggedIn) {
            replyTo?.let { target ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "正在回复 @${target.authorName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "取消",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { replyTo = null }
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { if (it.length <= 200) input = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = {
                        Text(
                            text = replyTo?.let { "回复 @${it.authorName}…" } ?: "写下你的想法…",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
                )
                IconButton(onClick = { send() }, enabled = input.isNotBlank()) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "发送",
                        tint = if (input.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRequireLogin() }
            ) {
                Text(
                    text = "登录后即可发表评论",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
    }
}

/** 单条评论（含二级回复）。 */
@Composable
private fun CommentRow(
    item: CommentItem,
    onReply: (CommentItem) -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            UserAvatar(nickname = item.authorName, colorIndex = item.authorColor, size = 34.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.authorName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = relativeTime(item.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "回复",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onReply(item) }
                    )
                    if (item.isMine) {
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = "删除",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable(onClick = onDelete)
                        )
                    }
                }
            }
        }

        // 二级回复
        if (item.replies.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 44.dp, top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.replies.forEach { reply ->
                    Row {
                        UserAvatar(
                            nickname = reply.authorName,
                            colorIndex = reply.authorColor,
                            size = 26.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = reply.authorName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                reply.replyToName?.let { name ->
                                    Text(
                                        text = " 回复 @$name",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = reply.content,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = relativeTime(reply.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 评论数（用于卡片上展示）。 */
@Composable
fun CommentCountLabel(type: String, targetId: Long, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val count by remember(type, targetId) { container.socialRepository.commentCount(type, targetId) }
        .collectAsStateWithLifecycle(initialValue = 0)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Reply,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 未登录时的默认提示文案。 */
object LoginHints {
    const val NEED_LOGIN = "请先登录后再操作"
}
