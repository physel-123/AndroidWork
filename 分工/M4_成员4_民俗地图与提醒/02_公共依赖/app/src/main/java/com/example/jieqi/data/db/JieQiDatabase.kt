package com.example.jieqi.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CheckInEntity::class,
        FavoriteEntity::class,
        FolkCustomEntity::class,
        UserEntity::class,
        PostEntity::class,
        CommentEntity::class,
        LikeEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class JieQiDatabase : RoomDatabase() {

    abstract fun checkInDao(): CheckInDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun folkCustomDao(): FolkCustomDao

    abstract fun userDao(): UserDao

    abstract fun postDao(): PostDao

    abstract fun commentDao(): CommentDao

    abstract fun likeDao(): LikeDao

    companion object {
        private const val DB_NAME = "jieqi.db"

        @Volatile
        private var instance: JieQiDatabase? = null

        fun get(context: Context): JieQiDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    JieQiDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
