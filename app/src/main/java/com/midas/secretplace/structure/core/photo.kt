package com.midas.secretplace.structure.core

import com.google.firebase.database.Exclude
import java.io.Serializable

class photo:Serializable
{
    var img_url:String? = null

    @set:Exclude @get:Exclude var isHeader:Boolean = false

    //필수..
    constructor()
    {

    }

    constructor(img_url:String)
    {
        this.img_url = img_url
    }
}