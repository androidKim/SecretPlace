package com.midas.secretplace.structure.core

import java.io.Serializable

class place:Serializable
{
    var user_fk:String? = null
    var name:String? = null
    var lat:String? = null
    var lng:String? = null

    constructor() {

    }

    constructor(user_fk:String, name:String, lat:String, lng:String)
    {
        this.user_fk = user_fk
        this.name = name
        this.lat = lat
        this.lng = lng
    }
}