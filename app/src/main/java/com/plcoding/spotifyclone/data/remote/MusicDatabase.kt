package com.plcoding.spotifyclone.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.plcoding.spotifyclone.Types.Companion.SONG_COLLECTION
import com.plcoding.spotifyclone.data.entities.Song
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION)

    suspend fun getSongs(): List<Song>{
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception){
            emptyList()
        }
    }
}