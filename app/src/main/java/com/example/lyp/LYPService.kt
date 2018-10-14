package com.example.lyp

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.lyp.ServiceCommand.*
import com.example.lyp.PlayState.*

const val EXTRA_COMMAND = "EXTRA_COMMAND"
enum class ServiceCommand {Start , Stop, Next, Prev}

val appState = AppState()

class LYPService: Service()  {
    private var mPlayer : MediaPlayer? = null

    private val myBinder = MyLocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    inner class MyLocalBinder : Binder() {
        fun getService() : LYPService {
            return this@LYPService
        }

    }

    override fun onStartCommand(intent :Intent, flags :Int, startId :Int) : Int {
        Log.i(APP_TAG, "Try to start service")

        //Hmm... may this return not ServiceCommand?
        val command = intent.getSerializableExtra(EXTRA_COMMAND) as ServiceCommand

        val callIntent = PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        val title = if (appState.getCurrentSong()!=null) appState.getCurrentSong()!!.name
        else "no title"
        val notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_icon_background)
                .setWhen(0)
                .setContentIntent(callIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentTitle("LYM-player")
                .setContentText(title)
                .build()

        //Proceed command
        Log.i(APP_TAG, "command = $command")
        when (command) {
            ServiceCommand.Stop -> {
               stop()
              //  appState.getCurrentSong() = SongData(0,"","")
            }
            Start -> {
                stop()
                play()
            }
            Next -> {
                stop()
                if (appState.shuffleState==ShuffleState.NoShuffle) { //TODO Add rating
                    if (!appState.currentSongsList.isEmpty()) {
                        if (appState.currentSongIndex != appState.currentSongsList.size-1 &&
                                appState.currentSongIndex != -1) {
                            appState.currentSongIndex = appState.currentSongIndex + 1
                        } else {
                            appState.currentSongIndex = 0
                        }
                        play()
                    }
                }
                else {
                    if (!appState.currentShuffledSongsList.isEmpty()) {
                        if (appState.getCurrentSongIndexInShuffledList() != appState.currentShuffledSongsList.size-1 &&
                                appState.getCurrentSongIndexInShuffledList() != -1) {
                            appState.currentSongIndex = appState.getIndexInListFromSLdelta(1)
                        } else {
                            appState.currentSongIndex = appState.getIndexInListFromShuffledListIndex(0)
                        }
                        play()
                    }
                }
            }
            Prev -> {
                stop()
                if (appState.shuffleState==ShuffleState.NoShuffle) {
                    if (!appState.currentSongsList.isEmpty()) {
                        when {
                            appState.currentSongIndex == -1 -> appState.currentSongIndex = 0
                            appState.currentSongIndex == 0 -> appState.currentSongIndex = appState.currentSongsList.lastIndex
                            else -> appState.currentSongIndex = appState.currentSongIndex - 1
                        }
                        play()
                    }
                }
                else {
                    if (!appState.currentShuffledSongsList.isEmpty()) {
                        when {
                            appState.getCurrentSongIndexInShuffledList() == -1 ->
                                appState.currentSongIndex = appState.getIndexInListFromShuffledListIndex(0)
                            appState.getCurrentSongIndexInShuffledList() == 0 ->
                                appState.currentSongIndex = appState.getIndexInListFromShuffledListIndex(
                                        appState.currentShuffledSongsList.lastIndex)
                            else ->
                                appState.currentSongIndex = appState.getIndexInListFromSLdelta( -1)
                        }
                        play()
                    }
                }
            }
        }

        bindUI()

        startForeground(9595, notification)
        return START_STICKY
    }

    private fun play() {
        if (appState.getCurrentSong()!=null) {
            val uri = Uri.parse(appState.getCurrentSong()!!.path)
            mPlayer = MediaPlayer.create(this, uri)
            mPlayer?.start()
            appState.playState = Play
        }
    }

    private fun stop() {
        appState.playState = PlayState.Stop
        mPlayer?.stop()
      //  appState.currentSong = SongData(0,"","")
    }

    private fun bindUI() {
            appState.mView.bindDataWithUi()
    }
}