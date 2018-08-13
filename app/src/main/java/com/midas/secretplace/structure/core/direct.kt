package com.midas.secretplace.structure.core

import java.io.Serializable

class direct:Serializable
{
    var seq:String? = null
    var user_fk:String? = null
    var name:String? = null
    var location_list:ArrayList<location_info>? = null

    constructor()
    {

    }

    constructor(seq:String, user_fk:String, name:String, location_list:ArrayList<location_info>)
    {
        this.seq = seq
        this.user_fk = user_fk
        this.name = name
        this.location_list = location_list
    }
}