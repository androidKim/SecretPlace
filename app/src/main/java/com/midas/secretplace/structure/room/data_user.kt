package com.midas.secretplace.structure.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_user")
data class data_user (@PrimaryKey(autoGenerate = true) val uid:Int,
                      @ColumnInfo(name = "img_url") val img_url: String,
                      @ColumnInfo(name = "name") val name: String,
                      @ColumnInfo(name = "sns_key") val sns_key: String,
                      @ColumnInfo(name = "sns_type") val sns_type: String,
                      @ColumnInfo(name = "user_key") val user_key: String)