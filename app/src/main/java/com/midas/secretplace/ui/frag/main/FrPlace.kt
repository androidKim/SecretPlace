package com.midas.secretplace.ui.frag.main

import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.firebase.database.*
import com.midas.mytimeline.ui.adapter.PlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.ReqBase
import com.midas.secretplace.structure.core.photo
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.act.ActMain
import com.midas.secretplace.ui.custom.SimpleDividerItemDecoration
import kotlinx.android.synthetic.main.frag_place.*




class FrPlace : Fragment(), SwipeRefreshLayout.OnRefreshListener, PlaceRvAdapter.ifCallback
{
    /**************************** Define ****************************/

    /**************************** Member ****************************/
    var m_Context: Context? = null
    var m_Activity:Activity? = null
    var m_App:MyApp? = null
    var m_IfCallback:ifCallback? = null
    var m_Adapter:PlaceRvAdapter? = null
    var m_arrPlace:ArrayList<place>? = null

    var m_strSeq:String? = null
    var m_bRunning:Boolean = false
    var m_bPagingFinish:Boolean = false

    /**************************** Controller ****************************/
    var m_RecyclerView:RecyclerView? = null

    /**************************** System Function ****************************/
    //------------------------------------------------------------------------
    //
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.frag_place, container, false)

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

    /**************************** User Function ****************************/
    //------------------------------------------------------------------------
    //
    fun initValue()
    {
        m_strSeq = ""
        m_arrPlace = ArrayList<place>()
    }
    //------------------------------------------------------------------------
    //
    fun setInitLayout()
    {
        //event..
        ly_SwipeRefresh.setOnRefreshListener(this)

        fbtn_SaveLocation?.setOnClickListener(View.OnClickListener
        {
            if(m_IfCallback != null)
            {
                var bPermissionVal:Boolean = m_IfCallback!!.checkPermission()

                if(bPermissionVal)
                {
                    seveLocationDialog()
                }
                else
                {

                }
            }
        })

        settingView()
    }
    //--------------------------------------------------------------
    //
    fun settingView()
    {
        m_Adapter = PlaceRvAdapter(m_Context!!, m_arrPlace!!, this)
        m_RecyclerView!!.adapter = m_Adapter

        m_RecyclerView!!.addItemDecoration(SimpleDividerItemDecoration(20))

        var nSpanCnt = 1
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
                        getPlaceListProc(m_strSeq!!)
                }
            }
        })

        getPlaceListProc("")
    }
    //--------------------------------------------------------------
    //
    fun getPlaceListProc(seq:String)
    {
        m_bRunning = true
        //m_App!!.showLoadingDialog(ly_LoadingDialog)
        progressBar.visibility = View.VISIBLE

        //var pQuery: Query = m_App!!.m_FirebaseDbCtrl!!.getPlaceList(seq!!)
        //pQuery!!.addListenerForSingleValueEvent(listenerForSingleValueEvent)
        //pQuery!!.addChildEventListener(childEventListener)

        var pQuery:Query? = null

        if(seq != null)
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE).orderByKey().startAt(seq).limitToFirst(ReqBase.ITEM_COUNT)
        else
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE).orderByKey().limitToFirst(ReqBase.ITEM_COUNT)

        pQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                // A new message has been added
                // onChildAdded() will be called for each node at the first time
                if(!m_strSeq.equals(dataSnapshot!!.key))
                {
                    m_bPagingFinish = false
                    val pInfo: place = dataSnapshot!!.getValue(place::class.java)!!
                    if(pInfo.user_fk.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))
                    {
                        m_strSeq = dataSnapshot!!.key
                        pInfo.seq = m_strSeq
                        if(pInfo.img_list != null)
                        {
                            //pInfo!!.img_list!!.removeAt(0)
                        }

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
                //m_App!!.hideLoadingDialog(ly_LoadingDialog)
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }
    //--------------------------------------------------------------
    //
    fun seveLocationDialog()
    {
        if(m_IfCallback != null)
        {
            var bCheckLocation:Boolean = m_IfCallback!!.checkLocationInfo()

            if(bCheckLocation)
            {
                var locationInfo = m_IfCallback!!.getLocation()
                var userKey:String? = m_App!!.m_SpCtrl!!.getSpUserKey()//G292919
                var tempArr:ArrayList<photo> = ArrayList()
                var pInfo:place = place("null",userKey!!, "null", String.format("%s",locationInfo.latitude), String.format("%s",locationInfo.longitude), tempArr)
                showPlaceInputDialog(pInfo)
            }
        }
    }
    //--------------------------------------------------------------
    //
    fun showPlaceInputDialog(pInfo:place)
    {
        if(pInfo == null)
            return

        val builder = AlertDialog.Builder(m_Context!!)
        builder.setMessage(getString(R.string.str_msg_3))
        var editName: EditText? = EditText(m_Context)
        editName!!.hint = getString(R.string.str_msg_4)
        builder.setView(editName)
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            pInfo.name = editName.text.toString()
            var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setPlaceInfo(pInfo)
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot?)
                {
                    if (dataSnapshot!!.exists())
                    {
                        if(!m_strSeq.equals(dataSnapshot!!.key))
                        {
                            m_strSeq = dataSnapshot!!.key
                        }

                        pInfo.seq = dataSnapshot!!.key
                        m_App!!.m_FirebaseDbCtrl!!.setPlaceInfo(pInfo)
                    }
                }

                override fun onCancelled(p0: DatabaseError?)
                {

                }
            })
        }

        builder.setNegativeButton(getString(R.string.str_no)){dialog,which ->

        }

        builder.setNeutralButton(getString(R.string.str_cancel)){_,_ ->

        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //----------------------------------------------------------------------
    //
    fun setRefresh()
    {
        m_strSeq = null
        m_arrPlace = ArrayList<place>()
        if(m_Adapter != null)
            m_Adapter!!.clearData()

        ly_SwipeRefresh.setRefreshing(false)

        getPlaceListProc("")
    }

    /******************************** Listener ********************************/
    //----------------------------------------------------------------------
    //
    override fun onRefresh()
    {
        setRefresh()
    }
    /******************************** callback function ********************************/
    //----------------------------------------------------------------------
    //listAdapter callback
    override fun deleteProc(pInfo: place)
    {
        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!.child(pInfo.seq)//where
        pDbRef!!.removeValue()

        setRefresh()
    }
    //----------------------------------------------------------------------
    //listAdapter callback
    override fun checkPermission(): Boolean
    {
        var bPermissionVal:Boolean = m_IfCallback!!.checkPermission()
        return bPermissionVal
    }

    /******************************** interface ********************************/
    //----------------------------------------------------------------------
    //
    interface ifCallback
    {
        fun checkPermission():Boolean
        fun checkLocationInfo():Boolean
        fun getLocation():Location
    }

}
