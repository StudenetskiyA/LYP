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

    fun getCurrentSong():SongData {
      return currentSongsList[currentSongIndex]
    }

    fun init(mv: MainActivity) {
        mView = mv
    }
}