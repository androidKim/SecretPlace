package com.midas.secretplace.ui.act

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.util.Log
import android.view.View
import com.google.firebase.database.*
import com.midas.mytimeline.ui.adapter.RequestForMeRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.couple
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.custom.SimpleDividerItemDecoration
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_request_for_me.*
import java.util.*

/*
나에게 온 요청 보기
 */

class ActRequestForMe : AppCompatActivity(), RequestForMeRvAdapter.ifCallback
{
    /*********************** Interface Callback ***********************/
    override fun serRequestOnOffProc(pInfo:couple)
    {
        if(pInfo!!.accept.equals(couple.APPCET_N))
            pInfo!!.accept = couple.APPCET_Y
        else
            pInfo!!.accept = couple.APPCET_N

        var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)
        pDbRef!!.addListenerForSingleValueEvent(valueEventListener)
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

        settingView()
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        //나에게 온 요청
        var pQuery: Query? = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!.orderByChild("responser_key").equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())
        pQuery!!.addChildEventListener(coupleTableChildEventListener)
        super.onStart()
    }

    /*********************** Firebase DB EventListener ***********************/
    //--------------------------------------------------------------
    //childEventListener..
    var coupleTableChildEventListener: ChildEventListener = object : ChildEventListener
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

    //--------------------------------------------------------------
    //
    var valueEventListener:ValueEventListener = object : ValueEventListener
    {
        override fun onDataChange(dataSnapshot: DataSnapshot)
        {
            // Get Post object and use the values to update the UI
            if(dataSnapshot!!.exists())
            {
                val children = dataSnapshot!!.children
                children.forEach {
                    val pInfo:couple = it!!.getValue(couple::class.java)!!
                    if(pInfo.responser_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))
                    {
                        //update accept value
                        if(pInfo.accept.equals(couple.APPCET_Y))
                            pInfo.accept = couple.APPCET_N
                        else
                            pInfo.accept = couple.APPCET_Y

                        m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!.child(it.key).setValue(pInfo)
                    }
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError)
        {
            // Getting Post failed, log a message

            // ...
        }
    }

    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun settingView()
    {
        m_Adapter = RequestForMeRvAdapter(m_Context!!, m_arrRequest!!, this)
        recyclerView!!.adapter = m_Adapter!!

        recyclerView!!.addItemDecoration(SimpleDividerItemDecoration(20))

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
                    // Call your API to load more items
                    //if(!m_bPagingFinish)
                    //getPlaceListProc(m_strPlaceLastSeq!!)
                }
            }
        })
    }

}
