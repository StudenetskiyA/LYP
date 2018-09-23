package com.example.lyp

import android.util.Log
import com.example.lyp.TagsFlag.*
import com.example.lyp.SortBy.*

enum class TagsFlag { And, Or }
enum class SortBy { Date, Count, Name, StarRating }

fun getAllSongsFromDb(): List<SongData>? {
    return mDb?.songDataDao()?.getAll()
}

fun isSongExistTest(name: String, path: String) {
    val task = Runnable {
        appState.count= mDb?.songDataDao()?.isExist(name,path)
        mView.bindDataWithUi()
    }
    mDbSongsThread.postTask(task)
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

        when (sort) {
            Name -> result = result?.sortedBy { it.name }
            Date -> result = result?.sortedBy { it.date }
            Count -> result = result?.sortedBy { it.count }
        }
        appState.currentSongsList = result
        mView.bindDataWithUi()
    }
    mDbSongsThread.postTask(task)
}

fun insertSongDataToDb(songData: SongData) {
    val task = Runnable {
        Log.i(APP_TAG, "insert to DB with name ${songData.name}")
        mDb?.songDataDao()?.insert(songData)
    }
    mDbSongsThread.postTask(task)
}

fun clearDb() {
    val task = Runnable {
        Log.i(APP_TAG, "Clear DB!")
        mDb?.songDataDao()?.deleteAll()
    }
    mDbSongsThread.postTask(task)
}