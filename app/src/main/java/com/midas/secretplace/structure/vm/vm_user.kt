package com.midas.secretplace.structure.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.midas.secretplace.structure.repository.UserRepository
import com.midas.secretplace.structure.room.data_user


class vm_user (application: Application) : AndroidViewModel(application) {

    var userRepository: UserRepository? = null
    var userInfo: LiveData<data_user>? = null

    init {
        userRepository = UserRepository(application)
        userInfo = userRepository!!.getUserData()
    }

    fun insert(pInfo: data_user) {
        userRepository!!.insert(pInfo)
    }
}