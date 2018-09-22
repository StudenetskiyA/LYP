package com.example.lyp

import android.os.Handler
import android.os.HandlerThread

class DbSongsThread(threadName: String) : HandlerThread(threadName) {

    private lateinit var mSongsHandler: Handler

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        mSongsHandler = Handler(looper)
    }

    fun postTask(task: Runnable) {
        mSongsHandler.post(task)
    }

}