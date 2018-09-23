package com.example.lyp

data class AppState (
        var currentSong: SongData = SongData(0,""),
        var currentSongTags: String = ""
)