package com.midas.secretplace.structure.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.os.AsyncTask
import com.midas.secretplace.core.MyRoomDatabase
import com.midas.secretplace.structure.room.dao_place
import com.midas.secretplace.structure.room.dao_user
import com.midas.secretplace.structure.room.data_place
import com.midas.secretplace.structure.room.data_user

class PlaceRepository(application: Application)
{
    private var placeDao: dao_place? = null
    private var placeList: LiveData<List<data_place>>? = null


    init {
        val placeRoomDatabase = MyRoomDatabase.getDatabase(application)
        placeDao = placeRoomDatabase?.placeDao()!!
        placeList = placeDao?.selectAll()
    }


    fun selectAll():LiveData<List<data_place>> {
        return placeList!!
    }

    fun deleteAll()
    {
        deleteAllAsyncTask(placeDao!!).execute()
    }

    fun insert(pInfo: data_place) {
        insertAsyncTask(placeDao!!).execute(pInfo)
    }

    fun update(pInfo: data_place) {
        updateAsyncTask(placeDao!!).execute(pInfo)
    }


    private class insertAsyncTask internal constructor(private val mAsyncTaskDao: dao_place) : AsyncTask<data_place, Void, Void>() {

        override fun doInBackground(vararg params: data_place): Void? {
            mAsyncTaskDao.insert(params[0])
            return null
        }
    }

    private class updateAsyncTask internal constructor(private val mAsyncTaskDao: dao_place) : AsyncTask<data_place, Void, Void>() {

        override fun doInBackground(vararg params: data_place): Void? {
            mAsyncTaskDao.update(params[0])
            return null
        }
    }

    private class deleteAllAsyncTask internal constructor(private val mAsyncTaskDao: dao_place) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            mAsyncTaskDao.deleteAll()
            return null
        }
    }

}