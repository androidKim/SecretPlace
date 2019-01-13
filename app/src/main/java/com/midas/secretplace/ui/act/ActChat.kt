package com.midas.secretplace.ui.act

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.chat
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util

class ActChat : AppCompatActivity()
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null
    var m_pChatDbRef: DatabaseReference? = null
    var m_pChatMemberDbRef: DatabaseReference? = null
    var m_pChatMessageDbRef: DatabaseReference? = null
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActChat)
        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_chat)
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        super.onStart()
        m_pChatDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_CHAT)!!
        m_pChatMemberDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_CHAT_MEMBER)!!
        m_pChatMessageDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_CHAT_MESSAGE)!!
        m_pChatDbRef!!.addChildEventListener(chatTableChildEventListener)
        //m_pChatMemberDbRef!!.addChildEventListener(chatMemberTableChildEventListener)
        //m_pChatMessageDbRef!!.addChildEventListener(chatMessageTableChildEventListener)
    }

    /*********************** Firebase DB EventListener ***********************/
    //--------------------------------------------------------------
    //
    var chatTableChildEventListener: ChildEventListener = object : ChildEventListener
    {
        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            // Get Post object and use the values to update the UI
            if(dataSnapshot!!.exists())//exist chat table data
            {
                val pInfo: chat = dataSnapshot!!.getValue(chat::class.java)!!
                //find my id..



            }
            else//no exist chat table data
            {
                //create chat table data


            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            Log.d("onChildChanged", "")
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?)
        {
            Log.d("onChildRemoved", "")
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            Log.d("onChildMoved", "")
        }

        override fun onCancelled(databaseError: DatabaseError?)
        {
            Log.d("onCancelled", "")
        }
    }
    //--------------------------------------------------------------
    //
    var chatMemberTableChildEventListener: ChildEventListener = object : ChildEventListener
    {
        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            // Get Post object and use the values to update the UI
            if(dataSnapshot!!.exists())//exist chat table data
            {
                val pInfo: chat = dataSnapshot!!.getValue(chat::class.java)!!
                //find my id..



            }
            else//no exist chat table data
            {
                //create chat table data


            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            Log.d("onChildChanged", "")
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?)
        {
            Log.d("onChildRemoved", "")
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            Log.d("onChildMoved", "")
        }

        override fun onCancelled(databaseError: DatabaseError?)
        {
            Log.d("onCancelled", "")
        }
    }
    /*********************** User Function ***********************/

}
