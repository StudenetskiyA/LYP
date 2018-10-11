package com.example.lyp

import android.content.Context
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Delete
import androidx.room.Update

const val TABLE_NAME = "songData"
const val NAME_COLUMN = "name"
const val PATH_COLUMN = "path" //Path+Name+Extension, ex. C:/folder/1.mp3

@Entity(tableName = "$TABLE_NAME")
data class SongData(@PrimaryKey(autoGenerate = true) var id: Long? = null,
                    @ColumnInfo(name = "$NAME_COLUMN") var name: String = "",
                    @ColumnInfo(name = "$PATH_COLUMN") var path: String = "",
                    @ColumnInfo(name = "tags") var tags: String = "",
                    @ColumnInfo(name = "date") var date: String = "",//May be not string?
                    @ColumnInfo(name = "count") var count: Int = 0,
                    @ColumnInfo(name = "rating") var rating: Int = 0,
                    @ColumnInfo(name = "duration") var duration: Int = 0
){
    //constructor():this(null,"","","","",0,0,0)
    override fun toString():String {
        return "$name,$path,$tags,$date,$duration\n"
    }

}

@Dao
interface SongDataDao {
    @Query("SELECT * from $TABLE_NAME")
    fun getAll(): List<SongData>

    @Query("SELECT * from $TABLE_NAME WHERE $NAME_COLUMN LIKE :songName LIMIT 1")
    fun findByName(songName: String): SongData

    @Query("SELECT COUNT(*) from $TABLE_NAME WHERE $PATH_COLUMN LIKE :path")
    fun isExist(path: String): Int

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