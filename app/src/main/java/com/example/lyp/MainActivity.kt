package com.example.lyp

import android.content.*
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ListView
import com.example.lyp.ServiceCommand.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.os.Environment
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.*
import android.widget.RelativeLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import com.google.android.material.bottomappbar.BottomAppBar
import android.util.TypedValue
import android.widget.TextView
import kotlin.collections.ArrayList

val APP_PREFERENCES = "mysettings"
val APP_PREFERENCES_MUSIC_FOLDER = "musicfolder"
val APP_PREFERENCES_LINKS = "links"
val APP_PREFERENCES_RANDOM = "random"
val APP_PREFERENCES_REPEAT = "repeat"
val APP_PREFERENCES_ANDOR = "andor"
val APP_PREFERENCES_LAST_DATE = "lastdate"
val APP_PREFERENCES_PAGE = "page"
val APP_PREFERENCES_SEARCH_STRING = "searchstring"
val APP_PREFERENCES_ANTISEARCH_STRING = "antisearchstring"
val APP_PREFERENCES_SEARCH_SPECIAL = "searchspecial"
val APP_PREFERENCES_CURRENT_SONG = "currentsong"
private val APP_PREFERENCES_BUTTON_PADDING = "buttonpadding"


const val APP_TAG = "lyp-tag"
var APP_PATH: String = ""
private lateinit var mSettings: SharedPreferences
var editor: SharedPreferences.Editor? = null
val mDbSongsThread: DbSongsThread = DbSongsThread("dbSongsThread")
var mDb: SongDataBase? = null
lateinit var adapter: SongListAdapter
var namesInList = ArrayList<ViewSongOnList>()

class MainActivity : AppCompatActivity() {
    var serv: LYPService? = null
    var isBound = false
    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as LYPService.MyLocalBinder
            serv = binder.getService()
            isBound = true
            Log.i(APP_TAG, "Service binded.")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            Log.i(APP_TAG, "Service unbinded.")
        }
    }
    private lateinit var listView: ListView
    private var startFabX = 0
    private var startFabY = 0
    private var topHeight = 0
    private var bottomHeight = 0
    private var totalHeight = 0
    var linksSelect = ArrayList<String>()

    fun bindDataWithUi() {
        this@MainActivity.runOnUiThread {
            Log.i(APP_TAG, "UI bind")

            //Bind song list
            namesInList.clear()
            for (song in appState.currentSongsList) {
                namesInList.add(ViewSongOnList(song.name, song.duration))
            }
            //Bind selected track in list
            val n = appState.currentSongsList.indexOf(appState.currentSong)
            if (n != -1) {
                current_list.setItemChecked(n, true)
                current_list.smoothScrollToPosition(n)
            }
            adapter.notifyDataSetChanged()

            //Bind current track
            track_name.text = appState.currentSong.name

            //Bind total found
            tracks_found.text = getString(R.string.tracks_found_part1) + appState.currentSongsList.size + getString(R.string.tracks_found_part2)

            //TODO UI Bind main button
            when (appState.playState) {
                PlayState.Play -> fab.setImageDrawable(resources.getDrawable(R.drawable.pause))
                PlayState.Stop, PlayState.Pause -> fab.setImageDrawable(resources.getDrawable(R.drawable.play))
            }
        }
    }

    private fun getPointOfView(view: View): Point {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        return Point(location[0], location[1])
    }

    private fun initFab() {
        //val step = 4
        val startCircle = 100
        val fabButton: FloatingActionButton = findViewById(R.id.fab)

        fabButton.post {
            // this callback will be executed after view is laid out
            val point = getPointOfView(fabButton)
            startFabX = point.x + fabButton.width / 2
            startFabY = point.y + fabButton.height / 2
            Log.i(APP_TAG, "fab first position is $startFabX / $startFabY")
        }

        fab.setOnTouchListener(View.OnTouchListener { view, event ->
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()
            val distanceX: Int = x - startFabX
            val distanceY: Int = y - startFabY
            val distance = Math.sqrt(Math.pow(Math.abs(distanceX).toDouble(), 2.0) + Math.pow(Math.abs(distanceY).toDouble(), 2.0))

            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    if (distance >= startCircle) {
                        when {
                            Math.abs(distanceX) > Math.abs(distanceY) && distanceX > 0 -> { //left
                                fab.setImageDrawable(resources.getDrawable(R.drawable.next))
                            }
                            Math.abs(distanceX) > Math.abs(distanceY) && distanceX < 0 -> { //right
                                fab.setImageDrawable(resources.getDrawable(R.drawable.prev))
                            }
                            Math.abs(distanceX) < Math.abs(distanceY) && distanceY < 0 -> { //up
                                fab.setImageDrawable(resources.getDrawable(R.drawable.edit))
                            }
                            Math.abs(distanceX) < Math.abs(distanceY) && distanceY > 0 -> { //down
                                fab.setImageDrawable(resources.getDrawable(R.drawable.less))
                            }
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    // distance = Math.sqrt(Math.pow(Math.abs(distanceX).toDouble(), 2.0) + Math.pow(Math.abs(distanceY).toDouble(), 2.0))
                    Log.i(APP_TAG, "Action up, distanceX=$distanceX , distanceY=$distanceY , total = $distance")
                    if (distance >= startCircle) {
                        when {
                            Math.abs(distanceX) > Math.abs(distanceY) && distanceX > 0 -> {
                                val intent = Intent(this@MainActivity, LYPService::class.java)
                                intent.putExtra(EXTRA_COMMAND, Next)
                                startService(intent)
                            }
                            Math.abs(distanceX) > Math.abs(distanceY) && distanceX < 0 -> {
                                val intent = Intent(this@MainActivity, LYPService::class.java)
                                intent.putExtra(EXTRA_COMMAND, Prev)
                                startService(intent)
                            }
                            Math.abs(distanceX) < Math.abs(distanceY) && distanceY < 0 -> {
                                showLinksBar(true)
                            }
                            Math.abs(distanceX) < Math.abs(distanceY) && distanceY > 0 -> {
                                showLinksBar(false)
                            }
                        }
                    } else {
                        if (appState.playState == PlayState.Play) {
                            val intent = Intent(this@MainActivity, LYPService::class.java)
                            intent.putExtra(EXTRA_COMMAND, Stop)
                            startService(intent)
                        } else {
                            val intent = Intent(this@MainActivity, LYPService::class.java)
                            intent.putExtra(EXTRA_COMMAND, Start)
                            startService(intent)
                        }
                    }
                    when (appState.playState) {
                        PlayState.Play -> fab.setImageDrawable(resources.getDrawable(R.drawable.pause))
                        else -> fab.setImageDrawable(resources.getDrawable(R.drawable.play))
                    }
                }
                else -> return@OnTouchListener false
            }
            true
        })
    }

    private fun createListener() {
        initFab()
        current_list.setOnItemClickListener { parent, view, position, id ->
            appState.currentSong = appState.currentSongsList[position]
            val intent = Intent(this@MainActivity, LYPService::class.java)
            intent.putExtra(EXTRA_COMMAND, Start)
            startService(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(APP_TAG, "App started.")
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.current_list)
        adapter = SongListAdapter(namesInList, this)
        listView.adapter = adapter

        //Binding the service
        appState.init(this)
        val intent = Intent(this, LYPService::class.java)
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE)

        //Init database
        Log.i(APP_TAG, "Thread to create/open database started.")
        mDbSongsThread.start()
        mDb = SongDataBase.getInstance(this)


//        insertSongDataToDb(SongData(name = "test name", path = "test path" + "/" + "test name",
//                date = "000", duration = 0))


        val layout0 = findViewById<RelativeLayout>(R.id.all_layout)
        layout0.afterMeasured {
            totalHeight = layout0.height
            if (totalHeight != 0 && topHeight != 0 && bottomHeight != 0) showLinksBar(false)
            Log.d(APP_TAG, "Total height: " + layout0.height)
        }

        val layout = findViewById<LinearLayout>(R.id.filtersbar)
        layout.afterMeasured {
            topHeight = layout.height
            if (totalHeight != 0 && topHeight != 0 && bottomHeight != 0) showLinksBar(false)
            Log.d(APP_TAG, "Top bar height: " + layout.height)
        }

        val layout2 = findViewById<BottomAppBar>(R.id.bottom_app_bar)
        layout2.afterMeasured {
            bottomHeight = layout2.height
            if (totalHeight != 0 && topHeight != 0 && bottomHeight != 0) showLinksBar(false)
            Log.d(APP_TAG, "Bottom bar height: " + layout2.height)
        }

        readSettings()
        readMusicDir()

        //Set up view
        createListener()
        setSupportActionBar(bottom_app_bar)

        Log.i(APP_TAG, "End onCreate.")
    }

    private inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }

    private fun showLinksBar(show: Boolean) {
        if (show) {
            tags_bar.visibility = View.VISIBLE
            val tagsLayout = findViewById<LinearLayout>(R.id.tags_bar)
            val layoutParams = tagsLayout.layoutParams as android.widget.RelativeLayout.LayoutParams
            val pxBottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomHeight - 100f + 8f, resources.getDisplayMetrics())

            layoutParams.setMargins(0, 0, 0, Math.round(pxBottomMargin))
            tagsLayout.layoutParams = layoutParams

            val innerLayout = findViewById<ListView>(R.id.current_list)
            val params = innerLayout.layoutParams
            tagsLayout.afterMeasured {
                params.height = totalHeight - bottomHeight - topHeight - tagsLayout.height
                Log.d(APP_TAG, "Set list layout height ${params.height}")
                innerLayout.layoutParams = params
                val n = appState.currentSongsList.indexOf(appState.currentSong)
                if (n != -1) {
                    current_list.setItemChecked(n, true)
                    current_list.smoothScrollToPositionFromTop(n, 2)
                }
                adapter.notifyDataSetChanged()
            }
        } else {
            tags_bar.visibility = View.GONE
            val layout = findViewById<ListView>(R.id.current_list)
            val params = layout.layoutParams
            params.height = totalHeight - bottomHeight - topHeight
            Log.d(APP_TAG, "Set height ${params.height}")
            layout.layoutParams = params
        }
    }

    private fun readSettings() {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        editor = mSettings.edit()
        APP_PATH = Environment.getExternalStorageDirectory().path + "/music/"
        ///= Environment.getExternalStorageDirectory().absolutePath
        Log.i(APP_TAG, "App path is $APP_PATH")
    }

    private fun readMusicDir() {
        val task = Runnable {
            val f = File(APP_PATH)//folder
            val file = f.listFiles()//TODO Include subfolder
            var newSongAdded = 0

            //Check access to directory
            if (f.isDirectory && f.exists() && f.canRead()) {
                Log.i(APP_TAG, "Music folder is totally OK.")
                for (i in 0 until file.size) {
                    if (file[i].name.endsWith(".mp3")) {
                        if (!isSongExist(file[i].path.getPathWithoutName() + "/" + file[i].name)) {
                            //Add new to database
                            //TODO Remove mp3 from name
                            val sdf = SimpleDateFormat("dd/M/yyyy HH:mm:ss")
                            val currentDate = sdf.format(Date())
                            val sd = SongData(name = file[i].name, path = file[i].path.getPathWithoutName() + "/" + file[i].name,
                                    date = currentDate, duration = 0)
                            Log.i(APP_TAG, "New song added : $sd")
                            insertSongDataToDb(sd)
                            newSongAdded++
                        }
                    }
                }
                Log.i(APP_TAG, "$newSongAdded new songs added.")
                getSongsFromDBToCurrentSongsList(listOf())
                if (appState.currentSongsList.isNotEmpty())//TODO && if !play
                    appState.currentSong = appState.currentSongsList[0]
            } else {
                Log.i(APP_TAG, "Trouble with music folder.")
            }
        }
        mDbSongsThread.postTask(task)
    }

    var menu: Menu? = null
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.bottomappbar_menu, menu)
        return true
    }

//    internal fun buildLinkField(): Boolean {
//        val definition = "".trim({ it <= ' ' })
//        amf.linkField.setMovementMethod(LinkMovementMethod.getInstance())
//        amf.linkField.setText(definition, TextView.BufferType.SPANNABLE)
//
//        val spans = amf.linkField.getText() as Spannable
//        val indices = amf.getSpaceIndices(amf.linkField.getText().toString(), SPACE_IN_LINK)
//        var start = 0
//        var end = 0
//        for (i in 0..indices.size) {
//            val clickSpan = object : ClickableSpan() {
//                override fun onClick(widget: View) {
//                    val tv = widget as TextView
//                    val s = tv.text.subSequence(tv.selectionStart, tv.selectionEnd).toString()
//                    Log.i(APPLICATION_TAG, "from link clicked $s")
//                    if (s != "") {
//                        addLinkToText(s)
//                    }
//                    buildLinkField()
//                }
//
//                override fun updateDrawState(ds: TextPaint) {}
//            }
//            // to cater last/only word
//            end = if (i < indices.size) indices[i] else spans.length
//            spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//            start = end + 1
//        }
//        val txt = amf.tagField.getText().toString()
//        var compareText = amf.linkField.getText().toString()
//        compareText = "; $compareText"
//        for (i in linkSelect.indices) {
//            if (txt.contains(linkSelect[i] + SPACE_IN_LINK)) {
//                Log.d(APP_TAG, "link must be orange = " + linkSelect[i])
//                val startIndex = compareText.indexOf("; " + linkSelect[i] + SPACE_IN_LINK, 0)
//                if (startIndex != -1) {
//                    spans.setSpan(ForegroundColorSpan(resources.getColor(R.color.selectLink)), startIndex, startIndex + linkSelect[i].length, Spannable.SPAN_PRIORITY_SHIFT)
//                }
//            }
//        }
//        amf.linkField.setText(spans)
//        buildLinkFieldSearch()
//        return true
//    }

    private fun addTagToSettings(s: String) {
    //Добавляет тег в базу
    if (tags.text.contains(s)) {
        //TODO Message - tag already exist
    } else {
        s.trim { it <= ' ' }
        //Добавляем в настройки приложения новый тег.
        editor?.putString(APP_PREFERENCES_LINKS, tags.text.toString() + s + "; ")
        editor?.apply()
        //Снова читаем из настроек.
        //linkField = readLinkFieldFromSettings()
        //buildLinkField()
    }
}

    private fun readLinkFieldFromSettings(): String {
        //Возвращает все теги из настроек.
        val defaultValue = resources.getString(R.string.default_tags)
        var links: String?
        var start = 0
        var i = 0
        var count = 0
        if (mSettings.contains(APP_PREFERENCES_LINKS)) {
            links = mSettings.getString(APP_PREFERENCES_LINKS, defaultValue)
            Log.d(APP_TAG, "Links from settings load: " + links);
        } else
            links = defaultValue
        if (links == "") links = defaultValue

        while (links!!.indexOf(";", start) != -1) {
            count++
            start = links.indexOf(";", start) + 2
        }
        linksSelect = ArrayList(count)
        start = 0
        val names = ArrayList<String>()
        while (links.indexOf(";", start) != -1) {
            linksSelect[i] = links.substring(start, links.indexOf(";", start))
            names.add(linksSelect[i])
            start = links.indexOf(";", start) + 2
            // Log.i(APPLICATION_TAG, linkSelect[i]);
            i++
        }
        names.sort()
        links = ""
        i = 0
        while (i < names.size) {
            links += names[i] + "; "
            i++
        }
        Log.i(APP_TAG, "Links from settings load and sorted: $links")
        return links
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.app_bar_shuffle -> {
                if (appState.shuffleState == ShuffleState.Shuffle) {
                    appState.shuffleState = ShuffleState.NoShuffle
                    item.icon = resources.getDrawable(R.drawable.noshuffle)
                    toast("Shuffle off")
                } else {
                    appState.shuffleState = ShuffleState.Shuffle
                    item.icon = resources.getDrawable(R.drawable.yesshuffle)
                    toast("Shuffle on")
                }
            }
            R.id.app_bar_repeat -> {
                when (appState.repeatState) {
                    RepeatState.All -> {
                        appState.repeatState = RepeatState.One
                        item.icon = resources.getDrawable(R.drawable.repeatone)
                        toast(getString(R.string.repeat_one))
                    }
                    RepeatState.One -> {
                        appState.repeatState = RepeatState.Stop
                        item.icon = resources.getDrawable(R.drawable.repeatstop)
                        toast(getString(R.string.repeat_stop))
                    }
                    RepeatState.Stop -> {
                        appState.repeatState = RepeatState.All
                        item.icon = resources.getDrawable(R.drawable.repeatall)
                        toast(getString(R.string.repeat_all))
                    }
                }
                toast(getString(R.string.search_clicked))
            }
            // R.id.app_bar_settings -> toast(getString(R.string.settings_clicked))
            android.R.id.home -> {
                val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
            }
        }
        return true
    }

    override fun onDestroy() {
        SongDataBase.destroyInstance()
        mDbSongsThread.quit()
        super.onDestroy()
    }
}
