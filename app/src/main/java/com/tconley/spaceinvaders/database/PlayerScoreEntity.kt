package com.tconley.spaceinvaders.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_score")
data class PlayerScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val score: Int
)
