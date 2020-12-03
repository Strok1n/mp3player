package com.plcoding.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.plcoding.spotifyclone.Types.Companion.MEDIA_ROOT_ID
import com.plcoding.spotifyclone.Types.Companion.NETWORK_ERROR
import com.plcoding.spotifyclone.exoplayer.callbacks.MusicPlaybackPreparer
import com.plcoding.spotifyclone.exoplayer.callbacks.MusicPlayerEventListener
import com.plcoding.spotifyclone.exoplayer.callbacks.MusicPlayerNotificationListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


private const val SERVICE_TAG ="MusicService"



@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {
   // fileManager
    //
    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var expPlayer: SimpleExoPlayer
    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)


    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private var curPlayingSong : MediaMetadataCompat?=null

    private var isPlayerInitialized = false

    companion object{
        var curSongDuration = 0L
        private set
    }


    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()

        }

        val activityIntent =
            packageManager
                ?.getLaunchIntentForPackage(
                    packageName
                )?.let {
                    PendingIntent
                        .getActivity(this, 0, it, 0)
                }


        mediaSession = MediaSessionCompat(this, SERVICE_TAG)
            .apply {
                setSessionActivity(activityIntent)
                isActive = true
            }






        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource){
            curPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }




        sessionToken =
            mediaSession.sessionToken
    musicNotificationManager =
        MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ){


            curSongDuration = expPlayer.duration



        }


        mediaSessionConnector =
            MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())



            mediaSessionConnector.setPlayer(expPlayer)



        musicPlayerEventListener = MusicPlayerEventListener(this)

        expPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(expPlayer)

    }


    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession){


        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }


    }






    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ){
        val curSongIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        expPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        expPlayer.seekTo(curSongIndex, 0L)
        expPlayer.playWhenReady = playNow

    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        expPlayer.stop()
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        expPlayer.removeListener(musicPlayerEventListener)

        expPlayer.release()
    }


    override fun onGetRoot(// load a bunch of media items
        //
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(//client can subscribe to id
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            MEDIA_ROOT_ID ->{
                val resultsSent = firebaseMusicSource.whenReady { isInitialized ->
                    if(isInitialized){
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        if(!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()){

                            preparePlayer(firebaseMusicSource.songs, firebaseMusicSource.songs[0], false)

                            isPlayerInitialized = true

                        } else{
                            mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                            result.sendResult(null)
                        }
                    }
                }
                if(!resultsSent){
                    result.detach()
                }
            }
        }
    }

}