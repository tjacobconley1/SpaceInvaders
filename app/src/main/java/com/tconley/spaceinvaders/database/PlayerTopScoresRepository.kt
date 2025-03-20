package com.tconley.spaceinvaders.database

import kotlinx.coroutines.flow.Flow

class PlayerTopScoresRepository(private val playerScoresDao: PlayerScoresDao) {

    suspend fun insertPlayerScore(playerScore: PlayerScoreEntity) {
        playerScoresDao.insertPlayerScore(playerScore)
    }

    fun getTopScores(): Flow<List<PlayerScoreEntity>> {
        return playerScoresDao.getTopScores()
    }
}
