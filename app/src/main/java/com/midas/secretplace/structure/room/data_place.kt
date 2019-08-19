package com.midas.secretplace.structure.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_place")
data class data_place (@PrimaryKey(autoGenerate = true) val uid:Int,
                       @ColumnInfo(name = "user_key") val user_key: String,
                       @ColumnInfo(name = "place_key") val place_key: String,
                       @ColumnInfo(name = "group_key") val group_key: String,
                       @ColumnInfo(name = "name") val name: String,
                       @ColumnInfo(name = "lat") val lat: String,
                       @ColumnInfo(name = "lng") val lng: String,
                       @ColumnInfo(name = "memo") val memo: String,
                       @ColumnInfo(name = "address") val address: String,
                       @ColumnInfo(name = "img_url") val img_url: String)