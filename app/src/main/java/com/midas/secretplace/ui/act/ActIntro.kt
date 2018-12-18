package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.midas.secretplace.ui.MyApp

class ActIntro : AppCompatActivity()
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null
    /*********************** xwSystem Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActIntro)
        var strUserKey:String? = m_App!!.m_SpCtrl!!.getSpUserKey()
        if(strUserKey != null && !strUserKey.equals(""))
        {
            var pIntent = Intent(m_Context, ActMain::class.java)
            startActivity(pIntent)
            finish()
        }
        else
        {
            var pIntent = Intent(m_Context, ActLogin::class.java)
            startActivity(pIntent)
            finish()
        }


    }
    /*********************** User Function ***********************/
}
