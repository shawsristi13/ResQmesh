package com.meshmap.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MeshMessageEntity::class], version = 1, exportSchema = false)
abstract class MeshDatabase : RoomDatabase() {
    abstract fun meshMessageDao(): MeshMessageDao

    companion object {
        @Volatile
        private var INSTANCE: MeshDatabase? = null

        fun getDatabase(context: Context): MeshDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeshDatabase::class.java,
                    "mesh_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
