package com.midas.secretplace.structure.core


class couple
{
    companion object
    {

    }
    var requester_key:String? = null
    var responser_key:String? = null
    var accept:Boolean? = false

    constructor()
    {

    }
    constructor(requester_key:String, responser_key:String, accept:Boolean)
    {
        this.requester_key = requester_key
        this.responser_key = responser_key
        this.accept = accept
    }
}