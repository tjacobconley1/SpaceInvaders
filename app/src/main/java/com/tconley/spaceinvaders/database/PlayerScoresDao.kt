package com.tconley.spaceinvaders.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerScoresDao {

    @Query("SELECT * FROM player_score ORDER BY score DESC")
    fun getTopScores(): Flow<List<PlayerScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerScore(playerScore: PlayerScoreEntity)
}
