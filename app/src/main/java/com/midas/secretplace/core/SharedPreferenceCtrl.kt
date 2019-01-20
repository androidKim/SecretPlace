package com.midas.secretplace.core

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceCtrl
{
    /************************* Defeine *************************/
    private val SP_THEME = "SP_THEME"
    private val SP_USER_KEY = "SP_USER_KEY"
    private val SP_USER_SNS_TYPE = "SP_USER_SNS_TYPE"

    private lateinit var preference: SharedPreferences

    public constructor()
    {

    }

    public fun init(pContext:Context)
    {
        preference = pContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }
    //---------------------------------------------------------
    //
    public fun clearData()
    {
        val editor = preference.edit()
        editor.clear()
        editor.commit()
    }
    //---------------------------------------------------------
    //
    private fun setStrSaveData(key: String, value: String)
    {
        val editor = preference.edit()
        editor.putString(key, value)
        editor.commit()
    }
    //---------------------------------------------------------
    //
    private fun getStrLoadData(key:String) : String?
    {
        var strResult:String ?= ""
        strResult = preference.getString(key, "")
        return strResult
    }
    /************************* User Function *************************/
    //---------------------------------------------------------
    //
    fun setSpTheme(value:String)
    {
        if(value == null)
            return

        setStrSaveData(SP_THEME, value)
    }
    fun getSpTheme():String?
    {
        var strResult:String?=null
        strResult = getStrLoadData(SP_THEME)
        return strResult
    }
    //---------------------------------------------------------
    //
    fun setSpUserKey(value:String)
    {
        if(value == null)
            return

        setStrSaveData(SP_USER_KEY, value)
    }
    fun getSpUserKey():String?
    {
        var strResult:String?=null
        strResult = getStrLoadData(SP_USER_KEY)
        return strResult
    }
    //---------------------------------------------------------
    //
    fun setSnsType(value:String)
    {
        if(value == null)
            return

        setStrSaveData(SP_USER_SNS_TYPE, value)
    }
    fun getSnsType():String?
    {
        var strResult:String?=null
        strResult = getStrLoadData(SP_USER_SNS_TYPE)
        return strResult
    }
}