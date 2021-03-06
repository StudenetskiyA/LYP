package com.example.lyp

import android.util.Log
import com.example.lyp.TagsFlag.*
import com.example.lyp.SortBy.*

enum class TagsFlag { And, Or }
enum class SortBy { Date, Count, Name, StarRating }

fun getAllSongsFromDb(): List<SongData>? {
    return mDb?.songDataDao()?.getAll()
}

fun isSongExistTest(path: String) {
    val task = Runnable {
        appState.count= mDb?.songDataDao()?.isExist(path)
        appState.mView.bindDataWithUi()
    }
    mDbSongsThread.postTask(task)
}

fun isSongExist(fullPath: String) : Boolean{
        val r =  mDb?.songDataDao()?.isExist(fullPath)
        return  if (r!=null) r>0 else false
}

fun getSongsFromDBToCurrentSongsList(tags: List<String>, tagsFlag: TagsFlag = Or, sort: SortBy = Name, searchName: String = "") {
    val task = Runnable {
        var result: List<SongData>? = if (!tags.isEmpty()) {
            if (tagsFlag == Or)
                getAllSongsFromDb()?.filter { it.tags.split(",").intersect(tags.asIterable()).isNotEmpty() }
            else
                getAllSongsFromDb()?.filter { it.tags.split(",").toMutableList().containsAll(tags) }
        } else getAllSongsFromDb()

        result = result?.filter { it.name.contains(searchName) }

        result = when (sort) {
            Name -> result?.sortedBy { it.name }
            Count -> result?.sortedBy { it.count }
            else -> {
                result?.sortedBy { it.date }
            }
        }

        if (result!=null) {
            appState.currentSongsList = result
            appState.currentShuffledSongsList = result
            appState.currentShuffledSongsList=appState.currentShuffledSongsList.shuffled()
        }
        else {
            //TODO Find nothing
        }
        Log.d(APP_TAG,"Songs found ${appState.currentSongsList.size}")
        appState.mView.bindDataWithUi()
    }
    mDbSongsThread.postTask(task)
}

fun insertSongDataToDb(songData: SongData) {
    val task = Runnable {
        //Log.i(APP_TAG, "insert to DB with name ${songData.name}")
        mDb?.songDataDao()?.insert(songData)
    }
    mDbSongsThread.postTask(task)
}

fun increaseRating (n:Int){

}

fun clearDb() {
    val task = Runnable {
        Log.i(APP_TAG, "Clear DB!")
        mDb?.songDataDao()?.deleteAll()
    }
    mDbSongsThread.postTask(task)
}