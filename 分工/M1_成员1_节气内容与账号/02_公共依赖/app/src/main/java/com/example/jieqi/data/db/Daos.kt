package com.example.jieqi.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: CheckInEntity)

    @Delete
    suspend fun delete(checkIn: CheckInEntity)

    @Query("SELECT * FROM check_in ORDER BY date DESC")
    fun observeAll(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_in ORDER BY date DESC")
    suspend fun getAll(): List<CheckInEntity>

    @Query("SELECT * FROM check_in WHERE date = :date LIMIT 1")
    suspend fun findByDate(date: String): CheckInEntity?

    @Query("SELECT COUNT(*) FROM check_in")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM check_in WHERE date >= :fromDate")
    suspend fun countSince(fromDate: String): Int

    @Query("SELECT DISTINCT termIndex FROM check_in ORDER BY termIndex")
    fun observeVisitedTermIndexes(): Flow<List<Int>>

    @Query("DELETE FROM check_in")
    suspend fun clear()
}

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorite ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorite WHERE type = :type ORDER BY createdAt DESC")
    fun observeByType(type: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE key = :key)")
    fun observeExists(key: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE key = :key)")
    suspend fun exists(key: String): Boolean

    @Query("SELECT COUNT(*) FROM favorite")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM favorite WHERE key = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM favorite")
    suspend fun clear()
}

@Dao
interface FolkCustomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(custom: FolkCustomEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customs: List<FolkCustomEntity>)

    @Update
    suspend fun update(custom: FolkCustomEntity)

    @Delete
    suspend fun delete(custom: FolkCustomEntity)

    @Query("SELECT * FROM folk_custom ORDER BY province, city, id")
    fun observeAll(): Flow<List<FolkCustomEntity>>

    @Query("SELECT * FROM folk_custom WHERE termName = :termName OR termName IS NULL ORDER BY province")
    suspend fun findByTerm(termName: String): List<FolkCustomEntity>

    @Query("SELECT * FROM folk_custom WHERE province = :province ORDER BY city")
    suspend fun findByProvince(province: String): List<FolkCustomEntity>

    @Query("SELECT * FROM folk_custom WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): FolkCustomEntity?

    @Query("SELECT COUNT(*) FROM folk_custom")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM folk_custom WHERE isBuiltIn = 0")
    fun observeUserContributionCount(): Flow<Int>

    @Query("DELETE FROM folk_custom WHERE isBuiltIn = 0")
    suspend fun clearUserContributions()

    @Query("DELETE FROM folk_custom")
    suspend fun clear()
}
