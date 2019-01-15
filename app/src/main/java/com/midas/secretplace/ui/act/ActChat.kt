package com.midas.secretplace.ui.act

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.firebase.database.*
import com.midas.mytimeline.ui.adapter.MessageRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.ReqBase
import com.midas.secretplace.structure.core.chat
import com.midas.secretplace.structure.core.couple
import com.midas.secretplace.structure.core.message
import com.midas.secretplace.structure.core.user
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_chat.*

class ActChat : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, MessageRvAdapter.ifCallback
{

    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null

    var m_Adapter:MessageRvAdapter? = null
    var m_arrMessage:ArrayList<message>  = ArrayList<message>()
    var m_CoupleInfo:couple = couple()
    var m_UserInfo:user = user()
    var m_strChayKey:String = ""
    var m_strMessageKey:String = ""
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

        ly_SwipeRefresh.setOnRefreshListener(this)//swife refresh listener

        var pUserQuery:Query = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!.child(m_App!!.m_SpCtrl!!.getSpUserKey())
        pUserQuery.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists())
                {
                    val pInfo: user = p0!!.getValue(user::class.java)!!
                    m_UserInfo = pInfo
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })

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
    /*********************** Firebase Listener ***********************/
    //--------------------------------------------------------------
    //
    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun refreshUi()
    {
        //m_strPlaceLastSeq = ""
        m_arrMessage = ArrayList<message>()
        if(m_Adapter != null)
            m_Adapter!!.clearData()

        ly_SwipeRefresh.isRefreshing = false

        m_strMessageKey = ""
        getMessageList(m_strMessageKey, ReqBase.ITEM_COUNT)
    }
    //--------------------------------------------------------------
    //
    fun settingChatData()
    {
        m_Adapter = MessageRvAdapter(m_Context!!, m_arrMessage!!, this)
        recyclerView!!.adapter = m_Adapter

        var nSpanCnt = 1
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)//landspace mode..
        {
            nSpanCnt = 1
        }

        val pLayoutManager = GridLayoutManager(m_Context, nSpanCnt)
        recyclerView!!.setHasFixedSize(true)

        recyclerView!!.layoutManager = pLayoutManager
        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int)
            {
                val visibleItemCount = pLayoutManager.childCount
                val totalItemCount = pLayoutManager.itemCount
                val firstVisible = pLayoutManager.findFirstVisibleItemPosition()

                if((visibleItemCount + firstVisible) >= totalItemCount)
                {
                    // Call your API to load more items
                    //if(!m_bPagingFinish)
                    //getPlaceListProc(m_strPlaceLastSeq!!)

                    getMessageList(m_strMessageKey, ReqBase.ITEM_COUNT)
                }
            }
        })

        getMessageList(m_strMessageKey, ReqBase.ITEM_COUNT)
    }
    //--------------------------------------------------------------
    //
    fun getMessageList(message_key:String, item_count:Int)
    {
        if(!message_key.equals(""))//paging get
        {
            var messageQuery:Query = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_MESSAGE)!!.equalTo(message_key).limitToLast(item_count)
        }
        else//first get
        {
            var messageQuery:Query = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_MESSAGE)!!.orderByChild("chat_key").equalTo(m_strChayKey).limitToLast(item_count)
            messageQuery!!.addChildEventListener(object:ChildEventListener {
                override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                    if(p0!!.exists())
                    {
                        //save item key
                        m_strMessageKey = p0.key

                        //UI update
                        val pInfo:message = p0!!.getValue(message::class.java)!!
                        m_Adapter!!.addData(pInfo)
                    }
                }

                override fun onChildChanged(p0: DataSnapshot?, p1: String?)
                {
                    if(p0!!.exists())
                    {
                        //refresh
                        refreshUi()
                    }
                }

                override fun onChildRemoved(p0: DataSnapshot?) {
                    if(p0!!.exists())
                    {
                        //refresh
                        refreshUi()
                    }
                }

                override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                    if(p0!!.exists())
                    {

                    }
                }

                override fun onCancelled(p0: DatabaseError?) {

                }
            })
        }
    }

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
                        m_strChayKey = it.key
                        settingChatData()
                    }
                }

                if(m_bExistChat)
                {

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
    // swife refresh
    override fun onRefresh()
    {
        refreshUi()
    }
    //--------------------------------------------------------------
    //send  message
    fun onClickSendMessage(view:View)
    {
        progressBar.visibility = View.VISIBLE
        var pInfo:message = message(m_strChayKey, m_UserInfo.user_key!!, m_UserInfo.name!!, edit_Input.text.toString(), m_UserInfo.img_url!!)
        var MessageDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_MESSAGE)!!
        MessageDbRef!!.push().setValue(pInfo)
        MessageDbRef!!.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists())
                {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
        edit_Input.setText("")
    }
}
