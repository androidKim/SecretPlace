package com.midas.secretplace.structure.core

import java.io.Serializable


/*
채팅메세지 모델
 */
class message: Serializable
{
    companion object {

    }
    var chat_key:String? = ""//채팅룸 시퀀스
    var user_key:String? = ""//유저 시퀀스
    var name:String? = ""//이름
    var msssage:String? = ""//메세지내용
    var img_url:String? = ""//이미지URL
    var timestamp:Long = 0//작성시간

    constructor()
    {

    }

    constructor(chat_key:String, user_key:String, name:String, message:String, img_url:String)
    {
        this.chat_key = chat_key
        this.user_key = user_key
        this.name = name
        this.msssage = message
        this.img_url = img_url
    }
}