package com.midas.secretplace.structure.core

import java.io.Serializable


/*
채팅메세지 모델
 */
class chat_member: Serializable
{
    companion object {
        val STATUS_TYPE_IN:String? = "Y"
        val STATUS_TYPE_OUT:String? = "N"
    }
    var user_key:String? = ""//유저  시퀀스
    var name:String? = ""
    var status:String? = ""//Y,N 입장 여부

    constructor()
    {

    }

    constructor(user_key:String, name:String, status:String)
    {
        this.user_key = user_key
        this.name = name
        this.status = status

    }
}