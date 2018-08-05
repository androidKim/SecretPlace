package com.midas.secretplace.structure.core

import java.io.Serializable

class place:Serializable
{
    var name:String? = null
    var lat:String? = null
    var lng:String? = null

    constructor() {

    }

    constructor(name:String, lat:String, lng:String)
    {
        this.name = name
        this.lat = lat
        this.lng = lng
    }
}