package com.midas.secretplace.structure.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import com.midas.secretplace.core.MyRoomDatabase
import com.midas.secretplace.structure.room.dao_user
import com.midas.secretplace.structure.room.data_user

class UserRepository(application: Application)
{
    var userDao: dao_user? = null
    var userInfo: LiveData<data_user>? = null



    init {
        val userRoomDatabase = MyRoomDatabase.getDatabase(application)
        userDao = userRoomDatabase?.userDao()!!
        userInfo = userDao?.getUserInfo()
    }


    fun getUserData(): LiveData<data_user> {
        return userInfo!!
    }

    fun insert(pInfo: data_user) {
        insertAsyncTask(userDao!!).execute(pInfo)
    }

    fun update(pInfo: data_user) {
        updateAsyncTask(userDao!!).execute(pInfo)
    }


    private class insertAsyncTask internal constructor(private val mAsyncTaskDao: dao_user) : AsyncTask<data_user, Void, Void>() {

        override fun doInBackground(vararg params: data_user): Void? {
            mAsyncTaskDao.insert(params[0])
            return null
        }
    }

    private class updateAsyncTask internal constructor(private val mAsyncTaskDao: dao_user) : AsyncTask<data_user, Void, Void>() {

        override fun doInBackground(vararg params: data_user): Void? {
            mAsyncTaskDao.update(params[0])
            return null
        }
    }

}