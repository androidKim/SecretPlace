package com.midas.secretplace.structure.core

import com.google.firebase.database.Exclude
import java.io.Serializable

class group:Serializable
{
    var user_key:String? = null
    var group_key:String? = null
    var name:String? = null
    //필수..
    constructor()
    {

    }

    constructor(user_key:String, group_key:String, name:String)
    {
        this.user_key = user_key
        this.group_key = group_key
        this.name = name
    }
}