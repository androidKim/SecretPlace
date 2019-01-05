package com.midas.secretplace.structure.core

import java.io.Serializable


class couple: Serializable
{
    companion object {
        val APPCET_Y: String = "Y"
        val APPCET_N: String = "N"
    }

    var requester_key:String? = ""
    var responser_key:String? = ""
    var accept:String? = ""

    constructor()
    {

    }

    constructor(requester_key:String, responser_key:String, accept:String)
    {
        this.requester_key = requester_key
        this.responser_key = responser_key
        this.accept = accept
    }
}