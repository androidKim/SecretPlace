package com.midas.secretplace.structure.core

import com.google.firebase.database.Exclude
import java.io.Serializable

class place_list_item:Serializable
{
    var place_info:place? = null
    @get:Exclude var img_list:ArrayList<String>? = null
    //@get:Exclude @set:Exclude var list_item:ArrayList<photo>? = null//

    //필수..
    constructor()
    {

    }

    constructor(place_info:place, img_list:ArrayList<String>)
    {
        this.place_info = place_info
        this.img_list = img_list
    }
}