package com.example.spotifyclone.Firebase

import android.util.Log
import com.example.spotifyclone.models.Song
import com.example.spotifyclone.utils.Constants.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val firestore=FirebaseFirestore.getInstance()
    private val songCollection=firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs():List<Song>{
        return try {
            songCollection.get().await().toObjects(Song::class.java)


        }
        catch (e:Exception){
            Log.d("PP", e.toString())
            emptyList()
        }
    }
}