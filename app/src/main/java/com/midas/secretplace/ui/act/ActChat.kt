package com.midas.secretplace.ui.act

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.google.firebase.database.*
import com.midas.mytimeline.ui.adapter.MessageRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.chat
import com.midas.secretplace.structure.core.couple
import com.midas.secretplace.structure.core.message
import com.midas.secretplace.structure.core.user
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_chat.*
import kotlinx.android.synthetic.main.ly_top.*

class ActChat : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, MessageRvAdapter.ifCallback
{

    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null
    var m_Imm:InputMethodManager? = null

    var m_Adapter:MessageRvAdapter? = null
    var m_arrMessage:ArrayList<message>  = ArrayList<message>()
    var m_CoupleInfo:couple = couple()
    var m_UserInfo:user = user()
    var m_strChayKey:String = ""
    var m_strLastKey:String = ""
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

        m_Imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        ly_SwipeRefresh.setOnRefreshListener(this)//swife refresh listener
        tv_Top.text = m_Context!!.resources.getString(R.string.str_msg_48)//TopTitle
        progressBar.visibility = View.VISIBLE

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

        m_strLastKey = ""
        getMessageList()
    }
    //--------------------------------------------------------------
    //
    fun settingChatData()
    {
        m_Adapter = MessageRvAdapter(m_Context!!, m_arrMessage!!, m_App!!.m_SpCtrl!!.getSpUserKey()!!, this)
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

                }
            }
        })

        getMessageList()
    }
    //--------------------------------------------------------------
    //
    fun getMessageList()
    {
        //전체 데이터
        var messageQuery:Query = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_MESSAGE)!!.orderByChild("chat_key").equalTo(m_strChayKey)
        messageQuery!!.addListenerForSingleValueEvent(object:ValueEventListener {

            override fun onDataChange(p0: DataSnapshot?) {
                val children = p0!!.children
                children.forEach {
                    m_strLastKey = it.key//last Key

                    //UI update
                    val pInfo:message = it!!.getValue(message::class.java)!!
                    m_Adapter!!.addData(pInfo)

                }

                progressBar.visibility = View.GONE
                if(m_Adapter!!.itemCount > 0)
                    recyclerView.smoothScrollToPosition(m_Adapter!!.itemCount - 1)

                messageQuery.removeEventListener(this)
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })

        //추가적인 이벤트 발생시..
        messageQuery!!.addChildEventListener(object :ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {

                if(m_strLastKey!!.equals(""))
                    return

                if(p0!!.exists())
                {
                    val pInfo:message = p0!!.getValue(message::class.java)!!
                    m_Adapter!!.addData(pInfo)

                    m_Imm!!.hideSoftInputFromWindow(edit_Input.windowToken, 0)//키보드 숨기기
                    if(m_Adapter!!.itemCount > 0)
                        recyclerView.smoothScrollToPosition(m_Adapter!!.itemCount - 1)//recyclerview position move to last item

                    progressBar.visibility = View.GONE
                }
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                if(p0!!.exists())
                {

                }
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                if(p0!!.exists())
                {

                }
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                if(p0!!.exists())
                {

                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
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
        val timestamp = System.currentTimeMillis() / 1000
        var pInfo:message = message(m_strChayKey, m_UserInfo.user_key!!, m_UserInfo.name!!, edit_Input.text.toString(), m_UserInfo.img_url!!, timestamp)
        var MessageDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_MESSAGE)!!
        MessageDbRef!!.push().setValue(pInfo)
        edit_Input.setText("")
    }
}
