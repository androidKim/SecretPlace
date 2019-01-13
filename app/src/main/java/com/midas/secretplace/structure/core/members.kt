package com.midas.secretplace.structure.core

import java.io.Serializable


/*
채팅 참여자
 */
class members: Serializable
{
    companion object {
        val STATUS_TYPE_IN:String? = "Y"
        val STATUS_TYPE_OUT:String? = "N"
    }
    var user_key:String? = ""//유저  시퀀스
    var status:String? = ""//Y,N 입장 여부

    constructor()
    {

    }

    constructor(user_key:String, status:String)
    {
        this.user_key = user_key
        this.status = status

    }
}