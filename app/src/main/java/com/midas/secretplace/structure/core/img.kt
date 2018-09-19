package com.midas.secretplace.structure.core

import com.google.firebase.database.Exclude
import java.io.Serializable

class img:Serializable
{
    var img_list:ArrayList<String>? = null

    //필수..
    constructor()
    {

    }

    constructor(img_list:ArrayList<String>)
    {
        this.img_list = img_list
    }
}