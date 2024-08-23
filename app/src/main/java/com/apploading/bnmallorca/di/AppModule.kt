package com.apploading.bnmallorca.di

import android.content.Context
import android.content.SharedPreferences
import com.apploading.bnmallorca.bncore.PlayManager
import com.apploading.bnmallorca.bncore.PushManager
import com.apploading.bnmallorca.bncore.TrackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("trackSharedPreferences")
    fun provideTrackSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return TrackManager.getSharedPreferencesForTrack(context)
    }

    @Provides
    @Singleton
    @Named("pushSharedPreferences")
    fun providePushSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PushManager.getSharedPreferencesForPush(context)
    }

    @Provides
    @Singleton
    @Named("playSharedPreferences")
    fun providePlaySharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PlayManager.getSharedPreferencesForPlay(context)
    }
}