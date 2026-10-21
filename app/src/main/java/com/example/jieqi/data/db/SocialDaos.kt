package com.example.jieqi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("SELECT * FROM user WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM user WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM user")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM user WHERE isDemo = 1")
    suspend fun demoCount(): Int

    @Query("SELECT COUNT(*) FROM user")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM user WHERE isDemo = 1 LIMIT :limit")
    suspend fun demoUsers(limit: Int): List<UserEntity>

    @Query("SELECT * FROM user ORDER BY createdAt LIMIT :limit")
    fun observeUsers(limit: Int): Flow<List<UserEntity>>
}

@Dao
interface PostDao {

    @Insert
    suspend fun insert(post: PostEntity): Long

    @Update
    suspend fun update(post: PostEntity)

    @Query("SELECT * FROM post ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM post WHERE authorId = :userId ORDER BY createdAt DESC")
    fun observeByAuthor(userId: Long): Flow<List<PostEntity>>

    @Query("SELECT * FROM post WHERE termIndex = :termIndex ORDER BY createdAt DESC")
    fun observeByTerm(termIndex: Int): Flow<List<PostEntity>>

    @Query("SELECT * FROM post WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): PostEntity?

    @Query("SELECT * FROM post WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<PostEntity?>

    @Query("SELECT COUNT(*) FROM post WHERE authorId = :userId")
    fun observeCountByAuthor(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM post")
    suspend fun count(): Int

    @Query("SELECT * FROM post ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<PostEntity>

    @Query("UPDATE post SET likeCount = MAX(0, likeCount + :delta) WHERE id = :id")
    suspend fun addLikeCount(id: Long, delta: Int)

    @Query("UPDATE post SET commentCount = MAX(0, commentCount + :delta) WHERE id = :id")
    suspend fun addCommentCount(id: Long, delta: Int)

    @Query("DELETE FROM post WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM post")
    suspend fun clear()
}

@Dao
interface CommentDao {

    @Insert
    suspend fun insert(comment: CommentEntity): Long

    @Query(
        "SELECT * FROM comment WHERE targetType = :type AND targetId = :targetId " +
            "ORDER BY createdAt ASC"
    )
    fun observeForTarget(type: String, targetId: Long): Flow<List<CommentEntity>>

    @Query(
        "SELECT COUNT(*) FROM comment WHERE targetType = :type AND targetId = :targetId"
    )
    fun observeCount(type: String, targetId: Long): Flow<Int>

    @Query(
        "SELECT COUNT(*) FROM comment WHERE targetType = :type AND targetId = :targetId"
    )
    suspend fun count(type: String, targetId: Long): Int

    @Query("UPDATE comment SET likeCount = MAX(0, likeCount + :delta) WHERE id = :id")
    suspend fun addLikeCount(id: Long, delta: Int)

    @Query("DELETE FROM comment WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM comment WHERE targetType = :type AND targetId = :targetId")
    suspend fun clearTarget(type: String, targetId: Long)
}

@Dao
interface LikeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(like: LikeEntity)

    @Query("DELETE FROM like_record WHERE targetType = :type AND targetId = :targetId AND userId = :userId")
    suspend fun delete(type: String, targetId: Long, userId: Long)

    @Query(
        "SELECT EXISTS(SELECT 1 FROM like_record WHERE targetType = :type " +
            "AND targetId = :targetId AND userId = :userId)"
    )
    fun observeLiked(type: String, targetId: Long, userId: Long): Flow<Boolean>

    @Query(
        "SELECT targetId FROM like_record WHERE targetType = :type AND userId = :userId"
    )
    fun observeLikedIds(type: String, userId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM like_record WHERE targetType = :type AND targetId = :targetId")
    suspend fun count(type: String, targetId: Long): Int

    @Query("SELECT COUNT(*) FROM like_record WHERE targetType = :type AND targetId = :targetId")
    fun observeCountFlow(type: String, targetId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM like_record WHERE userId = :userId")
    fun observeGivenCount(userId: Long): Flow<Int>

    @Query("DELETE FROM like_record WHERE targetType = :type AND targetId = :targetId")
    suspend fun clearTarget(type: String, targetId: Long)

    @Query("DELETE FROM like_record")
    suspend fun clear()
}
