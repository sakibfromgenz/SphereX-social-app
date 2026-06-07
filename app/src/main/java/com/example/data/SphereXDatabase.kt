package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        PostEntity::class,
        ReelEntity::class,
        CommentEntity::class,
        MessageEntity::class,
        CommunityEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SphereXDatabase : RoomDatabase() {
    abstract val dao: SphereXDao

    companion object {
        @Volatile
        private var INSTANCE: SphereXDatabase? = null

        fun getDatabase(context: Context): SphereXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SphereXDatabase::class.java,
                    "spherex_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
