package com.midas.secretplace.structure.core

import com.google.firebase.database.Exclude


class user
{
    companion object
    {
        val SNS_TYPE_GOOGLE:String = "G"
        val SNS_TYPE_KAKAO:String = "K"
        val SNS_TYPE_FACEBOOK:String = "F"
        val SNS_TYPE_TWITTER:String = "T"
        val SNS_TYPE_NAVER:String = "N"
        val SNS_TYPE_INSTARGRAM:String = "I"
    }

    var sns_type:String? = null
    var sns_key:String? = null
    var user_key:String? = null
    var name:String? = null
    var img_url:String? = null

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