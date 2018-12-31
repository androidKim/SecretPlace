package com.midas.secretplace.ui.act

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.midas.secretplace.R
import com.midas.secretplace.ui.MyApp

class ActCouple : AppCompatActivity()
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
        setContentView(R.layout.act_couple)
        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActCouple)

    }
    /*********************** User Function ***********************/
}
