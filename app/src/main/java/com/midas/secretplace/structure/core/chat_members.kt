package com.midas.secretplace.structure.core

import java.io.Serializable


/*
채팅메세지 모델
 */
class chat_members: Serializable
{
    companion object {

    }
    var chat_key:String? = ""//채팅룸 시퀀스
    var members:ArrayList<chat_member>? = ArrayList()

    constructor()
    {

    }

    constructor(chat_key:String, members:ArrayList<chat_member>)
    {
        this.chat_key = chat_key
        this.members = members

    }
}