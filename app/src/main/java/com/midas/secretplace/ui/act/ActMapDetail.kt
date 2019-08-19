package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.frag.MapFragment
import com.midas.secretplace.util.Util

class ActMapDetail : AppCompatActivity()
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp = MyApp()
    var m_Context: Context = this
    var m_PlaceInfo:place = place()
    var m_arrPlace:ArrayList<place> = ArrayList()
    /*********************** xwSystem Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActMapDetail)
        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_map_detail)

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

        if(pIntent.hasExtra(Constant.INTENT_DATA_PLACE_LIST_OBJECT))
            m_arrPlace = pIntent.extras.get(Constant.INTENT_DATA_PLACE_LIST_OBJECT) as ArrayList<place>
    }
    //--------------------------------------------------------------
    //
    fun initLayout()
    {
        if(m_arrPlace!!.size > 0)//리스트
        {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
            mapFragment!!.getMapAsync(mapFragment)
            val mArgs = Bundle()
            mArgs.putSerializable(Constant.INTENT_DATA_PLACE_LIST_OBJECT, m_arrPlace!!)
            mapFragment.arguments = mArgs
        }
        else if(m_PlaceInfo != null)//1개장소
        {
            //map..
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
            mapFragment!!.getMapAsync(mapFragment)
            val mArgs = Bundle()
            mArgs.putSerializable(Constant.INTENT_DATA_PLACE_OBJECT, m_PlaceInfo!!)
            mapFragment.arguments = mArgs
        }
        else
        {
            return
        }
    }
}
