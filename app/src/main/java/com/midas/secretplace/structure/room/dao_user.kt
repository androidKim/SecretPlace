package com.midas.secretplace.structure.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface dao_user {
    @Insert
    fun insert(pInfo: data_user)

    @Query("DELETE FROM tb_user")
    fun deleteAll()

    @Query("SELECT * FROM tb_user LIMIT 1")
    fun getUserInfo(): LiveData<data_user>
}