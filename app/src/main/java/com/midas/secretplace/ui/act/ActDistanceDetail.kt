
package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.distance
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.frag.MapFragment
import kotlinx.android.synthetic.main.act_distance_detail.*


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

        if(pIntent.hasExtra(Constant.INTENT_DATA_DISTANCE_OBJECT))
            m_DistanceInfo =  pIntent.extras.get(Constant.INTENT_DATA_DISTANCE_OBJECT) as distance
    }
    //--------------------------------------------------------------
    //
    fun initLayout()
    {
        btn_AddLocation.setOnClickListener(View.OnClickListener {
            showAddLocationDialog()
        })

        settingView()
    }
    //--------------------------------------------------------------
    //
    fun settingView()
    {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(mapFragment)
        val mArgs = Bundle()
<<<<<<< HEAD
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
=======

>>>>>>> 4145d4f75eee6bb8ff377a855efd5ff9f7cbef67
        mArgs.putSerializable(Constant.INTENT_DATA_DISTANCE_OBJECT, m_DistanceInfo)
        mapFragment.arguments = mArgs
        System.err.println("OnCreate end")
    }
    //--------------------------------------------------------------
    //
    fun showAddLocationDialog()
    {
        val builder = AlertDialog.Builder(m_Context!!)
        builder.setMessage(getString(R.string.str_msg_6))
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            /*
            var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.updateDistanceLocation(pInfo)
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?)
                {
                    if (dataSnapshot!!.exists())
                    {
                        //refresh Map Marker..
                    }
                }

                override fun onCancelled(p0: DatabaseError?)
                {

                }
            })
            */
        }

        builder.setNegativeButton(getString(R.string.str_no)){dialog,which ->

        }

        builder.setNeutralButton(getString(R.string.str_cancel)){_,_ ->

        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    //--------------------------------------------------------------
    //
    fun updateCurrentLocationProc()
    {

    }

    /************************* listener *************************/

    /*********************** listener ***********************/
}
