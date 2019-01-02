package com.midas.secretplace.ui.act

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.couple
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_couple.*

class ActCouple : AppCompatActivity()
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null

    var m_pCoupleDbRef: DatabaseReference? = null
    /*********************** xwSystem Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActCouple)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_couple)

    }

    override fun onStart()
    {
        super.onStart()

        m_pCoupleDbRef =  m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)
        m_pCoupleDbRef!!.addListenerForSingleValueEvent(coupleTableRefListener)
    }

    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //요청하기 버튼 이벤트..
    fun coupleRequestProc(view: View)
    {
        var pInfo:couple = couple(m_App!!.m_SpCtrl!!.getSpUserKey()!! , edit_UserKey.text.toString(), false)
        m_pCoupleDbRef!!.push()!!.setValue(pInfo!!)//insert
    }

    /*********************** Firebase DB EventListener ***********************/
    //--------------------------------------------------------------
    //
    var coupleTableRefListener:ValueEventListener = object : ValueEventListener
    {
        override fun onDataChange(dataSnapshot: DataSnapshot)
        {
            // Get Post object and use the values to update the UI
            if(dataSnapshot!!.exists())
            {

            }
        }

        override fun onCancelled(databaseError: DatabaseError)
        {
            // Getting Post failed, log a message

            // ...
            if(databaseError != null)
            {

            }
        }
    }
}
