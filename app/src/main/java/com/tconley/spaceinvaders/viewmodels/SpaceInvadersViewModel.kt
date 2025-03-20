package com.tconley.spaceinvaders.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tconley.spaceinvaders.database.PlayerScoreEntity
import com.tconley.spaceinvaders.database.PlayerTopScoresRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpaceInvadersViewModel
@Inject constructor(private val playerTopScoresRepository: PlayerTopScoresRepository) : ViewModel() {

    private val _topScoresData = playerTopScoresRepository.getTopScores()
    val topScoresData = _topScoresData

    fun InsertPlayerScore(name: String, score: Int) {
        try {
            viewModelScope.launch {
                val playerScore = PlayerScoreEntity(name = name, score = score)
                playerTopScoresRepository.insertPlayerScore(playerScore)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("InsertPlayerScore", e.message.toString())
        }
    }

    fun GetPlayerTopScores(): Flow<List<PlayerScoreEntity>> {
        return playerTopScoresRepository.getTopScores()
    }
}
