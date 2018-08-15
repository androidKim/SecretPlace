package com.midas.secretplace.structure.core

import java.io.Serializable

class photo:Serializable
{
    var img_url:String? = null


    constructor()
    {

    }

    constructor(img_url:String)
    {
        this.img_url = img_url
    }
}