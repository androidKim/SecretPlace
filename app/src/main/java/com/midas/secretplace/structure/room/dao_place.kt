package com.midas.secretplace.structure.room

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

@Dao
interface dao_place {
    @Insert
    fun insert(pInfo: data_place)

    @Update
    fun update(pInfo: data_place)

    @Query("DELETE FROM tb_place")
    fun deleteAll()

    @Query("SELECT * FROM tb_place")
    fun getPlaceList(): MutableLiveData<ArrayList<data_place>>
}