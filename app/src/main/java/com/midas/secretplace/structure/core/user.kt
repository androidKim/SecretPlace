package com.midas.secretplace.structure.core

import com.google.firebase.database.Exclude


class user
{
    companion object
    {
        val JOIN_TYPE_ANONY:String = "N"
        val JOIN_TYPE_GOOGLE:String = "G"
        val JOIN_TYPE_KAKAO:String = "K"
        val JOIN_TYPE_FACEBOOK:String = "F"
        val JOIN_TYPE_TWITTER:String = "T"
        val JOIN_TYPE_NAVER:String = "N"
        val JOIN_TYPE_INSTARGRAM:String = "I"
    }

    var sns_type:String? = ""
    var sns_key:String? = ""
    var user_key:String? = ""
    var name:String? = ""
    var img_url:String? = ""

    constructor()
    {

    }
    constructor(sns_type:String, key:String, user_key:String ,name:String, img_url:String)
    {
        this.sns_type = sns_type
        this.sns_key = key
        this.user_key = user_key
        this.name = name
        this.img_url = img_url
    }
}