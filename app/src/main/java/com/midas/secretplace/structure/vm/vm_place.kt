package com.midas.secretplace.structure.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.midas.secretplace.structure.repository.PlaceRepository
import com.midas.secretplace.structure.repository.UserRepository
import com.midas.secretplace.structure.room.data_place
import com.midas.secretplace.structure.room.data_user


class vm_place (application: Application) : AndroidViewModel(application) {

    var placeRepository: PlaceRepository? = null
    var placeList: MutableLiveData<ArrayList<data_user>>? = null

    init {
        placeRepository = PlaceRepository(application)
        placeList = placeRepository!!.getPlaceList()
    }

    fun insert(pInfo: data_place) {
        placeRepository!!.insert(pInfo)
    }

    fun update(pInfo: data_place) {
        placeRepository!!.update(pInfo)
    }

    fun select() {
        placeRepository!!.getPlaceList()
    }
}