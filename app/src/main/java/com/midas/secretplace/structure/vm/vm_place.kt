package com.midas.secretplace.structure.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.midas.secretplace.structure.repository.PlaceRepository
import com.midas.secretplace.structure.repository.UserRepository
import com.midas.secretplace.structure.room.data_place
import com.midas.secretplace.structure.room.data_user


class vm_place (application: Application) : AndroidViewModel(application) {

    var placeRepository: PlaceRepository? = null
    var placeList: LiveData<List<data_place>>? = null

    init {
        placeRepository = PlaceRepository(application)
        placeList = placeRepository!!.selectAll()
    }

    fun insert(pInfo: data_place) {
        placeRepository!!.insert(pInfo)
    }

    fun update(pInfo: data_place) {
        placeRepository!!.update(pInfo)
    }

    fun deleteAll(){
        placeRepository!!.deleteAll()
    }

    fun select() {
        placeRepository!!.selectAll()
    }
}