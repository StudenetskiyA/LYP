package com.example.lyp

import com.example.lyp.PlayState.*

enum class PlayState {Play , Stop, Pause}
enum class ShuffleState {Shuffle, NoShuffle}
enum class SortState {ByName, ByDate, ByCount}
enum class RepeatState {All, One, Stop}

class AppState (
        var currentSongIndex: Int = 0,
        var count: Int? = 0,
        var currentSongsList:List<SongData> = ArrayList(),
        var currentShuffledSongsList:List<SongData> = ArrayList(),
        var playState:PlayState = Stop,
        var shuffleState:ShuffleState = ShuffleState.NoShuffle,
        var repeatState: RepeatState = RepeatState.All,
        var allTags:String = ""
) {
    lateinit var mView: MainActivity

    fun getCurrentSong():SongData? {
        return if (currentSongsList.isNotEmpty() && currentSongIndex<currentSongsList.size)
            currentSongsList[currentSongIndex]
        else null
    }

    fun getIndexInListFromSLdelta(delta :Int ) :Int {
        return getIndexInListFromShuffledListIndex(getCurrentSongIndexInShuffledList()+delta)
    }

    fun getIndexInListFromShuffledListIndex(index : Int) :Int {
        return currentSongsList.indexOf(currentShuffledSongsList[index])
    }

    fun getCurrentSongIndexInShuffledList() :Int {
        return currentShuffledSongsList.indexOf(getCurrentSong())
    }

    fun init(mv: MainActivity) {
        mView = mv
    }
}