package com.midas.secretplace.ui.frag.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.google.firebase.database.*
import com.midas.mytimeline.ui.adapter.DistanceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.structure.core.distance
import com.midas.secretplace.structure.core.location_info
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.act.ActMain
import kotlinx.android.synthetic.main.frag_distance.*

class FrDistance : Fragment(), SwipeRefreshLayout.OnRefreshListener
{

    /************************** Define **************************/

    /************************** Member **************************/
    var m_Context: Context? = null
    var m_Activity: Activity? = null
    var m_App:MyApp? = null
    var m_IfCallback:ifCallback? = null
    var m_Adapter:DistanceRvAdapter? = null
    var m_arrDistance:ArrayList<distance>? = null
    var m_strSeq:String? = null
    var m_strRunningSeq:String? = null
    var m_bRunning:Boolean = false
    var m_bPagingFinish:Boolean = false
    /************************** Controller **************************/
    var m_RecyclerView: RecyclerView? = null
    var m_btn_SaveDistance:Button? = null
    /************************** System Function **************************/
    //----------------------------------------------------------------------
    //
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.frag_distance, container, false)

        m_Context = context
        m_Activity = activity
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context!!)

        return view
    }
    //------------------------------------------------------------------------
    //
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        m_RecyclerView = view.findViewById(R.id.recyclerView)
        m_btn_SaveDistance = view.findViewById(R.id.btn_SaveDistance)
        initValue()
        setInitLayout()
    }
    //------------------------------------------------------------------------
    //
    override fun onAttach(pContext: Context?)
    {
        super.onAttach(pContext)
        if (pContext is ActMain)
        {
            m_IfCallback = pContext
        }
        else
        {
            throw RuntimeException(pContext!!.toString() + " must implement FragmentEvent")
        }
    }


    /************************** User Function **************************/
    //------------------------------------------------------------------------
    //
    fun initValue()
    {
        m_arrDistance = ArrayList<distance>()
    }
    //------------------------------------------------------------------------
    //
    fun setInitLayout()
    {
        //event..
        ly_SwipeRefresh.setOnRefreshListener(this)

        m_btn_SaveDistance!!.setOnClickListener(View.OnClickListener {
            if(m_IfCallback != null)
            {
                var bPermissionVal:Boolean = m_IfCallback!!.checkPermission()

                if(bPermissionVal)
                {
                    showDistanceInputDialog()
                }
                else
                {

                }
            }
        })


        settingView()
    }
    //------------------------------------------------------------------------
    //
    fun settingView()
    {
        m_Adapter = DistanceRvAdapter(m_Context!!, m_arrDistance!!)
        m_RecyclerView!!.adapter = m_Adapter

        var nSpanCnt = 3
        /*
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)//landspace mode..
        {
            nSpanCnt = 4
        }
        */

        val pLayoutManager = GridLayoutManager(m_Context, nSpanCnt)
        m_RecyclerView!!.layoutManager = pLayoutManager
        m_RecyclerView!!.setHasFixedSize(true)

        m_RecyclerView!!.layoutManager = pLayoutManager
        m_RecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int)
            {
                val visibleItemCount = pLayoutManager.childCount
                val totalItemCount = pLayoutManager.itemCount
                val firstVisible = pLayoutManager.findFirstVisibleItemPosition()

                if(!m_bRunning && (visibleItemCount + firstVisible) >= totalItemCount)
                {
                    // Call your API to load more items
                    if(!m_bPagingFinish)
                        getDistanceListProc(m_strSeq!!)
                }
            }
        })

        getDistanceListProc("")
    }

    //--------------------------------------------------------------
    //
    fun getDistanceListProc(seq:String)
    {
        m_bRunning = true
        m_App!!.showLoadingDialog(ly_LoadingDialog)

        var pQuery: Query = m_App!!.m_FirebaseDbCtrl!!.getDistanceList(seq!!)
        //pQuery!!.addListenerForSingleValueEvent(listenerForSingleValueEvent)
        //pQuery!!.addChildEventListener(childEventListener)
        pQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                // A new message has been added
                // onChildAdded() will be called for each node at the first time
                if(!m_strSeq.equals(dataSnapshot!!.key))
                {
                    val pInfo: distance = dataSnapshot!!.getValue(distance::class.java)!!
                    if(pInfo.user_fk.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))
                    {
                        m_strSeq = dataSnapshot!!.key
                        pInfo.seq = dataSnapshot!!.key
                        m_Adapter!!.addData(pInfo)
                    }
                }
                else
                {
                    //no more data
                    m_bPagingFinish = true
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                //Log.e("TAG", "onChildChanged:" + dataSnapshot!!.key)

                // A message has changed
                //val message = dataSnapshot.getValue(Message::class.java)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?)
            {
                //Log.e(TAG, "onChildRemoved:" + dataSnapshot!!.key)

                // A message has been removed
                //val message = dataSnapshot.getValue(Message::class.java)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                //Log.e(TAG, "onChildMoved:" + dataSnapshot!!.key)

                // A message has changed position
                //val message = dataSnapshot.getValue(Message::class.java)
            }

            override fun onCancelled(databaseError: DatabaseError?)
            {
                //Log.e(TAG, "postMessages:onCancelled", databaseError!!.toException())
            }
        })

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?)
            {
                m_bRunning = false
                m_App!!.hideLoadingDialog(ly_LoadingDialog)
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }

    //--------------------------------------------------------------
    //
    @SuppressLint("MissingPermission")
    fun showDistanceInputDialog()
    {
        val builder = AlertDialog.Builder(m_Context!!)
        builder.setMessage(getString(R.string.str_msg_5))
        //custom view..
        var pLayout: LinearLayout? = LinearLayout(m_Context)
        pLayout!!.orientation = LinearLayout.VERTICAL

        var editName: EditText? = EditText(m_Context)
        editName!!.hint = getString(R.string.str_msg_4)
        pLayout.addView(editName)

        builder.setView(pLayout)

        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            var pInfo:distance = distance()//init
            pInfo.name = editName.text.toString()
            pInfo.user_fk = m_App!!.m_SpCtrl!!.getSpUserKey()
            pInfo.location_list = ArrayList()

            if(m_IfCallback != null)
                m_IfCallback!!.setDistanceInfo(pInfo)

            /*
            var minute:String = editTime.text.toString()//사용자가 입력한 분
            var minTime:Long = minute.toLong() * 1000 * 60

            if(m_IfCallback != null)
                m_IfCallback!!.setLocationManagerInterval(minTime)
            */

            saveDistanceProc()
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
    fun saveDistanceProc()
    {
        var pInfo:distance? = null

        if(m_IfCallback != null)
        {
            pInfo = m_IfCallback!!.getSavedDistanceInfo()

            if(pInfo != null)
            {
                var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setDistanceInfo(pInfo)
                pDbRef.addListenerForSingleValueEvent(object : ValueEventListener
                {
                    override fun onDataChange(dataSnapshot: DataSnapshot?)
                    {
                        if (dataSnapshot!!.exists())
                        {
                            val pInfo: distance = dataSnapshot.getValue(distance::class.java)!!
                        }

                        pInfo = null
                    }

                    override fun onCancelled(p0: DatabaseError?)
                    {

                    }
                })
            }
        }
    }
    //--------------------------------------------------------------
    //
    fun addLocationPointProc()
    {
        if(m_IfCallback != null)
        {
            //-LJSUyiSTYMkz6dLFo8N location_list 에 업데이트

            var locationInfo = m_IfCallback!!.getLocation()
            var userKey:String? = m_App!!.m_SpCtrl!!.getSpUserKey()//G292919

            var pInfo:location_info = location_info(locationInfo.latitude.toString(), locationInfo.longitude.toString())
        }
    }
    //----------------------------------------------------------------------
    //
    fun setRefresh()
    {
        m_strSeq = null
        m_arrDistance = ArrayList<distance>()
        if(m_Adapter != null)
            m_Adapter!!.clearData()

        ly_SwipeRefresh.setRefreshing(false)

        getDistanceListProc("")
    }
    /************************** callback **************************/
    //----------------------------------------------------------------------
    //swiper listener callback
    override fun onRefresh()
    {
        setRefresh()
    }

    /************************** interface **************************/
    //----------------------------------------------------------------------
    //
    interface ifCallback
    {
        fun checkPermission():Boolean
        fun checkLocationInfo():Boolean
        fun setDistanceInfo(pInfo:distance)
        fun getSavedDistanceInfo():distance
        fun disableDistanceSave()
        fun getLocation(): Location
    }
    /************************** util **************************/
    //-----------------------------------------------------------------
    //editText max Length..
    fun EditText.limitLength(maxLength: Int)
    {
        filters = arrayOf(InputFilter.LengthFilter(maxLength))
    }
}
