package com.midas.secretplace.structure.core

import com.google.firebase.database.Exclude
import java.io.Serializable

class place:Serializable
{
    var user_key:String? = null
    var place_key:String? = null
    var name:String? = null
    var lat:String? = null
    var lng:String? = null
    //필수..
    constructor()
    {

    }

    constructor(user_key:String, place_key:String, name:String, lat:String, lng:String)
    {
        this.user_key = user_key
        this.place_key = place_key
        this.name = name
        this.lat = lat
        this.lng = lng
    }
}