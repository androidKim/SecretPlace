package com.midas.secretplace.structure.core

class distance
{
    var user_fk:String? = null
    var name:String? = null
    var location_list:ArrayList<location_info>? = null

    constructor()
    {

    }

    constructor(user_fk:String, name:String, location_list:ArrayList<location_info>)
    {
        this.user_fk = user_fk
        this.name = name
        this.location_list = location_list
    }
}