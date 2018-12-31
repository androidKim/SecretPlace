package com.midas.secretplace.structure.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "tb_user")
data class data_user (@PrimaryKey
                      @ColumnInfo(name = "img_url") val img_url: String,
                      @ColumnInfo(name = "name") val name: String,
                      @ColumnInfo(name = "sns_key") val sns_key: String,
                      @ColumnInfo(name = "sns_type") val sns_type: String)