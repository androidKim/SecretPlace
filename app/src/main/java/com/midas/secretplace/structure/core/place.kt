package com.midas.secretplace.structure.core

import java.io.Serializable

class place:Serializable
{
    var user_key:String? = ""
    var place_key:String? = ""
    var group_key:String? = ""
    var name:String? = ""
    var lat:String? = ""//위도
    var lng:String? = ""//경도
    var memo:String? = ""
    var address:String? = ""
    var img_url:String? = ""//최근에 등록한 이미지 1개만 저장됨..
    var favorite:String? = ""//YN
    //필수..
    constructor()
    {

    }

    constructor(user_key:String, place_key:String,group_key:String, name:String, lat:String, lng:String, memo:String, address:String, img_url:String, favorite:String)
    {
        this.user_key = user_key
        this.place_key = place_key
        this.group_key = group_key
        this.name = name
        this.lat = lat
        this.lng = lng
        this.memo = memo
        this.address = address
        this.img_url = img_url
        this.favorite = favorite
    }


}