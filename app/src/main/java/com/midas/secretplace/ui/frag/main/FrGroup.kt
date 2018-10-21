package com.midas.secretplace.ui.frag.main

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.midas.mytimeline.ui.adapter.GroupRvAdapter
import com.midas.mytimeline.ui.adapter.PlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.ReqBase
import com.midas.secretplace.structure.core.group
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.act.ActGroupDetail
import com.midas.secretplace.ui.act.ActMain
import com.midas.secretplace.ui.custom.SimpleDividerItemDecoration
import kotlinx.android.synthetic.main.frag_group.*
import java.io.Serializable


class FrGroup : Fragment(), SwipeRefreshLayout.OnRefreshListener, GroupRvAdapter.ifCallback
{

    /**************************** Define ****************************/

    /**************************** Member ****************************/
    var m_Context: Context? = null
    var m_Activity:Activity? = null
    var m_App:MyApp? = null
    var m_IfCallback:ifCallback? = null
    var m_Adapter:GroupRvAdapter? = null
    var m_arrGroup:ArrayList<group>? = null

    var m_strSeq:String? = ""//group key
    var m_bRunning:Boolean = false
    var m_bPagingFinish:Boolean = false

    /**************************** Controller ****************************/
    var m_RecyclerView:RecyclerView? = null

    /**************************** System Function ****************************/
    //------------------------------------------------------------------------
    //
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.frag_group, container, false)

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
    //------------------------------------------------------------------------
    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Constant.FOR_RESULT_IS_REFRESH)
        {
            setRefresh()
        }
    }

    /**************************** User Function ****************************/
    //------------------------------------------------------------------------
    //
    fun initValue()
    {
        m_strSeq = ""
        m_arrGroup = ArrayList<group>()
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
        m_Adapter = GroupRvAdapter(m_Context!!, m_arrGroup!!, this)
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
        if(!m_strSeq.equals(""))
        {
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP).child(m_strSeq).orderByChild("user_key").equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())//.limitToFirst(ReqBase.ITEM_COUNT)
        }
        else
        {
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP).orderByChild("user_key").equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())//.limitToFirst(ReqBase.ITEM_COUNT)
        }


        pQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                if(dataSnapshot!!.exists())
                {
                    if(!m_strSeq.equals(dataSnapshot!!.key))
                    {
                        m_bPagingFinish = false
                        val pInfo:group = dataSnapshot!!.getValue(group::class.java)!!
                        m_strSeq = dataSnapshot!!.key
                        pInfo.group_key = dataSnapshot!!.key
                        m_Adapter!!.addData(pInfo!!)
                    }
                }
                else
                {
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
                if(!p0!!.exists())
                    m_bPagingFinish = true

                m_bRunning = false
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
                var userKey:String? = m_App!!.m_SpCtrl!!.getSpUserKey()//G292919...xxx

                var pInfo:group = group(userKey!!, "", "")
                showInputDialog(pInfo)
            }
        }
    }
    //--------------------------------------------------------------
    //
    fun showInputDialog(pInfo:group)
    {
        if(pInfo == null)
            return

        val builder = AlertDialog.Builder(m_Context!!)
        builder.setMessage(getString(R.string.str_msg_3))
        var editName: EditText? = EditText(m_Context)
        editName!!.hint = getString(R.string.str_msg_4)
        builder.setView(editName)
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            pInfo!!.name = editName.text.toString()

            var pDbRef:DatabaseReference? = null
            pDbRef =  m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)!!.push()//insert..
            pDbRef!!.setValue(pInfo!!)//insert
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot?)
                {
                    if (dataSnapshot!!.exists())
                    {
                        m_strSeq = dataSnapshot!!.key
                        pInfo!!.group_key = dataSnapshot!!.key
                        m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)!!.child(dataSnapshot!!.key).setValue(pInfo)//update..
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
        m_strSeq = ""
        m_arrGroup = ArrayList<group>()
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
    override fun deleteGroupProc(pInfo: group)
    {
        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)!!.child(pInfo.group_key)//where
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
    //----------------------------------------------------------------------
    //listAdapter callback
    override fun moveDetailActivity(pInfo: group)
    {
        var pIntent = Intent(m_Context, ActGroupDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_GROUP_OBJECT, pInfo as Serializable)
        startActivityForResult(pIntent, 0)
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
