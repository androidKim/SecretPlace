package com.midas.secretplace.ui.structure.core

class place
{
    var name:String? = null
    var lat:String? = null
    var lng:String? = null


    fun place(name:String, lat:String, lng:String)
    {
        this.name = name
        this.lat = lat
        this.lng = lng
    }
}