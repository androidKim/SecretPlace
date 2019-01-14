package com.midas.secretplace.ui.act

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.database.*
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.chat
import com.midas.secretplace.structure.core.couple
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_chat.*

class ActChat : AppCompatActivity()
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null

    var m_CoupleInfo:couple = couple()
    var m_bExistCouple:Boolean = false//커풀 존재유무
    var m_bExistChat:Boolean = false//커플채팅방존재유무
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

        var pCoupleDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!
        pCoupleDbRef.addValueEventListener(object :ValueEventListener
        {
            override fun onDataChange(dataSnapshot:DataSnapshot?) {
                // Get Post object and use the values to update the UI
                val children = dataSnapshot!!.children
                children.forEach {
                    val pInfo: couple = it!!.getValue(couple::class.java)!!
                    if(pInfo.requester_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()) ||
                            pInfo.responser_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))// 요청자또는 응답자가가 나일떄
                    {
                        if(pInfo.accept.equals(couple.APPCET_Y))
                        {
                            m_CoupleInfo = pInfo!!
                            m_bExistCouple = true
                        }

                    }
                }

                if(m_bExistCouple)
                {
                    showChatLayout()
                }
                else
                {
                    showEmptyLayout()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        super.onStart()
    }
    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun showChatLayout()
    {
        ly_Empty.visibility = View.GONE
        ly_Chat.visibility = View.VISIBLE

        var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_CHAT)!!
        pDbRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(dataSnapshot:DataSnapshot?) {
                // Get Post object and use the values to update the UI
                val children = dataSnapshot!!.children
                children.forEach {
                    val pInfo:chat = it!!.getValue(chat::class.java)!!
                    if(pInfo.requester_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()) ||
                            pInfo.responser_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))// 요청자또는 응답자가가 나일떄
                    {
                        m_bExistChat = true
                    }
                }

                if(m_bExistChat)
                {
                    //get message list
                    var pChatDbRef:Query = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_MESSAGE)!!

                }
                else
                {
                    //create chat room  &  get message list
                    var pInfo:chat = chat("", m_CoupleInfo.requester_key!!, m_CoupleInfo.responser_key!!, "", 0)
                    var pChatDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_CHAT)!!
                    pChatDbRef!!.push().setValue(pInfo!!)
                    pChatDbRef!!.addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot?) {
                            if(p0!!.exists())
                            {
                                val children = p0!!.children
                                children.forEach {
                                    val pInfo:chat = it!!.getValue(chat::class.java)!!
                                    if(pInfo.requester_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()) ||
                                            pInfo.responser_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))// 요청자또는 응답자가가 나일떄
                                    {
                                        pInfo.chat_key = it!!.key
                                        pChatDbRef!!.child(it!!.key).setValue(pInfo)//update
                                    }
                                }
                            }
                            else
                            {

                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {

                        }
                    })
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }
    //--------------------------------------------------------------
    //
    fun showEmptyLayout()
    {
        ly_Empty.visibility = View.VISIBLE
        ly_Chat.visibility = View.GONE
    }

    /*********************** Listener ***********************/
    //--------------------------------------------------------------
    //send  message
    fun onClickSendMessage(view:View)
    {

    }
}
