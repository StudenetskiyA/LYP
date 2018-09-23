package com.example.lyp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.view.MotionEvent

const val APP_TAG = "lyp-tag"

lateinit var mDbSongsThread: DbSongsThread
val mUiHandler = Handler()
var mDb: SongDataBase? = null
lateinit var mView : MainActivity


class MainActivity : AppCompatActivity() {
    var serv: LYPService? = null
    var isBound = false

    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as LYPService.MyLocalBinder
            serv = binder.getService()
            isBound = true
            Log.i(APP_TAG,"Service binded.")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            Log.i(APP_TAG,"Service unbinded.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(APP_TAG,"App started.")

        val intent = Intent(this, LYPService::class.java)
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE)

        mView = this


        setContentView(R.layout.activity_main)

        setSupportActionBar(bottom_app_bar)
        initFab()

        //DATABASE
        Log.i(APP_TAG,"Thread to create/open database started.")
        mDbSongsThread = DbSongsThread("dbSongsThread")
        mDbSongsThread.start()
        mDb = SongDataBase.getInstance(this)

        Log.i(APP_TAG,"End onCreate.")
    }

    private fun initFab() {
        fab.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0.toFloat()
            var startRawX = 0.toFloat()
            var startY = 0.toFloat()
            var startRawY = 0.toFloat()

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = view.x - event.rawX
                        startRawX = event.rawX
                        startY = view.y - event.rawY
                        startRawY = event.rawY
                    }

                    MotionEvent.ACTION_UP -> {
                        var distanceX: Float = event.rawX - startRawX
                        var distanceY: Float = event.rawY - startRawY
                        val distance = Math.sqrt(Math.abs(distanceX).toDouble() + Math.abs(distanceY).toDouble())
                        if (distance > 5) {
                            when {
                                Math.abs(distanceX) > Math.abs(distanceY) && distanceX > 0 -> {
                                    //toast(getString(R.string.fab_draged_right))
                                    serv?.insertSongDataToDb(SongData(1,"name",tags="test tag"))
                                }
                                Math.abs(distanceX) > Math.abs(distanceY) && distanceX < 0 -> {
                                    // toast(getString(R.string.fab_draged_left))
                                    serv?.getSongDataFromDb("name")
                                }
                                Math.abs(distanceX) < Math.abs(distanceY) && distanceY < 0 -> {
                                    toast(getString(R.string.fab_draged_up))
                                    val intent = Intent(this@MainActivity, LYPService::class.java)
                                    intent.putExtra(EXTRA_COMMAND, 1)
                                    startService(intent)
                                }
                                Math.abs(distanceX) < Math.abs(distanceY) && distanceY > 0 -> {
                                    toast(getString(R.string.fab_draged_down))
                                    val intent = Intent(this@MainActivity, LYPService::class.java)
                                    intent.putExtra(EXTRA_COMMAND, 0)
                                    startService(intent)
                                }
                            }
                        } else {
                            toast(getString(R.string.settings_clicked))
                        }
                    }

                    else -> return false
                }
                return true
            }
        })
    }



    fun bindDataWithUi() {
        this@MainActivity.runOnUiThread {
            Log.i(APP_TAG, "UI bind")
            hello_label.text = serv?.appState?.count.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.bottomappbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.app_bar_fav -> toast(getString(R.string.fav_clicked))
            R.id.app_bar_search -> toast(getString(R.string.search_clicked))
            R.id.app_bar_settings -> toast(getString(R.string.settings_clicked))
            android.R.id.home -> {
                val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
            }
        }
        return true
    }

    // This is an extension method for easy Toast call
    fun Context.toast(message: CharSequence) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 325)
        toast.show()
    }

    override fun onDestroy() {
        SongDataBase.destroyInstance()
        mDbSongsThread.quit()
        super.onDestroy()
    }
}
