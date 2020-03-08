package com.midas.secretplace.ui.frag.main

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp

class FrMap : Fragment()
{
    /**************************** Define ****************************/

    /**************************** Member ****************************/
    var m_Context: Context? = null
    var m_Activity:Activity? = null
    var m_App:MyApp? = null
    var m_RequestManager: RequestManager? = null
    var placeList:ArrayList<place> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_map, container, false)

        m_Context = context
        m_Activity = activity
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context!!)

        m_RequestManager = Glide.with(m_Context)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getPlaceListProc("")
    }

    //--------------------------------------------------------------
    //
    fun getPlaceListProc(categoryCode:String)
    {
        var pQuery: Query? = null
        if(categoryCode.equals("")){
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE).child(m_App!!.m_SpCtrl!!.getSpUserKey()!!).orderByKey()
        }else{
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey()!!).orderByChild("categoryCode").equalTo(categoryCode)
        }

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if(dataSnapshot!!.exists())
                {
                    val children = dataSnapshot!!.children
                    children.forEach {
                        val placeItem:place = it!!.getValue(place::class.java)!!
                        if(placeItem != null){
                            placeList!!.add(placeItem)
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError)
            {

            }
        })
    }
}
