package com.midas.secretplace.structure.core

class location_info
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