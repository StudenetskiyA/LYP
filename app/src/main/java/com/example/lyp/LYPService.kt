package com.example.lyp

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import java.util.*
import kotlin.concurrent.fixedRateTimer

const val EXTRA_COMMAND = "EXTRA_COMMAND"
//enum class SERVICE_COMMAND(val command:Int) {Start(1) , Stop(0)}

class LYPService: Service()  {

    val appState = AppState()

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

        var command = intent.getIntExtra(EXTRA_COMMAND, 0)

//        val notificationIntent = Intent(this, MainActivity::class.java)

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

        Log.i(APP_TAG, "command = $command")
        when (command) {
            0 -> {

            }
            1 -> {
                fixedRateTimer("default", false, 0L, 1000){
                    appState.count++
                    Log.i(APP_TAG, "count now = ${appState.count}")
                    bindUI()
                }
            }
        }
        startForeground(9595, notification)
        return START_STICKY
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

    private fun bindUI() {
            mView.bindDataWithUi()
    }
}