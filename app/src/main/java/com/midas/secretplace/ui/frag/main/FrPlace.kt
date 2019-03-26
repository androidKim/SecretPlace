package com.midas.secretplace.ui.frag.main

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ShareCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.midas.mytimeline.ui.adapter.PlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.structure.room.data_place
import com.midas.secretplace.structure.vm.vm_place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.act.ActMain
import com.midas.secretplace.ui.act.ActMapDetail
import com.midas.secretplace.ui.act.ActPlaceDetail
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.frag_place.*
import pl.kitek.rvswipetodelete.SwipeToDeleteCallback
import java.io.Serializable


class FrPlace : Fragment(), SwipeRefreshLayout.OnRefreshListener, PlaceRvAdapter.ifCallback
{
    /**************************** Define ****************************/

    /**************************** Member ****************************/
    private var mViewModelPlace: vm_place?= null//mvvm
    var m_Context: Context? = null
    var m_Activity:Activity? = null
    var m_App:MyApp? = null
    var m_IfCallback:ifCallback? = null
    var m_RequestManager: RequestManager? = null
    var m_Adapter:PlaceRvAdapter? = null
    var m_arrPlace:ArrayList<place>? = ArrayList()

    //var m_strPlaceLastSeq:String? = ""
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

        m_RequestManager = Glide.with(m_Context)

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
        //m_strPlaceLastSeq = ""
        m_arrPlace = ArrayList<place>()
    }
    //------------------------------------------------------------------------
    //
    fun setViewModel(){
        mViewModelPlace = ViewModelProviders.of(this).get(vm_place::class.java)
        mViewModelPlace?.deleteAll()//init..
        mViewModelPlace?.placeList?.observe(this, object : Observer<List<data_place>> {
            override fun onChanged(list: List<data_place>?) {
                if(list != null)
                {
                    return
                }
            }
        })
    }
    //------------------------------------------------------------------------
    //
    fun setInitLayout()
    {
        setViewModel()

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
    //-----------------------------------------------------
    //show map dialog
    fun goMapDetail()
    {
        if(m_Adapter != null)
            m_arrPlace = m_Adapter!!.placeList

        if(m_arrPlace == null)
            return

        if(m_arrPlace!!.size == 0)
            return

        var pIntent = Intent(m_Context, ActMapDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_PLACE_LIST_OBJECT, m_arrPlace!! as Serializable)
        startActivity(pIntent)
    }
    //--------------------------------------------------------------
    //
    fun settingView()
    {
        m_Adapter = PlaceRvAdapter(m_Context!!, m_RequestManager!!, m_arrPlace!!, this)
        m_RecyclerView!!.adapter = m_Adapter

        var nSpanCnt = 1
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)//landspace mode..
        {
            nSpanCnt = 1
        }

        val pLayoutManager = GridLayoutManager(m_Context, nSpanCnt)
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
                    //if(!m_bPagingFinish)
                        //getPlaceListProc(m_strPlaceLastSeq!!)
                }
            }
        })

        //swipe remove listener..
        val swipeHandler = object : SwipeToDeleteCallback(m_Context!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //val adapter = recyclerView.adapter as SimpleAdapter
                m_Adapter!!.removeAt(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)


        getPlaceListProc("")
    }
    //--------------------------------------------------------------
    //
    fun getPlaceListProc(seq:String)
    {
        progressBar.visibility = View.VISIBLE
        m_bRunning = true
        var pQuery:Query? = null
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE).child(m_App!!.m_SpCtrl!!.getSpUserKey()).orderByKey()
        pQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                if(dataSnapshot!!.exists())
                {
                    m_bPagingFinish = false
                    val pInfo:place = dataSnapshot!!.getValue(place::class.java)!!
                    pInfo.place_key = dataSnapshot!!.key
                }
                else
                {
                    m_bPagingFinish = true
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?)
            {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {

            }

            override fun onCancelled(databaseError: DatabaseError?)
            {

            }
        })

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if(dataSnapshot!!.exists())
                {
                    m_bPagingFinish = false
                    val children = dataSnapshot!!.children
                    children.forEach {
                        val pInfo:place = it!!.getValue(place::class.java)!!
                        m_Adapter!!.addData(pInfo)

                        var dataPlace:data_place = data_place(0,
                                pInfo!!.user_key!!,
                                pInfo!!.place_key!!,
                                pInfo!!.group_key!!,
                                pInfo!!.name!!,
                                pInfo!!.lat!!,
                                pInfo!!.lng!!,
                                pInfo!!.memo!!,
                                pInfo!!.address!!,
                                pInfo!!.img_url!!)
                        mViewModelPlace?.insert(dataPlace)//
                    }
                }
                else
                {
                    m_bPagingFinish = true
                }


                m_bRunning = false
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }
    //----------------------------------------------------------------------
    //
    fun menuItemShowMap(){
        if(m_Adapter == null)
        {
            Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.str_no_exit_location), Toast.LENGTH_SHORT).show()
            return
        }

        if(m_Adapter!!.itemCount <= 0)
        {
            Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.str_no_exit_location), Toast.LENGTH_SHORT).show()
            return
        }


        if(m_IfCallback != null)
        {
            var bPermissionVal:Boolean = m_IfCallback!!.checkPermission()
            if(bPermissionVal)
            {
                goMapDetail()
            }
            else
            {

            }
        }
    }
    //----------------------------------------------------------------------
    //
    fun menuItemShareLocation(){
        if(m_IfCallback != null)
        {
            var bPermissionVal:Boolean = m_IfCallback!!.checkPermission()
            if(bPermissionVal)
            {
                Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.str_share_my_location), Toast.LENGTH_SHORT).show()

                var locationInfo = m_IfCallback!!.getLocation()
                var strAddress:String = Util.getAddress(m_Context!!, locationInfo.latitude, locationInfo.longitude)
                var strMyLocation:String = String.format("주소 : %s, 위도 : %s, 경도 : %s", strAddress, locationInfo.latitude, locationInfo.longitude)

                val shareIntent = ShareCompat.IntentBuilder.from(activity)
                        .setText(strMyLocation)
                        .setType("text/plain")
                        .createChooserIntent()
                        .apply {
                            // https://android-developers.googleblog.com/2012/02/share-with-intents.html
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                // If we're on Lollipop, we can open the intent as a document
                                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                            } else {
                                // Else, we will use the old CLEAR_WHEN_TASK_RESET flag
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                            }
                        }
                startActivity(shareIntent)
            }
            else
            {

            }
        }
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
                var address:String? = Util.getAddress(m_Context!!, locationInfo.latitude, locationInfo.longitude)
                var pInfo:place = place(userKey!!, "", "", "", String.format("%s",locationInfo.latitude), String.format("%s",locationInfo.longitude), "", address!!, "")
                showPlaceInputDialog(pInfo)
            }
        }
    }
    //--------------------------------------------------------------
    //
    fun showPlaceInputDialog(pInfo:place)
    {
        //val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
        //"/"+currentFirebaseUser!!.uid

        if(pInfo == null)
            return

        val builder = AlertDialog.Builder(m_Context!!)
        builder.setMessage(getString(R.string.str_msg_3))
        var editName: EditText? = EditText(m_Context)
        editName!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL//singline..
        editName!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))//maxlength
        editName!!.hint = getString(R.string.str_msg_4)
        builder.setView(editName)
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            pInfo!!.name = editName.text.toString()

            var pDbRef:DatabaseReference? = null
            pDbRef =  m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey())!!
                    .push()!!//insert..

            pDbRef!!.setValue(pInfo!!)//insert
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot?)
                {
                    if (dataSnapshot!!.exists())
                    {
                        //key update
                        var key = dataSnapshot!!.key
                        pInfo.place_key = key
                        pDbRef =  m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!
                                .child(m_App!!.m_SpCtrl!!.getSpUserKey())!!
                                .child(key)!!//

                        pDbRef!!.setValue(pInfo)//insert
                        m_Adapter!!.addData(pInfo!!)

                        var dataPlace:data_place = data_place(0,
                                pInfo!!.user_key!!,
                                pInfo!!.place_key!!,
                                pInfo!!.group_key!!,
                                pInfo!!.name!!,
                                pInfo!!.lat!!,
                                pInfo!!.lng!!,
                                pInfo!!.memo!!,
                                pInfo!!.address!!,
                                pInfo!!.img_url!!)
                        mViewModelPlace?.insert(dataPlace)//
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
        //m_strPlaceLastSeq = ""
        m_arrPlace = ArrayList<place>()
        if(m_Adapter != null)
            m_Adapter!!.clearData()

        ly_SwipeRefresh.isRefreshing = false

        getPlaceListProc("")
    }
    //----------------------------------------------------------------------
    //storage image delete
    fun storageDeleteItemProc(placeKey:String)
    {
        val storageRef = FirebaseStorage.getInstance(Constant.FIRE_STORE_URL)

        var pQuery:Query? = null
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .child(placeKey).orderByKey()

        pQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                if(dataSnapshot!!.exists())
                {

                }
                else
                {

                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                //Log.e("TAG", "onChildChanged:" + dataSnapshot!!.key)

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?)
            {
                //Log.e(TAG, "onChildRemoved:" + dataSnapshot!!.key)

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                //Log.e(TAG, "onChildMoved:" + dataSnapshot!!.key)


            }

            override fun onCancelled(databaseError: DatabaseError?)
            {
                //Log.e(TAG, "postMessages:onCancelled", databaseError!!.toException())
            }
        })

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if(dataSnapshot!!.exists())
                {
                    val children = dataSnapshot!!.children
                    children.forEach {
                        var hashMap = it.value as HashMap<Object, String>
                        var fileNm:String = hashMap.values.toString()
                        fileNm = fileNm.replace("[","")
                        fileNm = fileNm.replace("]","")

                        //split ?
                        var arrTemp:List<String> = fileNm.split("?")
                        fileNm = arrTemp.get(0)
                        //split "/"  get lastItem is FileName
                        arrTemp = fileNm.split("/")
                        fileNm = arrTemp.get(arrTemp.size - 1)

                        // Create a reference to the file to delete
                        var desertRef = storageRef.reference.child(fileNm)//
                        // Delete the file
                        desertRef.delete().addOnSuccessListener {
                            // File deleted successfully

                        }.addOnFailureListener {
                            // Uh-oh, an error occurred!

                        }


                    }
                }
                else
                {

                }
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })

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
        //place data remove
        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .child(pInfo.place_key)//where
        pDbRef!!.removeValue()

        //file storage remove
        storageDeleteItemProc(pInfo.place_key!!)

        //file data remove
        pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .child(pInfo.place_key)//where

        pDbRef!!.removeValue()

        //refresh
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
    override fun moveDetailActivity(pInfo: place)
    {
        var pIntent = Intent(m_Context, ActPlaceDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_PLACE_OBJECT, pInfo as Serializable)
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
