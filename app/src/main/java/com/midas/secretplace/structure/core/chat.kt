package com.midas.secretplace.structure.core

import java.io.Serializable


/*
채팅메세지 모델
 */
class chat: Serializable
{
    companion object {

    }
    var chat_key:String? = ""//채팅룸 시퀀스
    var last_msg:String? = ""//마지막메세지
    var timestamp:Long = 0//생성시간

    constructor()
    {

    }

    constructor(chat_key:String, last_msg:String, timestamp:Long)
    {
        this.chat_key = chat_key
        this.last_msg = last_msg
        this.timestamp = timestamp
    }
}