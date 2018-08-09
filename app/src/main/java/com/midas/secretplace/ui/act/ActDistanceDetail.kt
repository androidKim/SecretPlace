
package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.distance
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.frag.MapFragment


class ActDistanceDetail : AppCompatActivity()
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    private var m_App: MyApp? = null
    private var m_Context: Context? = null
    private var m_DistanceInfo:distance? = null
    /*********************** Controller ***********************/
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_distance_detail)
        m_Context = this
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActDistanceDetail)

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
        var pIntent: Intent = intent

        if(pIntent == null)
            return

        if(pIntent.hasExtra(Constant.INTENT_DATA_DISTANCE_OBJECT))
            m_DistanceInfo =  pIntent.extras.get(Constant.INTENT_DATA_DISTANCE_OBJECT) as distance
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
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(mapFragment)
        val mArgs = Bundle()
        //testcode
        /*
        var tempInfo1:location_info = location_info()
        tempInfo1.lat = "37.498278"
        tempInfo1.lng = "127.046151"
        var tempInfo2:location_info = location_info()
        tempInfo2.lat = "37.489096"
        tempInfo2.lng = "127.055813"
        var tempInfo3:location_info = location_info()
        tempInfo3.lat = "37.498992"
        tempInfo3.lng = "127.116259"
        var tempInfo4:location_info = location_info()
        tempInfo4.lat = "37.454722"
        tempInfo4.lng = "127.070987"

        m_DistanceInfo!!.location_list!!.add(tempInfo1)
        m_DistanceInfo!!.location_list!!.add(tempInfo2)
        m_DistanceInfo!!.location_list!!.add(tempInfo3)
        m_DistanceInfo!!.location_list!!.add(tempInfo4)
           */
        mArgs.putSerializable(Constant.INTENT_DATA_DISTANCE_OBJECT, m_DistanceInfo)
        mapFragment.arguments = mArgs
        System.err.println("OnCreate end")
    }
    /************************* listener *************************/

    /*********************** listener ***********************/
}
