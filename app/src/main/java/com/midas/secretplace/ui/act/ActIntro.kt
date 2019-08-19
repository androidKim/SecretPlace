package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
             Intent(m_Context, ActMain::class.java).let{
                 startActivity(it)
                 finish()
            }

        }
        else
        {
            Intent(m_Context, ActLogin::class.java).let {
                startActivity(it)
                finish()
            }

        }
    }
    /*********************** User Function ***********************/
}
