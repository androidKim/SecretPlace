package com.midas.secretplace.ui.act

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.util.Log
import android.view.View
import com.google.firebase.database.*
import com.midas.mytimeline.ui.adapter.RequestForMeRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.couple
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_request_for_me.*

/*
나에게 온 요청 보기
 */

class ActRequestForMe : AppCompatActivity(), RequestForMeRvAdapter.ifCallback, SwipeRefreshLayout.OnRefreshListener
{
    /*********************** Interface Callback ***********************/
    //-------------------------------------------------------------
    //favorite 상태변경..
    override fun setRequestOnOffProc(pInfo:couple)
    {
        //show dialog..
        val pAlert = AlertDialog.Builder(this@ActRequestForMe).create()
        pAlert.setTitle(m_Context!!.resources.getString(R.string.str_msg_62))
        pAlert.setMessage(m_Context!!.resources.getString(R.string.str_msg_63))
        pAlert.setButton(AlertDialog.BUTTON_POSITIVE, m_Context!!.resources.getString(R.string.str_ok),{
            dialogInterface, i ->

            var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)
            pDbRef!!.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    // Get Post object and use the values to update the UI
                    if(dataSnapshot!!.exists())
                    {
                        val children = dataSnapshot!!.children
                        children.forEach {
                            val targetInfo:couple = it!!.getValue(couple::class.java)!!
                            if(pInfo.responser_key.equals(targetInfo.responser_key)
                                    && pInfo.requester_key.equals(targetInfo.requester_key))//선택한 아이템만 변경..
                            {
                                //update accept value
                                if(pInfo.accept.equals(couple.APPCET_Y))
                                    pInfo.accept = couple.APPCET_N
                                else
                                    pInfo.accept = couple.APPCET_Y

                                m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!.child(it.key).setValue(pInfo)
                            }
                            else//선택되지않은아이템..
                            {
                                if(targetInfo.accept.equals(couple.APPCET_Y))//커플설정이 된 아이템이 있으면 N으로 변경
                                {
                                    targetInfo.accept = couple.APPCET_N
                                    m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!.child(it.key).setValue(targetInfo)
                                }
                            }
                        }
                        //ui refresh..
                        setRefresh()
                        m_Adapter!!.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError)
                {
                    // Getting Post failed, log a message

                    // ...
                }
            })

            pAlert.dismiss()
        })
        pAlert.setButton(AlertDialog.BUTTON_NEGATIVE, m_Context!!.resources.getString(R.string.str_no),{
            dialogInterface, i ->
            pAlert.dismiss()
        })
        pAlert.show()
    }

    //-------------------------------------------------------------
    //
    override fun onRefresh()
    {
        setRefresh()
    }

    /*********************** extentios function ***********************/
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null
    var m_RequestDbRef:DatabaseReference? = null//firebase databse

    var m_Adapter: RequestForMeRvAdapter? = null
    var m_arrRequest: ArrayList<couple>? =  ArrayList<couple>()
    var m_bRunning:Boolean? = false
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActRequestForMe)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_request_for_me)
        ly_SwipeRefresh.setOnRefreshListener(this)
        settingView()
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        super.onStart()
    }

    override fun onBackPressed() {
        setResult(Constant.FOR_RESULT_REQUEST_FOR_ME)
        finish()
    }

    /*********************** Firebase DB EventListener ***********************/
    //--------------------------------------------------------------
    //childEventListener..
    var childEventListener: ChildEventListener = object : ChildEventListener
    {
        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            if (dataSnapshot!!.exists())//exist..
            {
                val pInfo:couple = dataSnapshot!!.getValue(couple::class.java)!!
                //update UI

                m_Adapter!!.addData(pInfo)
                ly_SwipeRefresh.visibility  = View.VISIBLE
                ly_NoData.visibility = View.GONE
            }
            else
            {

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
    //--------------------------------------------------------------
    //
    fun settingView()
    {
        toolbar.title = m_Context!!.resources.getString(R.string.str_msg_60)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        var strTheme:String = m_App!!.m_SpCtrl!!.getSpTheme()!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setToolbarBackgroundColor(m_Context!!, this.toolbar, strTheme!!)
        }


        m_Adapter = RequestForMeRvAdapter(m_Context!!, m_arrRequest!!, this)
        recyclerView!!.adapter = m_Adapter!!

        var nSpanCnt = 1
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)//landspace mode..
        {
            nSpanCnt = 3
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

                if(!m_bRunning!! && (visibleItemCount + firstVisible) >= totalItemCount)
                {

                }
            }
        })

        //getData
        var pQuery: Query? = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!.orderByChild("responser_key").equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())
        pQuery!!.addChildEventListener(childEventListener)
    }

    //-------------------------------------------------------------
    //
    fun setRefresh()
    {
        if(m_Adapter != null)
            m_Adapter!!.clearData()

        m_arrRequest = ArrayList()
        ly_SwipeRefresh.isRefreshing = false
        settingView()
    }
}
