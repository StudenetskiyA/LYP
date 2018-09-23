package com.example.lyp

import android.content.Context
import android.util.Log

class LYPService (val context: Context, val mView: MainActivity){

    val appState = AppState()

    fun changeTag (tag: String, song: String) {

    }

    fun insertSongDataToDb(songData: SongData) {
        val task = Runnable {
            Log.i(APP_TAG,"insert to DB with name ${songData.name}")
            mDb?.songDataDao()?.insert(songData) }
        mDbSongsThread.postTask(task)
    }

    fun getSongDataFromDb(songName:String) {
        val task = Runnable {
            Log.i(APP_TAG,"getSong from DB with name '$songName'")
            val songData = mDb?.songDataDao()?.findByName(songName)
            mUiHandler.post {
                if (songData == null )  {
                   // toast("No song with name '$songName' in database!!")
                } else {
                    Log.i(APP_TAG,"readed from DB tag '${songData.tags}'")
                    appState.currentSong = songData
                   bindUI()
                }
            }
        }
        mDbSongsThread.postTask(task)
    }

    fun bindUI() {
        mView.bindDataWithUi()
    }
}