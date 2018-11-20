package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.frag.MapFragment

class ActMapDetail : AppCompatActivity()
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp = MyApp()
    var m_Context: Context = this
    var m_PlaceInfo:place = place()
    /*********************** xwSystem Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_map_detail)
        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActMapDetail)

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

        if(pIntent.hasExtra(Constant.INTENT_DATA_PLACE_OBJECT))
            m_PlaceInfo =  pIntent.extras.get(Constant.INTENT_DATA_PLACE_OBJECT) as place
    }
    //--------------------------------------------------------------
    //
    fun initLayout()
    {
        //map..
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        mapFragment!!.getMapAsync(mapFragment)
        val mArgs = Bundle()
        mArgs.putSerializable(Constant.INTENT_DATA_PLACE_OBJECT, m_PlaceInfo!!)
        mapFragment.arguments = mArgs
    }
}
