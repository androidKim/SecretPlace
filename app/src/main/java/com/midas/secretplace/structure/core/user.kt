package com.midas.secretplace.structure.core

import com.google.firebase.database.Exclude


class user
{
    companion object
    {
        val JOIN_TYPE_GOOGLE:String = "G"
        val JOIN_TYPE_KAKAO:String = "K"
        val JOIN_TYPE_FACEBOOK:String = "F"
        val JOIN_TYPE_TWITTER:String = "T"
        val JOIN_TYPE_NAVER:String = "N"
        val JOIN_TYPE_INSTARGRAM:String = "I"
    }

    var join_type:String? = null
    var key:String? = null
    var name:String? = null
    var img_url:String? = null

    constructor()
    {

    }
    constructor(join_type:String, key:String, name:String, img_url:String)
    {
        this.join_type = join_type
        this.key = key
        this.name = name
        this.img_url = img_url
    }

    @Exclude
    fun toMap(): Map<String, Any>
    {
        val result:HashMap<String, Any> = HashMap()
        result.put("join_type", join_type!!)
        result.put("key", key!!)
        result.put("name", name!!)
        result.put("img_url", img_url!!)
        return result
    }
}