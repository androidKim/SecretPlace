package com.midas.secretplace.structure.core

import com.google.firebase.database.Exclude
import java.io.Serializable

class place:Serializable
{
    var seq:String? = null
    var user_fk:String? = null
    var name:String? = null
    var lat:String? = null
    var lng:String? = null
    @get:Exclude var img_list:ArrayList<photo>? = null//

    //필수..
    constructor()
    {

    }

    constructor(seq:String, user_fk:String, name:String, lat:String, lng:String, img_list:ArrayList<photo>)
    {
        this.seq = seq
        this.user_fk = user_fk
        this.name = name
        this.lat = lat
        this.lng = lng
        this.img_list = img_list
    }
}