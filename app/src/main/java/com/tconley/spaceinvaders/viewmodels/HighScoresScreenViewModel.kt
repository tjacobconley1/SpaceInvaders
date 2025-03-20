package com.tconley.spaceinvaders.viewmodels

import androidx.lifecycle.ViewModel
import com.tconley.spaceinvaders.database.PlayerScoreEntity
import com.tconley.spaceinvaders.database.PlayerTopScoresRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HighScoresScreenViewModel
@Inject constructor(private val repository: PlayerTopScoresRepository) : ViewModel() {

    private val _highScores = repository.getTopScores()
    val highScores: Flow<List<PlayerScoreEntity>> = _highScores
}
