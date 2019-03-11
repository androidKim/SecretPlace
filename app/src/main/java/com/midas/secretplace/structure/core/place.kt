package com.midas.secretplace.structure.core

import java.io.Serializable

class place:Serializable
{
    var user_key:String? = ""
    var place_key:String? = ""
    var group_key:String? = ""
    var name:String? = ""
    var lat:String? = ""
    var lng:String? = ""
    var memo:String? = ""
    var address:String? = ""
    //필수..
    constructor()
    {

    }

    constructor(user_key:String, place_key:String,group_key:String, name:String, lat:String, lng:String, memo:String, address:String)
    {
        this.user_key = user_key
        this.place_key = place_key
        this.group_key = group_key
        this.name = name
        this.lat = lat
        this.lng = lng
        this.memo = memo
        this.address = address
    }
}