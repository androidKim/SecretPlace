
package com.midas.secretplace.ui.act

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.midas.secretplace.R
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.frag.MapFragment


class ActPlaceDetail : AppCompatActivity()
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    private var m_App: MyApp? = null
    private var m_Context: Context? = null

    /*********************** Controller ***********************/
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_place_detail)
        m_Context = this
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActPlaceDetail)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(mapFragment)
        System.err.println("OnCreate end")

        initValue()
        recvIntentData()
        initLayout()
    }

    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun initValue()
    {

    }
    //--------------------------------------------------------------
    //
    fun recvIntentData()
    {

    }
    //--------------------------------------------------------------
    //
    fun initLayout()
    {
        settingView()
    }
    //--------------------------------------------------------------
    //
    fun settingView()
    {

    }
    /************************* listener *************************/

    /*********************** listener ***********************/
}
