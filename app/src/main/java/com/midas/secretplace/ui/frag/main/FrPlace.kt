package com.midas.secretplace.ui.frag.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.midas.mytimeline.ui.adapter.PlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.CategoryType
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.category
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.act.ActMain
import com.midas.secretplace.ui.act.ActMapDetail
import com.midas.secretplace.ui.act.ActPlaceDetail
import com.midas.secretplace.ui.adapter.CateSpinnerAdapter
import com.midas.secretplace.ui.adapter.DialogSpinnerAdapter
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.frag_place.*
import pl.kitek.rvswipetodelete.SwipeToDeleteCallback
import java.io.Serializable


class FrPlace : Fragment(), SwipeRefreshLayout.OnRefreshListener, PlaceRvAdapter.ifCallback
{
    /**************************** Define ****************************/

    /**************************** Member ****************************/
    //private var mViewModelPlace: vm_place?= null//mvvm
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
    var m_RecyclerView: RecyclerView? = null

    /**************************** System Function ****************************/
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
    override fun onStart() {
        super.onStart()
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
    fun getCategoryList():ArrayList<category>{
        var array:ArrayList<category> = ArrayList()
        array.add(category("","전체"))
        array.add(category(CategoryType.DR0.name,"직접 입력"))
        array.add(category(CategoryType.MT1.name,"대형마트"))
        array.add(category(CategoryType.CS2.name,"편의점"))
        array.add(category(CategoryType.PS3.name,"어린이집,유치원"))
        array.add(category(CategoryType.SC4.name,"학교"))
        array.add(category(CategoryType.AC5.name,"학원"))
        array.add(category(CategoryType.PK6.name,"주차장"))
        array.add(category(CategoryType.OL7.name,"주유소,전소"))
        array.add(category(CategoryType.SW8.name,"지하철역"))
        array.add(category(CategoryType.BK9.name,"은행"))
        array.add(category(CategoryType.CT1.name,"문화시설"))
        array.add(category(CategoryType.AG2.name,"중개업소"))
        array.add(category(CategoryType.PO3.name,"공공기관"))
        array.add(category(CategoryType.AT4.name,"관광명소"))
        array.add(category(CategoryType.AD5.name,"숙박"))
        array.add(category(CategoryType.FD6.name,"음식점"))
        array.add(category(CategoryType.CE7.name,"카페"))
        array.add(category(CategoryType.HP8.name,"병원"))
        array.add(category(CategoryType.PM9.name,"약국"))
        return array
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
                //시스템권한
                var bLocationUseable:Boolean = m_IfCallback!!.checkLocationInfo()
                if(!bLocationUseable)
                {

                }
                else
                {
                    //앱권한
                    var bPermissionVal:Boolean = m_IfCallback!!.checkPermission()
                    if(bPermissionVal)
                    {
                        seveLocationDialog()
                    }
                }
            }
        })

        //spinner
        var adapter:CateSpinnerAdapter = CateSpinnerAdapter(activity!!, getCategoryList())
        cateSpinner.adapter = adapter
        cateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val cateInfo: category? = adapter.getItem(position)
                if(cateInfo!!.code.equals("")){
                    setRefresh()//전체
                }else{
                    setRefresh(cateInfo!!.code)//
                }
            }
        }
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
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
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
    }
    //--------------------------------------------------------------
    //
    fun getPlaceListProc(categoryCode:String)
    {
        progressBar.visibility = View.VISIBLE
        m_bRunning = true
        var pQuery:Query? = null
        if(categoryCode.equals("")){
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE).child(m_App!!.m_SpCtrl!!.getSpUserKey()!!).orderByKey()
        }else{
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey()!!).orderByChild("categoryCode").equalTo(categoryCode)
        }

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if(dataSnapshot!!.exists())
                {
                    m_bPagingFinish = false
                    ly_Empty.visibility = View.GONE
                    val children = dataSnapshot!!.children
                    children.forEach {
                        val pInfo:place = it!!.getValue(place::class.java)!!
                        m_Adapter!!.addData(pInfo)

                        /*
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
                        */
                    }

                    m_Adapter!!.reverseList()
                }
                else
                {
                    ly_Empty.visibility = View.VISIBLE
                    m_bPagingFinish = true
                }

                m_bRunning = false
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(p0: DatabaseError)
            {

            }
        })
    }
    //----------------------------------------------------------------------
    //
    fun menuItemShowMap(){
        if(m_Adapter == null) {
            Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.str_no_exit_location), Toast.LENGTH_SHORT).show()
            return
        }

        if(m_Adapter!!.itemCount <= 0) {
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
            var locationInfo = m_IfCallback!!.getLocation()
            if(locationInfo != null)
            {
                if(locationInfo.latitude ==0.0 || locationInfo.longitude == 0.0)
                {
                    //위치정보를 가져오지 못함..
                    Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.not_call_location), Toast.LENGTH_SHORT).show()
                    return
                }
                else
                {
                    var userKey:String? = m_App!!.m_SpCtrl!!.getSpUserKey()//G292919...xxx
                    var address:String? = Util.getAddress(m_Context!!, locationInfo.latitude, locationInfo.longitude)
                    var pInfo:place = place(userKey!!, "", "", "", String.format("%s",locationInfo.latitude), String.format("%s",locationInfo.longitude), "", address!!, "", "N","","")
                    showPlaceInputDialog(pInfo)
                    return
                }
            }
            else
            {
                //위치정보를 가져오지 못함..
                Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.not_call_location), Toast.LENGTH_SHORT).show()
                return
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
        builder.setTitle(getString(R.string.str_msg_3))

        var layout:LinearLayout = LinearLayout(m_Context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 0, 0)
        layout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        //Cate msg
        var tvCateMsg:TextView = TextView(m_Context)
        tvCateMsg.textSize = 15f
        val black:String = "#000000"
        tvCateMsg.setTextColor(Color.parseColor(black))
        tvCateMsg.text = "구분을 선택하세요."
        tvCateMsg.setPadding(20, 20, 0, 0)
        tvCateMsg.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layout.addView(tvCateMsg)

        //CateCode spinner
        var spinner:Spinner = Spinner(m_Context)
        spinner.setPadding(0, 20, 0, 0)
        var dialogCateAdapter = DialogSpinnerAdapter(m_Context!!, getCategoryList())
        spinner.adapter = dialogCateAdapter
        layout.addView(spinner)

        var editDirectInput: EditText? = EditText(m_Context)
        editDirectInput!!.visibility = View.VISIBLE
        editDirectInput!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL//singline..
        editDirectInput!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))//maxlength
        editDirectInput!!.hint = "구분"
        editDirectInput.visibility = View.GONE

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val cateInfo: category? = dialogCateAdapter.getItem(position)
                pInfo.categoryCode = cateInfo!!.code
                pInfo.categoryName = cateInfo!!.name
                if(pInfo.categoryCode.equals(CategoryType.DR0.name))//구분
                {
                    editDirectInput.visibility = View.VISIBLE
                }
                else
                {
                    editDirectInput.visibility = View.GONE
                }
            }
        }

        layout.addView(editDirectInput)

        //name
        var editName: EditText? = EditText(m_Context)
        editName!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL//singline..
        editName!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(30))//maxlength
        editName!!.hint = getString(R.string.str_msg_4)
        layout.addView(editName)

        builder.setView(layout)
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            if(editName.text.toString().equals(""))
            {
                Toast.makeText(m_Context, "장소이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            else
            {
                pInfo!!.name = editName.text.toString()
                if(pInfo.categoryCode.equals(CategoryType.DR0.name))//직접입력이면..
                {
                    val str:String = editDirectInput?.text.toString()
                    if(str.equals(""))
                    {
                        Toast.makeText(m_Context, "구분을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    else
                    {
                        pInfo.categoryName = str
                    }
                }

                var pDbRef:DatabaseReference? = null
                pDbRef =  m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!
                        .child(m_App!!.m_SpCtrl!!.getSpUserKey()!!)!!
                        .push()!!//insert..

                pDbRef!!.setValue(pInfo!!)//insert
                pDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot)
                    {
                        if (dataSnapshot!!.exists())
                        {
                            //key update
                            var key = dataSnapshot!!.key
                            pInfo.place_key = key
                            pDbRef =  m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!
                                    .child(m_App!!.m_SpCtrl!!.getSpUserKey()!!)!!
                                    .child(key!!)!!//

                            pDbRef!!.setValue(pInfo)//insert
                            m_Adapter!!.addFirst(pInfo!!)
                            ly_Empty.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(p0: DatabaseError)
                    {

                    }
                })
            }
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
    //
    fun setRefresh(categoryCode:String)
    {
        //m_strPlaceLastSeq = ""
        m_arrPlace = ArrayList<place>()
        if(m_Adapter != null)
            m_Adapter!!.clearData()

        ly_SwipeRefresh.isRefreshing = false

        getPlaceListProc(categoryCode)
    }
    //----------------------------------------------------------------------
    //storage image delete
    fun storageDeleteItemProc(placeKey:String)
    {
        val storageRef = FirebaseStorage.getInstance(Constant.FIRE_STORE_URL)

        var pQuery:Query? = null
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!
                .child(m_App!!.m_SpCtrl!!.getSpUserKey()!!)
                .child(placeKey).orderByKey()

        pQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?)
            {
                if(dataSnapshot!!.exists())
                {

                }
                else
                {

                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?)
            {
                //Log.e("TAG", "onChildChanged:" + dataSnapshot!!.key)

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot)
            {
                //Log.e(TAG, "onChildRemoved:" + dataSnapshot!!.key)

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?)
            {
                //Log.e(TAG, "onChildMoved:" + dataSnapshot!!.key)


            }

            override fun onCancelled(databaseError: DatabaseError)
            {
                //Log.e(TAG, "postMessages:onCancelled", databaseError!!.toException())
            }
        })

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if(dataSnapshot!!.exists())
                {
                    val children = dataSnapshot!!.children
                    children.forEach {
                        var fileNm:String = it.value as String
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

            override fun onCancelled(p0: DatabaseError)
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
    override fun deleteProc(pInfo: place, position:Int)
    {
        val builder = AlertDialog.Builder(activity!!)
        builder.setCancelable(false)
        builder.setMessage(getString(R.string.msg_question_delete))
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->

            m_Adapter!!.removeRow(position)

            //place data remove
            var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey()!!)
                    .child(pInfo.place_key!!)//where
            pDbRef!!.removeValue()

            //file storage remove
            storageDeleteItemProc(pInfo.place_key!!)

            //file data remove
            pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey()!!)
                    .child(pInfo.place_key!!)//where

            pDbRef!!.removeValue()

            //refresh
            setRefresh()
        }

        builder.setNegativeButton(getString(R.string.str_no)){dialog,which ->
            m_Adapter!!.notifyItemChanged(position)
        }

        builder.setNeutralButton(getString(R.string.str_cancel)){_,_ ->
            m_Adapter!!.notifyItemChanged(position)
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
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
