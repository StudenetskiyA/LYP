package com.example.lyp

import android.content.Context
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Delete
import androidx.room.Update

const val TABLE_NAME = "songData"
const val NAME_COLUMN = "name"

@Entity(tableName = "$TABLE_NAME")
data class SongData(@PrimaryKey(autoGenerate = true) var id: Long?,
                    @ColumnInfo(name = "$NAME_COLUMN") var name: String = "",
                    @ColumnInfo(name = "path") var path: String = "",
                    @ColumnInfo(name = "tags") var tags: String = "",
                    @ColumnInfo(name = "date") var date: String = "",//May be not string?
                    @ColumnInfo(name = "count") var count: Int = 0,
                    @ColumnInfo(name = "rating") var rating: Int = 0,
                    @ColumnInfo(name = "duration") var duraton: Long = 0
){
    constructor():this(0,"","","","",0,0,0)
}

@Dao
interface SongDataDao {
    @Query("SELECT * from songData")
    fun getAll(): List<SongData>

    @Query("SELECT * from songData WHERE $NAME_COLUMN LIKE :songName LIMIT 1")
    fun findByName(songName: String): SongData

    @Insert(onConflict = REPLACE)
    fun insert(songData: SongData)

    @Query("DELETE from $TABLE_NAME")
    fun deleteAll()

    @Update
    fun update(songData: SongData)

    @Delete
    fun delete(songData: SongData)
}

@Database(entities = [SongData::class], version = 1)
abstract class SongDataBase : RoomDatabase() {

    abstract fun songDataDao(): SongDataDao

    companion object {
        private var INSTANCE: SongDataBase? = null

        fun getInstance(context: Context): SongDataBase? {
            if (INSTANCE == null) {
                synchronized(SongDataBase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            SongDataBase::class.java, "songs.db")
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}