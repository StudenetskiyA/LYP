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
        val notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_icon_background)
                .setWhen(0)
                .setContentIntent(callIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentTitle("LYM-player")
                .setContentText(appState.currentSong.name)
                .build()

        //Proceed command
        Log.i(APP_TAG, "command = $command")
        when (command) {
            ServiceCommand.Stop -> {
               stop()
                appState.currentSong = SongData(0,"","")
            }
            Start -> {
                stop()
                play()
            }
            Next -> {
                stop()
                if (appState.shuffleState==ShuffleState.NoShuffle) { //TODO Add rating
                    if (!appState.currentSongsList.isEmpty()) {
                        if (appState.currentSongsList.indexOf(appState.currentSong) != appState.currentSongsList.size-1 &&
                                appState.currentSongsList.indexOf(appState.currentSong) != -1) {
                            appState.currentSong = appState.currentSongsList[appState.currentSongsList.indexOf(appState.currentSong) + 1]
                        } else {
                            appState.currentSong = appState.currentSongsList[0]
                        }
                        play()
                    }
                }
                else {
                    if (!appState.currentShuffledSongsList.isEmpty()) {
                        if (appState.currentShuffledSongsList.indexOf(appState.currentSong) != appState.currentShuffledSongsList.size-1 &&
                                appState.currentShuffledSongsList.indexOf(appState.currentSong) != -1) {
                            appState.currentSong = appState.currentShuffledSongsList[appState.currentShuffledSongsList.indexOf(appState.currentSong) + 1]
                        } else {
                            appState.currentSong = appState.currentShuffledSongsList[0]
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
                            appState.currentSongsList.indexOf(appState.currentSong) == -1 -> appState.currentSong = appState.currentSongsList[0]
                            appState.currentSongsList.indexOf(appState.currentSong) == 0 -> appState.currentSong = appState.currentSongsList[appState.currentSongsList.lastIndex]
                            else -> appState.currentSong = appState.currentSongsList[appState.currentSongsList.indexOf(appState.currentSong) - 1]
                        }
                        play()
                    }
                }
                else {
                    if (!appState.currentShuffledSongsList.isEmpty()) {
                        when {
                            appState.currentShuffledSongsList.indexOf(appState.currentSong) == -1 ->
                                appState.currentSong = appState.currentShuffledSongsList[0]
                            appState.currentShuffledSongsList.indexOf(appState.currentSong) == 0 ->
                                appState.currentSong = appState.currentShuffledSongsList[appState.currentShuffledSongsList.lastIndex]
                            else ->
                                appState.currentSong = appState.currentShuffledSongsList[appState.currentShuffledSongsList.indexOf(appState.currentSong) - 1]
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
        val uri = Uri.parse(appState.currentSong.path)
        mPlayer = MediaPlayer.create(this, uri)
        mPlayer?.start()
        appState.playState = Play
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