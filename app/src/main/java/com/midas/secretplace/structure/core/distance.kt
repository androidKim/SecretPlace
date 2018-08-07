package com.midas.secretplace.structure.core

import java.io.Serializable

class distance:Serializable
{
    var user_fk:String? = null
    var name:String? = null
    var location_list:ArrayList<location>? = null

    constructor()
    {

    }

    constructor(user_fk:String, name:String, location_list:ArrayList<location>)
    {
        this.user_fk = user_fk
        this.name = name
        this.location_list = location_list
    }
}