package com.midas.secretplace.ui

import android.content.Context
import android.content.Intent
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.midas.secretplace.core.SharedPreferenceCtrl
import com.midas.secretplace.ui.act.ActLogin
import com.midas.secretplace.ui.act.ActMain



class MyApp:MultiDexApplication()
{
    var m_Context: Context? = null
    var m_binit:Boolean = false
    var m_SpCtrl:SharedPreferenceCtrl? = null

    override fun attachBaseContext(base: Context)
    {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun init(pContext:Context)
    {
        if(m_binit == false)
        {
            m_Context = pContext;

            //sharedPreference..
            m_SpCtrl = SharedPreferenceCtrl()
            m_SpCtrl!!.init(m_Context!!)

            //localDb.

            //device size

            //packgename

            //etc..

            m_binit = true
        }
    }

    //-----------------------------------------------
    //
    public fun goMain(pContext:Context)
    {
        if(pContext == null)
            return

        var pIntent = Intent(pContext, ActMain::class.java)
        pIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        pIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        pContext.startActivity(pIntent)
    }
    //-----------------------------------------------
    //
    public fun logoutProc(pContext:Context)
    {
        if(pContext == null)
            return

        var pIntent = Intent(pContext, ActLogin::class.java)
        pIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        pIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        pContext.startActivity(pIntent)

        m_SpCtrl?.clearData()
    }
}