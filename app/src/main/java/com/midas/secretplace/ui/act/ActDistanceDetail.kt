
package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.distance
import com.midas.secretplace.structure.core.location_info
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.frag.MapFragment
import kotlinx.android.synthetic.main.act_distance_detail.*


class ActDistanceDetail : ActBase()
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
            if(m_DistanceInfo != null)
            {

                var locationInfo = getLocation()

                //first LatLng
                var pLocationInfo: location_info = location_info(String.format("%s",locationInfo.latitude), String.format("%s",locationInfo.longitude))
                if(m_DistanceInfo!!.location_list == null)
                {
                    m_DistanceInfo!!.location_list = ArrayList<location_info>()
                    m_DistanceInfo!!.location_list!!.add(pLocationInfo)
                }
                else
                {
                    m_DistanceInfo!!.location_list!!.add(pLocationInfo)
                }

                var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.updateDistanceLocation()
                pDbRef.child(m_DistanceInfo!!.seq).setValue(m_DistanceInfo!!)
                pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot?)
                    {
                        if (dataSnapshot!!.exists())
                        {
                            //refresh Map Marker..
                            val pInfo: distance = dataSnapshot.getValue(distance::class.java)!!
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?)
                    {

                    }
                })
            }
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
    /*********************** listener ***********************/


    /*********************** interface ***********************/
}
