
package com.midas.secretplace.ui.act

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.midas.mytimeline.ui.adapter.HorizontalPlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.group
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.adapter.PhotoRvAdapter
import com.midas.secretplace.ui.frag.MapFragment
import kotlinx.android.synthetic.main.act_group_detail.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class ActGroupDetail : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,HorizontalPlaceRvAdapter.ifCallback,PhotoRvAdapter.ifCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, MapFragment.ifCallback
{
    /*********************** Define ***********************/
    //-------------------------------------------------------------
    //
    companion object
    {
        private val IMAGE_DIRECTORY = "/scplace"
        private val REQUEST_TAKE_PHOTO = 1001
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 1002
    }
    /*********************** Member ***********************/
    lateinit var mGoogleApiClient: GoogleApiClient
    var mLocationManager: LocationManager? = null
    lateinit var mLocation: Location
    var mLocationRequest: LocationRequest? = null
    val listener: com.google.android.gms.location.LocationListener? = null
    val UPDATE_INTERVAL = ((5000).toLong())
    val FASTEST_INTERVAL: Long = 5000
    /*
    .setInterval(15000) // 15 seconds
    .setFastestInterval(5000) // 5000ms
        기기는 15초마다 혹은 그보다 더 빠르거나 느리게 위치를 수집하지만 5초보다 빠르게 수집하진 않을것입니다
     */

    lateinit var locationManager: LocationManager

    var m_App: MyApp? = null
    var m_Context: Context? = null
    var m_GroupInfo:group? = group()
    var m_arrPlace:ArrayList<place>? = ArrayList()//group의 place list(horizontal listview)
    var m_LayoutInflater:LayoutInflater? = null
    var m_HorizontalAdapter:HorizontalPlaceRvAdapter? = null
    var m_Adapter: PhotoRvAdapter? = null
    var selectedImage: Uri? = null
    var imageUri: Uri? = null
    var m_PlaceInfo:place? = null
    var m_strImgpath:String ?= null
    //var m_strPlaceLastSeq:String? = ""
    var m_bRunning:Boolean? = false
    var m_bFinish:Boolean? = false
    var m_arrItem:ArrayList<String>? = ArrayList()//imglist(vertical listview)
    /*********************** Controller ***********************/
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_group_detail)
        m_Context = this
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActGroupDetail)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mLocation = Location("dummyProvider")

        initValue()
        recvIntentData()
        initLayout()
    }
    //---------------------------------------------------------------------------------------------------
    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)//Intent?  <-- null이 올수도있다
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM)//select gallery
        {
            if (data != null)
            {
                progressBar.visibility = View.VISIBLE
                val contentURI = data!!.data
                try
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    m_strImgpath = saveImage(bitmap)
                    //iv_Attach!!.setImageBitmap(bitmap)

                    val data = FirebaseStorage.getInstance("gs://secretplace-29d5e.appspot.com")
                    var value = 0.0

                    var timestamp:Long = System.currentTimeMillis()
                    var fileName:String = String.format("%s_%s",timestamp, "img")
                    var storage = data.getReference().child(fileName).putFile(contentURI)
                            .addOnProgressListener { taskSnapshot ->
                                value = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                            }
                            .addOnSuccessListener {
                                taskSnapshot ->
                                val uri = taskSnapshot.downloadUrl

                                //update
                                var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child(m_PlaceInfo!!.place_key).child("img_list").push()//where
                                pDbRef!!.setValue(taskSnapshot.downloadUrl.toString())//insert

                                //var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setPlaceInfo(m_PlaceInfo!!)
                                pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot?)
                                    {
                                        if (dataSnapshot!!.exists())
                                        {
                                            setRefresh()
                                            progressBar.visibility = View.GONE
                                        }
                                    }

                                    override fun onCancelled(p0: DatabaseError?)
                                    {
                                        progressBar.visibility = View.GONE
                                    }
                                })

                            }
                            .addOnFailureListener{
                                exception -> exception.printStackTrace()
                            }

                }
                catch (e: IOException)
                {
                    e.printStackTrace()
                }
            }
        }
        else if (requestCode == REQUEST_TAKE_PHOTO)//take photo
        {
            progressBar.visibility = View.VISIBLE
            try
            {
                try
                {
                    selectedImage = imageUri
                }
                catch (e: Exception)
                {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show()
                }


                val data = FirebaseStorage.getInstance("gs://secretplace-29d5e.appspot.com")
                var value = 0.0

                var timestamp:Long = System.currentTimeMillis()
                var fileName:String = String.format("%s_%s",timestamp, "img")
                var storage = data.getReference().child(fileName).putFile(selectedImage!!)
                        .addOnProgressListener { taskSnapshot ->
                            value = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                        }
                        .addOnSuccessListener {
                            taskSnapshot ->
                            val uri = taskSnapshot.downloadUrl
                            //update
                            var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child(m_PlaceInfo!!.place_key).child("img_list").push()//where
                            pDbRef!!.setValue(taskSnapshot.downloadUrl.toString())//insert
                            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot?)
                                {
                                    if (dataSnapshot!!.exists())
                                    {
                                        setRefresh()
                                        progressBar.visibility = View.GONE
                                    }
                                }

                                override fun onCancelled(p0: DatabaseError?)
                                {
                                    progressBar.visibility = View.GONE
                                }
                            })

                        }
                        .addOnFailureListener{
                            exception -> exception.printStackTrace()
                        }

            }
            catch (e: IOException)
            {
                e.printStackTrace()
            }
        }
    }

    //--------------------------------------------------------------
    //
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        when (requestCode)
        {
            Constant.REQUEST_ID_MULTIPLE_PERMISSIONS ->
            {
                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.ACCESS_FINE_LOCATION] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.size > 0)
                {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.ACCESS_COARSE_LOCATION] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED)
                    {
                        checkPermissionLocation()
                    }
                    else
                    {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                        {
                            checkPermissionLocation()
                        }
                        else
                        {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                                return

                            if(mLocation.latitude <= 0 || mLocation.longitude <= 0)
                            {
                                var fusedLocationProviderClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                                fusedLocationProviderClient .getLastLocation().addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                                    // Got last known location_info. In some rare situations this can be null.
                                    if (location != null)
                                    {
                                        // Logic to handle location_info object
                                        mLocation = location
                                    }
                                })
                            }
                            else
                            {

                            }
                        }
                    }
                }
            }
        }

    }
    /*********************** Location Function ***********************/
    //--------------------------------------------------------------
    //location_info...
    override fun onConnectionSuspended(p0: Int)
    {
        Log.i("", "Connection Suspended")
        mGoogleApiClient.connect()
    }
    //--------------------------------------------------------------
    //
    override fun onConnectionFailed(connectionResult: ConnectionResult)
    {
        Log.i("", "Connection failed. Error: " + connectionResult.getErrorCode());
    }
    //--------------------------------------------------------------
    //
    override fun onLocationChanged(location: Location)
    {
        var msg = "Updated Location: Latitude " + location.longitude.toString() + location.longitude;
        mLocation = location
    }
    //--------------------------------------------------------------
    //
    override fun onConnected(p0: Bundle?)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        startLocationUpdates()

        var fusedLocationProviderClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient .getLastLocation().addOnSuccessListener(this, OnSuccessListener<Location> { location ->
            // Got last known location_info. In some rare situations this can be null.
            if (location != null)
            {
                // Logic to handle location_info object
                mLocation = location
            }
        })
    }
    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun initValue()
    {
        m_arrPlace = ArrayList<place>()//placelist
        m_arrItem = ArrayList()//img list
    }
    //--------------------------------------------------------------
    //
    fun recvIntentData()
    {
        var pIntent: Intent = intent

        if(pIntent == null)
            return

        if(pIntent.hasExtra(Constant.INTENT_DATA_GROUP_OBJECT))
            m_GroupInfo =  pIntent.extras.get(Constant.INTENT_DATA_GROUP_OBJECT) as group
    }
    //--------------------------------------------------------------
    //
    fun initLayout()
    {
        ly_NoData.visibility = View.GONE
        m_LayoutInflater = LayoutInflater.from(m_Context)

        //event..
        ly_SwipeRefresh.setOnRefreshListener(this)

        //horizontal rv
        m_HorizontalAdapter = HorizontalPlaceRvAdapter(m_Context!!, m_arrPlace!!, this)
        horizontalRecyclerView!!.adapter = m_HorizontalAdapter
        val pLayoutManager = LinearLayoutManager(m_Context, LinearLayoutManager.HORIZONTAL,false)
        horizontalRecyclerView!!.layoutManager = pLayoutManager

        //map expand
        ly_MapExpand.setOnClickListener(View.OnClickListener {
            ly_MapExpand.visibility = View.GONE
            ly_MapCollapse.visibility = View.VISIBLE

            //expand map..
            val params = mapFragment!!.getView()!!.getLayoutParams()
            params.height = 1500
            mapFragment!!.getView()!!.setLayoutParams(params)

            val horizontalParam = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            horizontalParam.addRule(RelativeLayout.BELOW, R.id.ly_Top)
            ly_SwipeRefresh.layoutParams = horizontalParam

            m_HorizontalAdapter!!.notifyDataSetChanged()
        })

        //map collapse
        ly_MapCollapse.setOnClickListener(View.OnClickListener {

            ly_MapExpand.visibility = View.VISIBLE
            ly_MapCollapse.visibility = View.GONE

            val params = mapFragment!!.getView()!!.getLayoutParams()
            params.height = 0
            mapFragment!!.getView()!!.setLayoutParams(params)

            val horizontalParam = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            horizontalParam.addRule(RelativeLayout.BELOW, R.id.ly_Top)
            ly_SwipeRefresh.layoutParams = horizontalParam

            m_HorizontalAdapter!!.notifyDataSetChanged()
        })

        fbtn_SaveLocation?.setOnClickListener(View.OnClickListener
        {
            var bPermissionVal:Boolean = checkPermissionLocation()
            if(bPermissionVal)
            {
                seveLocationDialog()
            }
            else
            {

            }
        })

        if(m_GroupInfo != null)
        {
            if(m_GroupInfo!!.name != null)
            {
                tv_GroupName.text = m_GroupInfo!!.name

                //group명변경
                ly_EditGroupName.setOnClickListener(View.OnClickListener
                {

                    //show dialog..
                    val pAlert = AlertDialog.Builder(this@ActGroupDetail).create()
                    pAlert.setTitle(m_Context!!.resources.getString(R.string.str_msg_8))
                    pAlert.setMessage(m_Context!!.resources.getString(R.string.str_msg_9))
                    var editName: EditText? = EditText(m_Context)
                    editName!!.hint = getString(R.string.str_msg_4)
                    pAlert.setView(editName)
                    pAlert.setButton(AlertDialog.BUTTON_POSITIVE, m_Context!!.resources.getString(R.string.str_ok),{
                        dialogInterface, i ->
                        var name:String = editName.text.toString()
                        editGroupName(name)
                        pAlert.dismiss()
                    })
                    pAlert.setButton(AlertDialog.BUTTON_NEGATIVE, m_Context!!.resources.getString(R.string.str_no),{
                        dialogInterface, i ->
                        pAlert.dismiss()
                    })
                    pAlert.show()


                })
            }
        }

        //getplacelist..
        getPlaceListProc()
    }
    //-------------------------------------------------------------
    //
    fun setRefresh()
    {
        initValue()
        if(m_HorizontalAdapter != null)
            m_HorizontalAdapter!!.clearData()

        ly_SwipeRefresh!!.setRefreshing(false)
        getPlaceListProc()
    }

    //-------------------------------------------------------------
    //
    fun setRefreshImgList()
    {
        initValue()
        if(m_Adapter != null)
            m_Adapter!!.clearData()

        m_arrItem!!.add(0, "header")//setHeader
        m_Adapter!!.addList(m_arrItem!!)
        m_Adapter!!.setPlaceInfo(m_PlaceInfo!!)

        ly_SwipeRefresh!!.setRefreshing(false)
        getImageListProc()
    }
    //-------------------------------------------------------------
    //
    fun getPlaceListProc()
    {
        m_PlaceInfo = null
        m_bRunning = true
        progressBar.visibility = View.VISIBLE
        //image list..
        var pQuery:Query?= null

        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE).orderByChild("group_key").equalTo(m_GroupInfo!!.group_key)//.limitToFirst(ReqBase.ITEM_COUNT)
        pQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                // A new message has been added
                // onChildAdded() will be called for each node at the first time

                val pInfo:place = dataSnapshot!!.getValue(place::class.java)!!
                if(m_PlaceInfo == null)
                    m_PlaceInfo = pInfo

                //m_strPlaceLastSeq = dataSnapshot!!.key
                pInfo.place_key = dataSnapshot!!.key
                m_HorizontalAdapter!!.addData(pInfo!!)
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
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if(dataSnapshot!!.exists())
                {


                }
                m_bRunning = false
                progressBar.visibility = View.GONE

                if(m_HorizontalAdapter!!.itemCount > 0)
                {
                    settingMapView()
                    settingImgListView()
                    ly_NoData.visibility = View.GONE
                }
                else
                {
                    tv_NoDataMsg.text = m_Context!!.resources.getString(R.string.str_msg_15)
                    ly_NoData.visibility = View.VISIBLE
                }

                if(m_PlaceInfo != null)
                    getImageListProc()
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }
    //-------------------------------------------------------------
    //
    fun settingMapView()
    {
        //map..
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        mapFragment!!.getMapAsync(mapFragment)
        val mArgs = Bundle()
        mArgs.putSerializable(Constant.INTENT_DATA_PLACE_LIST_OBJECT, m_arrPlace!!)
        mapFragment.arguments = mArgs
        mapFragment.setIfCallback(this)
    }
    //--------------------------------------------------------------
    //
    fun settingImgListView()
    {
        if(m_PlaceInfo == null)
            return

        m_arrItem!!.add(0, "header")//setHeader
        m_Adapter = PhotoRvAdapter(m_Context!!, m_PlaceInfo!!, m_arrItem!!, this, supportFragmentManager)
        recyclerView.adapter = m_Adapter

        var nSpanCnt = 1
        /*
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)//landspace mode.
            nSpanCnt = 4
        */

        val pLayoutManager = GridLayoutManager(m_Context, nSpanCnt)
        recyclerView!!.layoutManager = pLayoutManager
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int)
            {
                val visibleItemCount = pLayoutManager.childCount
                val totalItemCount = pLayoutManager.itemCount
                val firstVisible = pLayoutManager.findFirstVisibleItemPosition()

                if(!m_bRunning!! && (visibleItemCount + firstVisible) >= totalItemCount)//더보기..
                {
                    // Call your API to load more items
                    //if(!m_bFinish!!)
                    //getImageListProc()
                }
            }
        })
    }
    //--------------------------------------------------------------
    //
    fun seveLocationDialog()
    {
        var bCheckLocation:Boolean = checkLocation()

        if(bCheckLocation)
        {
            var pInfo:place = place(m_GroupInfo!!.user_key!!, "", m_GroupInfo!!.group_key!!, "", String.format("%s",mLocation.latitude), String.format("%s",mLocation.longitude))
            showPlaceInputDialog(pInfo)
        }
    }
    //--------------------------------------------------------------
    //
    fun showPlaceInputDialog(pInfo:place)
    {
        if(pInfo == null)
            return

        val builder = android.support.v7.app.AlertDialog.Builder(m_Context!!)
        builder.setMessage(getString(R.string.str_msg_3))
        var editName: EditText? = EditText(m_Context)
        editName!!.hint = getString(R.string.str_msg_4)
        builder.setView(editName)
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            pInfo!!.name = editName.text.toString()

            var pDbRef:DatabaseReference? = null
            pDbRef =  m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!.push()//insert..
            pDbRef!!.setValue(pInfo!!)//insert
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot?)
                {
                    if (dataSnapshot!!.exists())
                    {
                        pInfo!!.place_key = dataSnapshot!!.key
                        m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!.child(dataSnapshot!!.key).setValue(pInfo)//update..
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

        val dialog: android.support.v7.app.AlertDialog = builder.create()
        dialog.show()
    }


    //-------------------------------------------------------------
    //
    fun getImageListProc()
    {
        m_bRunning = true
        progressBar.visibility = View.VISIBLE
        //image list..
        var pQuery:Query?= null

        //if(m_strImgLastSeq != null)
        //pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child("place_key").startAt(m_PlaceInfo!!.place_key).limitToFirst(ReqBase.ITEM_COUNT)
        //else
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child(m_PlaceInfo!!.place_key).child("img_list").orderByKey()//.limitToFirst(ReqBase.ITEM_COUNT)

        pQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                // A new message has been added
                // onChildAdded() will be called for each node at the first time
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
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if(dataSnapshot!!.exists())
                {
                    val children = dataSnapshot!!.children
                    children.forEach {

                        //if(m_strImgLastSeq != null)
                        //{
                        //if(!m_strImgLastSeq.equals(it!!.key))
                        //{
                        //m_strImgLastSeq = it!!.key

                        var strUrl:String = it.getValue(String::class.java)!!
                        m_Adapter!!.addItem(strUrl)
                        //}
                        //else//not add same key..
                        //{
                        //m_bFinish = true//get lastitem detect
                        //}
                        //}
                        //else
                        //{
                        //m_strImgLastSeq = it!!.key

                        //var strUrl:String = it.getValue(String::class.java)!!
                        //m_Adapter!!.addItem(strUrl)
                        //}
                    }
                }
                else
                {
                    m_bFinish = true//
                }

                if(m_Adapter!!.itemCount > 1)//1 : header..
                {
                    ly_NoData.visibility = View.GONE
                }
                else
                {
                    tv_NoDataMsg.text = m_Context!!.resources.getString(R.string.str_msg_14)
                    ly_NoData.visibility = View.VISIBLE
                }
                m_bRunning = false
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }
    //-------------------------------------------------------------
    //
    private fun checkPermissionWriteStorage()
    {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            permissionWriteStorege()
        }
        else
        {
            selectImageInAlbum()
        }
    }
    //-------------------------------------------------------------
    //
    private fun checkPermissionCamera()
    {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if(permission != PackageManager.PERMISSION_GRANTED)
        {
            permissionCamerra()
        }
        else
        {
            takePhoto()
        }
    }
    //-------------------------------------------------------------
    //
    private fun permissionWriteStorege()
    {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_SELECT_IMAGE_IN_ALBUM)
    }
    //-------------------------------------------------------------
    //
    private fun permissionCamerra()
    {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_TAKE_PHOTO)
    }
    //--------------------------------------------------------------
    //
    private fun checkPermissionLocation():Boolean
    {
        var bResult:Boolean = false

        val permissionCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val permissionFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()

        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED )//
        {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if(permissionFineLocation != PackageManager.PERMISSION_GRANTED)
        {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!listPermissionsNeeded.isEmpty())//
        {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), Constant.REQUEST_ID_MULTIPLE_PERMISSIONS)
        }
        else
        {
            if(mLocation.latitude <= 0 || mLocation.longitude <= 0)
            {
                var fusedLocationProviderClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient .getLastLocation().addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                    // Got last known location_info. In some rare situations this can be null.
                    if (location != null)
                    {
                        // Logic to handle location_info object
                        mLocation = location
                    }
                })
            }
            else
            {

            }

            bResult = true
        }
        return bResult
    }
    //--------------------------------------------------------------
    //
    private fun checkLocation(): Boolean
    {
        if(!isLocationEnabled())
            showAlert()

        return isLocationEnabled()
    }
    //--------------------------------------------------------------
    //
    private fun isLocationEnabled(): Boolean
    {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    //--------------------------------------------------------------
    //
    protected fun startLocationUpdates()
    {
        // Create the location_info request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location_info updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }

        LocationServices.getFusedLocationProviderClient(applicationContext!!).requestLocationUpdates(mLocationRequest, locationCallback, null)
    }
    //--------------------------------------------------------------
    //
    private fun showAlert()
    {
        val dialog = android.support.v7.app.AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt -> })
        dialog.show()
    }
    //-------------------------------------------------------------
    //add local image filename
    /*
    fun addPhoto()
    {

    }
    */
    //-------------------------------------------------------------
    //
    fun selectImageInAlbum()
    {
        val pIntent = Intent(Intent.ACTION_GET_CONTENT)
        pIntent.type = "image/*"
        if (pIntent.resolveActivity(packageManager) != null)
        {
            startActivityForResult(pIntent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }
    //-------------------------------------------------------------
    //
    fun takePhoto()
    {
        val pIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        var timestamp:Long = System.currentTimeMillis()
        var fileName:String = String.format("%s_%s",timestamp, "img")
        val photo = File((Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY, fileName)

        pIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(m_Context!!, "com.midas.secretplace.fileprovider", photo))
        imageUri = FileProvider.getUriForFile(m_Context!!, "com.midas.secretplace.fileprovider", photo)

        if (pIntent.resolveActivity(packageManager) != null)
        {
            startActivityForResult(pIntent, REQUEST_TAKE_PHOTO)
        }
    }
    //-------------------------------------------------------------
    //
    fun editGroupName(strName:String)
    {
        m_GroupInfo!!.name = strName

        //update
        var pDbRef: DatabaseReference? = null
        pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP).child(m_GroupInfo!!.group_key)
        pDbRef!!.setValue(m_GroupInfo!!)

        pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if (dataSnapshot!!.exists())
                {
                    val pInfo: group = dataSnapshot!!.getValue(group::class.java)!!
                    if(pInfo != null)
                    {
                        tv_GroupName!!.text = pInfo!!.name
                        m_GroupInfo = pInfo
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }

    //-------------------------------------------------------------
    //
    fun saveImage(myBitmap: Bitmap):String
    {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val wallpaperDirectory = File((Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        // have the object build the directory structure, if needed.
        Log.d("fee",wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists())
        {
            wallpaperDirectory.mkdirs()
        }

        try
        {
            Log.d("heel",wallpaperDirectory.toString())
            val pFile = File(wallpaperDirectory, ((Calendar.getInstance().getTimeInMillis()).toString() + ".jpg"))
            pFile.createNewFile()
            val fo = FileOutputStream(pFile)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this, arrayOf(pFile.getPath()), arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + pFile.getAbsolutePath())
            return pFile.getAbsolutePath()
        }
        catch (e1: IOException)
        {
            e1.printStackTrace()
        }
        return ""
    }
    //-------------------------------------------------------------
    //
    fun editContentProc(strName:String)
    {
        //update
        m_PlaceInfo!!.name = strName

        var pDbRef: DatabaseReference? = null
        pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE).child(m_PlaceInfo!!.place_key)
        pDbRef!!.setValue(m_PlaceInfo!!)

        pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if (dataSnapshot!!.exists())
                {
                    //setRefresh()
                }
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }
    /************************* listener *************************/
    //--------------------------------------------------------------------
    //
    var locationCallback = object : LocationCallback()
    {
        override fun onLocationResult(locationResult: LocationResult?)
        {
            locationResult ?: return
            for (location in locationResult.locations)
            {
                /*
                if(m_DistanceInfo != null)
                {
                    var pInfo:location_info = location_info(location.latitude.toString(), location.longitude.toString())
                    m_DistanceInfo!!.location_list!!.add(pInfo)
                }
                */
            }
        }
    }

    /************************* callback function *************************/
    //-----------------------------------------------------
    //Swipe Refresh Listener
    override fun onRefresh()
    {
        setRefresh()
    }
    //-----------------------------------------------------
    //horizontal place adapter ifCallback
    override fun selectPlaceItem(pInfo: place)
    {
        m_PlaceInfo = pInfo

        //refresh img list..
        setRefreshImgList()
    }
    //-----------------------------------------------------
    //horizontal place adapter ifCallback
    override fun deletePlaceItem(pInfo: place)
    {
        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!.child(pInfo.place_key)//where
        pDbRef!!.removeValue()

        pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child(pInfo.place_key)//where
        pDbRef!!.removeValue()

        setRefresh()
    }
    //-----------------------------------------------------
    //adapter ifCallback
    override fun addPhoto()
    {
        //show dialog..
        val pAlert = AlertDialog.Builder(this@ActGroupDetail).create()
        pAlert.setTitle(m_Context!!.resources.getString(R.string.str_msg_12))
        pAlert.setMessage(m_Context!!.resources.getString(R.string.str_msg_9))
        pAlert.setButton(AlertDialog.BUTTON_POSITIVE, m_Context!!.resources.getString(R.string.str_msg_11),{
            dialogInterface, i ->
            checkPermissionWriteStorage();
            pAlert.dismiss();
        })
        pAlert.setButton(AlertDialog.BUTTON_NEGATIVE, m_Context!!.resources.getString(R.string.str_msg_10),{
            dialogInterface, i ->
            checkPermissionCamera();
            pAlert.dismiss();
        })
        pAlert.show()
    }
    //-----------------------------------------------------
    //adapter ifCallback
    override fun editContent()
    {
        //show dialog..
        val pAlert = AlertDialog.Builder(this@ActGroupDetail).create()
        pAlert.setTitle(m_Context!!.resources.getString(R.string.str_msg_13))
        pAlert.setMessage(m_Context!!.resources.getString(R.string.str_msg_9))
        var editName: EditText? = EditText(m_Context)
        editName!!.hint = getString(R.string.str_msg_4)
        pAlert.setView(editName)
        pAlert.setButton(AlertDialog.BUTTON_POSITIVE, m_Context!!.resources.getString(R.string.str_ok),{
            dialogInterface, i ->
            var name:String = editName.text.toString()
            editContentProc(name)
            pAlert.dismiss()
        })
        pAlert.setButton(AlertDialog.BUTTON_NEGATIVE, m_Context!!.resources.getString(R.string.str_no),{
            dialogInterface, i ->
            pAlert.dismiss()
        })
        pAlert.show()
    }
    /*********************** interface ***********************/

}
