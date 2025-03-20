package com.tconley.spaceinvaders.hilt

import com.tconley.spaceinvaders.database.PlayerScoresDao
import com.tconley.spaceinvaders.database.PlayerTopScoresRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerScoresRepositoryModule {
    @Provides
    @Singleton
    fun providePlayerScoresRepository(playerScoresDao: PlayerScoresDao): PlayerTopScoresRepository {
        return PlayerTopScoresRepository(playerScoresDao)
    }
}
