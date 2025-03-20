package com.tconley.spaceinvaders.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlayerScoreEntity::class], version = 1, exportSchema = false)
abstract class PlayerTopScoresDatabase : RoomDatabase() {
    abstract fun playerScoresDao(): PlayerScoresDao

    companion object {
        @Volatile
        private var INSTANCE: PlayerTopScoresDatabase? = null
        fun getPlayerTopScoresDatabaseInstance(context: Context): PlayerTopScoresDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    PlayerTopScoresDatabase::class.java,
                    "player_scores"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
