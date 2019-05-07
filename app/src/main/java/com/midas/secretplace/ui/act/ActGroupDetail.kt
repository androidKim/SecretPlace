
package com.midas.secretplace.ui.act

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.midas.mytimeline.ui.adapter.PlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.group
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.custom.dlg_photo_view
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_group_detail.*
import pl.kitek.rvswipetodelete.SwipeToDeleteCallback
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


/*
한 그룹의 매장리스트
 */
class ActGroupDetail : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,PlaceRvAdapter.ifCallback , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{
    //extention functions..
    inline fun Activity.showPhotoViewDialog(func: dlg_photo_view.() -> Unit): AlertDialog =
            dlg_photo_view(this).apply {
                func()
            }.create()

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
    var m_RequestManager: RequestManager? = null
    var m_GroupInfo:group? = group()
    var m_arrPlace:ArrayList<place>? = ArrayList()//group의 place list(horizontal listview)
    var m_LayoutInflater:LayoutInflater? = null
    var m_PlaceAdapter:PlaceRvAdapter? = null
    var selectedImage: Uri? = null
    var m_bFinish:Boolean? = false
    var m_bModify:Boolean? = false//변경이력여부
    /*********************** Controller ***********************/

    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        m_Context = this
        m_RequestManager = Glide.with(this)
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActGroupDetail)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_group_detail)

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
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        super.onStart()
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect()
    }
    //--------------------------------------------------------------
    //
    override fun onStop()
    {
        super.onStop()
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect()
    }

    //---------------------------------------------------------------------------------------------------
    //
    override fun onBackPressed()
    {
        if(m_bModify!!)
        {
            setResult(Constant.FOR_RESULT_IS_REFRESH)
            finish()
        }
        else
        {
            super.onBackPressed()
        }
    }
    //---------------------------------------------------------------------------------------------------
    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)//Intent?  <-- null이 올수도있다
    {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Constant.FOR_RESULT_IS_REFRESH)
        {
            setRefresh()
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
                // Fill with actual results from vm_user
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
    /*********************** Menu Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        var menuItem1: MenuItem = menu!!.findItem(R.id.action_share).setVisible(false)
        var menuItem2: MenuItem = menu!!.findItem(R.id.share_location).setVisible(false)
        var menuItem3: MenuItem = menu!!.findItem(R.id.show_map).setVisible(false)
        var menuItem4: MenuItem = menu!!.findItem(R.id.edit).setVisible(true)
        var menuItem5: MenuItem = menu!!.findItem(R.id.add_photo).setVisible(false)

        return super.onCreateOptionsMenu(menu)
    }
    //--------------------------------------------------------------
    //
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.edit -> {
                showEditDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //--------------------------------------------------------------
    //
    private fun showEditDialog(){
        //show dialog..
        val pAlert = AlertDialog.Builder(this@ActGroupDetail).create()
        pAlert.setTitle("["+m_GroupInfo!!.name+"]"+m_Context!!.resources.getString(R.string.str_msg_16))
        pAlert.setMessage(m_Context!!.resources.getString(R.string.str_msg_17))
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
        toolbar.title = ""
        setSupportActionBar(toolbar)//enable app bar
        var actionBar: ActionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        toolbar.setNavigationOnClickListener { view -> onBackPressed() }
        var strTheme:String = m_App!!.m_SpCtrl!!.getSpTheme()!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setToolbarBackgroundColor(m_Context!!, this.toolbar, strTheme!!)
        }


        ly_NoData.visibility = View.GONE

        m_LayoutInflater = LayoutInflater.from(m_Context)
        //event..
        ly_SwipeRefresh.setOnRefreshListener(this)

        m_PlaceAdapter = PlaceRvAdapter(m_Context!!, m_RequestManager!!, m_arrPlace!!, this)
        recyclerView!!.adapter = m_PlaceAdapter

        var nSpanCnt = 1
        val pLayoutManager = GridLayoutManager(m_Context, nSpanCnt)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = pLayoutManager

        //swipe remove listener..
        val swipeHandler = object : SwipeToDeleteCallback(m_Context!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //val adapter = recyclerView.adapter as SimpleAdapter
                m_PlaceAdapter!!.removeAt(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)


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
                //setTitle..
                toolbar.title = m_GroupInfo!!.name
            }
        }

        getPlaceListProc()
    }
    //-------------------------------------------------------------
    //
    fun setRefresh()
    {
        initValue()
        ly_SwipeRefresh!!.setRefreshing(false)

        m_PlaceAdapter = PlaceRvAdapter(m_Context!!, m_RequestManager!!, m_arrPlace!!, this)
        recyclerView!!.adapter = m_PlaceAdapter

        var nSpanCnt = 1
        val pLayoutManager = GridLayoutManager(m_Context, nSpanCnt)
        recyclerView!!.setHasFixedSize(true)

        recyclerView!!.layoutManager = pLayoutManager
        getPlaceListProc()
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

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
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
                        var desertRef = storageRef.reference.child(fileNm)
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

    //-------------------------------------------------------------
    //
    fun getPlaceListProc()
    {
        progressBar.visibility = View.VISIBLE
        //image list..
        var pQuery:Query = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .child(m_GroupInfo!!.group_key)
                .child("place_list")
                .orderByKey()

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if(dataSnapshot!!.exists())
                {
                    val children = dataSnapshot!!.children
                    children.forEach {
                        val pInfo:place = it!!.getValue(place::class.java)!!
                        m_PlaceAdapter!!.addData(pInfo)
                    }
                    m_PlaceAdapter!!.reverseList()
                }
                else
                {
                    //ly_Empty.visibility = View.VISIBLE

                }
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
        onLocationChanged(mLocation)
        var bCheckLocation:Boolean = checkLocation()

        if(bCheckLocation)
        {
            var address:String? = Util.getAddress(m_Context!!, mLocation.latitude, mLocation.longitude)
            var pInfo:place = place(m_GroupInfo!!.user_key!!, "", m_GroupInfo!!.group_key!!, "", String.format("%s",mLocation.latitude), String.format("%s",mLocation.longitude), "", address!!, "", "N")
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
            pDbRef =  m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)!!
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                    .child(m_GroupInfo!!.group_key)
                    .child("place_list")
                    .push()//insert..

            pDbRef!!.setValue(pInfo!!)//insert
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot?)
                {
                    if (dataSnapshot!!.exists())
                    {
                        m_bModify = true

                        pInfo!!.place_key = dataSnapshot!!.key
                        m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)!!
                                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                                .child(m_GroupInfo!!.group_key)
                                .child("place_list")
                                .child(dataSnapshot!!.key).setValue(pInfo)//update..

                        m_PlaceAdapter!!.addFirst(pInfo)
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
            bResult = false
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
                    else
                    {

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
    //
    fun editGroupName(strName:String)
    {
        m_GroupInfo!!.name = strName

        //update
        var pDbRef: DatabaseReference? = null
        pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .child(m_GroupInfo!!.group_key)

        pDbRef!!.setValue(m_GroupInfo!!)

        pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if (dataSnapshot!!.exists())
                {
                    m_bModify = true

                    val pInfo: group = dataSnapshot!!.getValue(group::class.java)!!
                    if(pInfo != null)
                    {
                        toolbar!!.title = pInfo!!.name
                        m_GroupInfo = pInfo
                    }
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
    //listAdapter callback
    override fun checkPermission(): Boolean {
        return true
    }
    //----------------------------------------------------------------------
    //listAdapter callback
    override fun deleteProc(pInfo: place, position:Int)
    {
        val builder = AlertDialog.Builder(this@ActGroupDetail)
        builder.setCancelable(false)
        builder.setMessage(getString(R.string.msg_question_delete))
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->

            m_PlaceAdapter!!.removeRow(position)

            //group place list remove
            var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)!!
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                    .child(m_GroupInfo!!.group_key)
                    .child("place_list")
                    .child(pInfo.place_key)

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

        builder.setNegativeButton(getString(R.string.str_no)){dialog,which ->
            m_PlaceAdapter!!.notifyItemChanged(position)
        }

        builder.setNeutralButton(getString(R.string.str_cancel)){_,_ ->
            m_PlaceAdapter!!.notifyItemChanged(position)
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()



    }
    //----------------------------------------------------------------------
    //listAdapter callback
    override fun moveDetailActivity(pInfo: place)
    {
        var pIntent = Intent(m_Context, ActGroupPlaceDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_PLACE_OBJECT, pInfo as Serializable)
        startActivityForResult(pIntent, 0)
    }
    /*********************** interface ***********************/

}
