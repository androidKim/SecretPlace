package com.midas.secretplace.ui.act

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.midas.secretplace.R
import com.midas.secretplace.ui.MyApp

class ActChat : AppCompatActivity()
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
        m_App!!.init(m_Context as ActChat)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.act_chat)

    }
    /*********************** User Function ***********************/
}
