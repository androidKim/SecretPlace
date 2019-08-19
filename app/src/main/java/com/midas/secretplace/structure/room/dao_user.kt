package com.midas.secretplace.structure.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface dao_user {
    @Insert
    fun insert(pInfo: data_user)

    @Update
    fun update(pInfo: data_user)

    @Query("DELETE FROM tb_user")
    fun deleteAll()

    @Query("SELECT * FROM tb_user LIMIT 1")
    fun getUserInfo(): LiveData<data_user>
}