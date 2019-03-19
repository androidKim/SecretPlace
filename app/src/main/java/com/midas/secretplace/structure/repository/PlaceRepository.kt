package com.midas.secretplace.structure.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import com.midas.secretplace.core.MyRoomDatabase
import com.midas.secretplace.structure.room.dao_place
import com.midas.secretplace.structure.room.dao_user
import com.midas.secretplace.structure.room.data_place
import com.midas.secretplace.structure.room.data_user

class PlaceRepository(application: Application)
{
    var placeDao: dao_place? = null
    var placeList: MutableLiveData<ArrayList<data_place>>? = null


    init {
        val placeRoomDatabase = MyRoomDatabase.getDatabase(application)
        placeDao = placeRoomDatabase?.placeDao()!!
        placeList = placeDao?.getPlaceList()
    }


    fun getPlaceList():MutableLiveData<ArrayList<data_place>> {
        return placeList!!
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

}