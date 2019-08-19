package com.midas.secretplace.structure.room
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface dao_place {
    @Insert
    fun insert(pInfo: data_place)

    @Update
    fun update(pInfo: data_place)

    @Query("DELETE FROM tb_place")
    fun deleteAll()

    @Query("SELECT * FROM tb_place")
    fun selectAll(): LiveData<List<data_place>>
}