package com.tconley.spaceinvaders.hilt

import android.content.Context
import com.tconley.spaceinvaders.database.PlayerScoresDao
import com.tconley.spaceinvaders.database.PlayerTopScoresDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PlayerScoresDatabaseModule {

    @Provides
    @Singleton
    fun providePlayerScoresDatabase(
        @ApplicationContext context: Context
    ): PlayerTopScoresDatabase {
        return PlayerTopScoresDatabase.getPlayerTopScoresDatabaseInstance(context)
    }

    @Provides
    @Singleton
    fun providePlayerScoresDao(database: PlayerTopScoresDatabase): PlayerScoresDao {
        return database.playerScoresDao()
    }
}
