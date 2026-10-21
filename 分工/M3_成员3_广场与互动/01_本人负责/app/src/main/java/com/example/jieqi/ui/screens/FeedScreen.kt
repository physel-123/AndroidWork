package com.example.jieqi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jieqi.data.FeedPost
import com.example.jieqi.data.TargetType
import com.example.jieqi.data.content.ArtworkLibrary
import com.example.jieqi.data.content.SolarTermContent
import com.example.jieqi.domain.TermDateUtils
import com.example.jieqi.ui.LocalAppContainer
import com.example.jieqi.ui.LocalSnackbarHostState
import com.example.jieqi.ui.components.CommentSection
import com.example.jieqi.ui.components.EmptyState
import com.example.jieqi.ui.components.JieQiCard
import com.example.jieqi.ui.components.LikeButton
import com.example.jieqi.ui.components.PaperColor
import com.example.jieqi.ui.components.SectionHeader
import com.example.jieqi.ui.components.SelectableChip
import com.example.jieqi.util.AssetImage
import com.example.jieqi.util.ShareUtils
import com.example.jieqi.util.UserAvatar
import com.example.jieqi.util.postTimeText
import com.example.jieqi.util.relativeTime
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * 打卡广场（互动模块主页面）。
 *
 * 用户可以发布带图文的打卡动态，其他人可以点赞、评论、回复，
 * 形成「打卡 → 分享 → 被互动 → 继续打卡」的正向循环。
 */
@Composable
fun FeedScreen(onRequireLogin: () -> Unit) {
    val container = LocalAppContainer.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    val posts by container.socialRepository.feed.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentUser by container.userRepository.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val memberCount by container.socialRepository.memberCount.collectAsStateWithLifecycle(initialValue = 0)

    val today = remember { LocalDateTime.now().toLocalDate() }
    val occurrence = remember { TermDateUtils.currentOccurrence(LocalDateTime.now()) }

    var showComposer by remember { mutableStateOf(false) }
    var filterMine by remember { mutableStateOf(false) }

    val shown = remember(posts, filterMine, currentUser) {
        if (filterMine && currentUser != null) posts.filter { it.isMine } else posts
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------- 顶部 ----------
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "打卡广场", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "共 $memberCount 位节气同好 · ${posts.size} 条动态",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = occurrence.term.color,
                    modifier = Modifier.clickable {
                        if (currentUser == null) onRequireLogin() else showComposer = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "发布动态",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ---------- 未登录提示 ----------
        if (currentUser == null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRequireLogin() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "登录后即可发布你的节气生活",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "还可以点赞、评论、回复其他同好",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ---------- 筛选 ----------
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableChip(
                    text = "全部动态",
                    selected = !filterMine,
                    accent = occurrence.term.color,
                    onClick = { filterMine = false }
                )
                SelectableChip(
                    text = "我的动态",
                    selected = filterMine,
                    accent = occurrence.term.color,
                    onClick = {
                        if (currentUser == null) onRequireLogin() else filterMine = true
                    }
                )
            }
        }

        // ---------- 动态列表 ----------
        if (shown.isEmpty()) {
            item {
                EmptyState(
                    text = if (filterMine) "你还没有发布过动态" else "广场上还没有动态",
                    hint = "点击右上角「发布动态」，记录今天的节气生活"
                )
            }
        } else {
            items(shown, key = { it.id }) { feedPost ->
                PostCard(
                    feedPost = feedPost,
                    onRequireLogin = onRequireLogin,
                    onDelete = {
                        scope.launch {
                            container.socialRepository.deletePost(feedPost.post)
                            snackbar.showSnackbar("已删除动态")
                        }
                    }
                )
            }
        }
    }

    // ---------- 发布动态 ----------
    if (showComposer) {
        val user = currentUser
        PostComposerDialog(
            termIndex = occurrence.index,
            termName = occurrence.name,
            onDismiss = { showComposer = false },
            onSubmit = { content, mood, imageAsset ->
                showComposer = false
                if (user == null) {
                    onRequireLogin()
                } else {
                    scope.launch {
                        val id = container.socialRepository.publish(
                            content = content,
                            termIndex = occurrence.index,
                            termName = occurrence.name,
                            mood = mood,
                            imageAsset = imageAsset,
                            author = user
                        )
                        snackbar.showSnackbar(if (id > 0) "发布成功" else "发布失败，请检查内容")
                    }
                }
            }
        )
    }
}

/**
 * 一条动态卡片：作者、节气标签、正文、配图、心情、点赞与评论。
 */
@Composable
fun PostCard(
    feedPost: FeedPost,
    onRequireLogin: () -> Unit,
    onDelete: (() -> Unit)? = null,
    showComments: Boolean = true
) {
    val post = feedPost.post
    val accent = SolarTermContent.byName(post.termName)?.color ?: MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var commentsExpanded by remember { mutableStateOf(false) }

    JieQiCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // 作者行
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(nickname = post.authorName, colorIndex = post.authorColor, size = 42.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (feedPost.isMine) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "我",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = postTimeText(post.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 节气标签
                Surface(shape = RoundedCornerShape(8.dp), color = accent.copy(alpha = 0.14f)) {
                    Text(
                        text = post.termName,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (feedPost.isMine && onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodyMedium)

            // 配图
            post.imageAsset?.let { asset ->
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.6f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaperColor)
                ) {
                    AssetImage(
                        path = asset,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            post.mood?.let { mood ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "# $mood",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 操作行
            Row(verticalAlignment = Alignment.CenterVertically) {
                LikeButton(
                    type = TargetType.POST,
                    targetId = post.id,
                    onRequireLogin = onRequireLogin
                )
                Spacer(Modifier.width(18.dp))
                Row(
                    modifier = Modifier.clickable { commentsExpanded = !commentsExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${post.commentCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    ShareUtils.shareText(
                        context,
                        "【节气民俗 · 打卡广场】\n${post.authorName} 在${post.termName}写的：\n" +
                            post.content + "\n#二十四节气 #打卡"
                    )
                }) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "分享",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 评论区
            AnimatedVisibility(visible = commentsExpanded && showComments) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    CommentSection(
                        type = TargetType.POST,
                        targetId = post.id,
                        onRequireLogin = onRequireLogin
                    )
                }
            }
        }
    }
}

/**
 * 发布动态弹窗：正文 + 心情 + 从该节气配画中选一张作为配图。
 */
@Composable
private fun PostComposerDialog(
    termIndex: Int,
    termName: String,
    onDismiss: () -> Unit,
    onSubmit: (content: String, mood: String?, imageAsset: String?) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<String?>(null) }

    val artworks = remember(termIndex) { ArtworkLibrary.of(termIndex) }
    val moods = remember { listOf("清爽", "惬意", "充实", "馋了", "惦记", "元气满满") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录我的${termName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 300) content = it },
                    label = { Text("今天和节气有关的小事") },
                    placeholder = { Text("例如：今天吃了春饼，卷了豆芽和酱肉") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "心情标签",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(moods) { m ->
                        SelectableChip(
                            text = m,
                            selected = mood == m,
                            accent = SolarTermContent.byIndex(termIndex)?.color
                                ?: MaterialTheme.colorScheme.primary,
                            onClick = { mood = if (mood == m) "" else m }
                        )
                    }
                }

                Text(
                    text = "从「$termName」的配画中选一张（可选）",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (artworks.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "该节气暂无配图",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(artworks) { artwork ->
                            val selected = selectedImage == artwork.file
                            Box(
                                modifier = Modifier
                                    .size(width = 92.dp, height = 92.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PaperColor)
                                    .clickable {
                                        selectedImage = if (selected) null else artwork.file
                                    }
                            ) {
                                AssetImage(
                                    path = artwork.file,
                                    contentDescription = artwork.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                            )
                                    )
                                    Text(
                                        text = "已选",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        content,
                        mood.ifBlank { null },
                        selectedImage
                    )
                },
                enabled = content.isNotBlank()
            ) { Text("发布") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

/** 社区成员展示（用于「我的」页面侧边展示）。 */
@Composable
fun MemberStrip(modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val members by container.userRepository.members(12)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    Column(modifier = modifier) {
        SectionHeader(title = "节气同好", subtitle = "共 ${members.size} 位成员")
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(members, key = { it.id }) { user ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    UserAvatar(nickname = user.nickname, colorIndex = user.avatarColor, size = 46.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = user.nickname,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                    Text(
                        text = relativeTime(user.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
