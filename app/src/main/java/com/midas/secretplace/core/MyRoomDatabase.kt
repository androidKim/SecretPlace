package com.midas.secretplace.core

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.midas.secretplace.structure.room.dao_place
import com.midas.secretplace.structure.room.dao_user
import com.midas.secretplace.structure.room.data_user


@Database(entities = [data_user::class], version = 1, exportSchema = false)
abstract class MyRoomDatabase : RoomDatabase() {

    abstract fun userDao(): dao_user
    abstract fun placeDao(): dao_place
    companion object
    {



        //SINGLETON
        var INSTANCE: MyRoomDatabase? = null

        fun getDatabase(context: Context): MyRoomDatabase {
            if (INSTANCE == null) {
                synchronized(MyRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                MyRoomDatabase::class.java, "my_db")
                                .build()
                    }
                }
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}