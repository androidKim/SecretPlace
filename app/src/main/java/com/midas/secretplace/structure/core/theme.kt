package com.midas.secretplace.structure.core

import java.io.Serializable

class theme:Serializable
{
    var colorName:String = ""


    //필수..
    constructor()
    {

    }

    constructor(colorName:String)
    {
        this.colorName = colorName
    }
}