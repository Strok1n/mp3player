package com.plcoding.spotifyclone.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.plcoding.spotifyclone.data.remote.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {


    @ServiceScoped
    @Provides
    fun provideMusicDatabase() =
        MusicDatabase()


    @ServiceScoped//same attributes in app service
    @Provides//it knows how
    fun provideAudioAttributes() =
        AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
        ) = SimpleExoPlayer.Builder(context)
        .build().apply {
            setAudioAttributes(audioAttributes,
            true)
            setHandleAudioBecomingNoisy(true)
        }

    @ServiceScoped
    @Provides
    fun providesDataSourceFactory(
        @ApplicationContext context: Context
    ) = DefaultDataSourceFactory(context,
    Util.getUserAgent(context, "Spotify app"))



}