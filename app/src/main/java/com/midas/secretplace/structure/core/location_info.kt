package com.midas.secretplace.structure.core

import java.io.Serializable

class location_info:Serializable
{
    var lat:String? = null
    var lng:String? = null

    constructor() {

    }

    constructor(lat:String, lng:String)
    {
        this.lat = lat
        this.lng = lng
    }
}