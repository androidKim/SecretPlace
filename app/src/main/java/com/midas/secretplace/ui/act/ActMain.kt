
package com.midas.secretplace.ui.act

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.multidex.MultiDex
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.midas.mytimeline.ui.adapter.PlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.distance
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.structure.core.user
import com.midas.secretplace.ui.MyApp
import kotlinx.android.synthetic.main.act_main.*
import kotlinx.android.synthetic.main.ly_main.*


class ActMain : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, SwipeRefreshLayout.OnRefreshListener
{

    /*********************** Define ***********************/
    val TYPE_SAVE_PLACE:Int = 1//
    val TYPE_SAVE_DISTANCE:Int = 2
    /*********************** Member ***********************/
    private var m_App: MyApp? = null
    private var m_Context: Context? = null

    private var m_arrPlace:ArrayList<place>? = null
    var m_Adapter:PlaceRvAdapter? = null
    var m_strSeq:String? = null
    //location..
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mLocationManager: LocationManager? = null
    lateinit var mLocation: Location
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = ((5000).toLong())
    private val FASTEST_INTERVAL: Long = 5000
    /*
    .setInterval(15000) // 15 seconds
    .setFastestInterval(5000) // 5000ms
    기기는 15초마다 혹은 그보다 더 빠르거나 느리게 위치를 수집하지만 5초보다 빠르게 수집하진 않을것입니다
     */

    lateinit var locationManager: LocationManager
    //
    private var m_DistanceInfo:distance? = null
    private var m_nSaveType:Int = 0
    private var m_bRunning:Boolean = false
    private var m_bPagingFinish:Boolean = false
    /*********************** Controller ***********************/
    private var m_btn_SaveLocation: Button?=null
    private var m_iv_Profile:ImageView? = null
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        m_Context = this
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActMain)

        MultiDex.install(this)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        initValue()
        recvIntentData()
        initLayout()
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        super.onStart();
        if (mGoogleApiClient != null)
        {
            mGoogleApiClient.connect()
        }
    }
    //--------------------------------------------------------------
    //
    override fun onStop()
    {
        super.onStop();
        if (mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect()
        }
    }
    //--------------------------------------------------------------
    //
    override fun onBackPressed()
    {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
        {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else
        {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray)
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
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null)
                                    {
                                        // Logic to handle location object
                                        mLocation = location
                                    }
                                })
                            }
                            else
                            {
                                saveLocation()
                            }
                        }
                    }
                }
            }
        }

    }
    /*********************** Location Function ***********************/
    //--------------------------------------------------------------
    //location...
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
        Toast.makeText(m_Context, "Update", Toast.LENGTH_SHORT).show()
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
            // Got last known location. In some rare situations this can be null.
            if (location != null)
            {
                // Logic to handle location object
                mLocation = location
            }
        })
    }
    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun initValue()
    {
        m_arrPlace = ArrayList<place>()
        mLocation = Location("dummyProvider")
    }
    //--------------------------------------------------------------
    //
    fun recvIntentData()
    {

    }
    //--------------------------------------------------------------
    //
    fun initLayout()
    {
        //event..
        ly_SwipeRefresh.setOnRefreshListener(this)

        m_btn_SaveLocation = findViewById(R.id.btn_SaveLocation)

        //listener..
        m_btn_SaveLocation?.setOnClickListener(View.OnClickListener {
            m_nSaveType = TYPE_SAVE_PLACE
            checkPermissionLocation()
        })
        btn_SaveDistance.setOnClickListener(View.OnClickListener {
            m_nSaveType = TYPE_SAVE_DISTANCE
            checkPermissionLocation()
        })

        settingDrawer()
        settingView()
        settingRecyclerView()
    }

    //--------------------------------------------------------------
    //
    private fun checkPermissionLocation()
    {
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
                    // Got last known location. In some rare situations this can be null.
                    if (location != null)
                    {
                        // Logic to handle location object
                        mLocation = location
                    }
                })
            }
            else
            {
                if(m_nSaveType == TYPE_SAVE_PLACE)
                    saveLocation()
                else if(m_nSaveType == TYPE_SAVE_DISTANCE)
                    saveDistance()
            }
        }
    }

    //--------------------------------------------------------------
    //
    fun settingRecyclerView()
    {
        m_Adapter = PlaceRvAdapter(this, m_arrPlace!!)
        recyclerView.adapter = m_Adapter

        var nSpanCnt = 3
        /*
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)//landspace mode..
        {
            nSpanCnt = 4
        }
        */

        val pLayoutManager = GridLayoutManager(this, nSpanCnt)
        recyclerView.layoutManager = pLayoutManager
        recyclerView.setHasFixedSize(true)

        recyclerView.layoutManager = pLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener()
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
                        getPlaceList(m_strSeq!!)
                }
            }
        })
    }
    //--------------------------------------------------------------
    //
    fun settingDrawer()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        val toggle = ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        navigation_view.setNavigationItemSelectedListener(this)

        //header..
        var v_Header:View = navigation_view!!.getHeaderView(0)
        m_iv_Profile = v_Header.findViewById(R.id.iv_Profile)
    }
    //--------------------------------------------------------------
    //
    fun settingView()
    {
        getUserData()
        getPlaceList("")
    }
    //--------------------------------------------------------------
    //
    fun getUserData()
    {
        var userKey:String? = m_App!!.m_SpCtrl!!.getSpUserKey()
        var pDbRef:DatabaseReference? = m_App!!.m_FirebaseDbCtrl!!.getUserDbRef().child(userKey)//where
        pDbRef!!.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                val pInfo: user = dataSnapshot!!.getValue(user::class.java)!!
                if(pInfo != null)
                {
                    settingUserView(pInfo)
                }
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }
    //--------------------------------------------------------------
    //
    fun settingUserView(pInfo:user)
    {
        if(pInfo == null)
            return

        if(pInfo.img_url != null)
        {
            if(pInfo.img_url!!.length > 0)
                Glide.with(this).load(pInfo.img_url).into(m_iv_Profile)
        }
    }
    //--------------------------------------------------------------
    //
    fun saveLocation()
    {
        if(checkLocation())
        {
            onLocationChanged(mLocation)
            var lat:Double = mLocation.latitude
            var lng:Double = mLocation.longitude

            var userKey:String? = m_App!!.m_SpCtrl!!.getSpUserKey()//G292919

            var pInfo:place = place(userKey!!, "", String.format("%s",lat), String.format("%s",lng))
            showPlaceInputDialog(pInfo)
        }
    }
    //--------------------------------------------------------------
    //
    fun saveDistance()
    {
        if(checkLocation())
        {
            showDistanceInputDialog()
        }
    }
    //--------------------------------------------------------------
    //
    fun getPlaceList(seq:String)
    {
        m_bRunning = true
        m_App!!.showLoadingDialog(ly_LoadingDialog)

        var pQuery:Query = m_App!!.m_FirebaseDbCtrl!!.getPlaceList(seq!!)
        //pQuery!!.addListenerForSingleValueEvent(listenerForSingleValueEvent)
        //pQuery!!.addChildEventListener(childEventListener)
        pQuery.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                // A new message has been added
                // onChildAdded() will be called for each node at the first time
                if(!m_strSeq.equals(dataSnapshot!!.key))
                {
                    val pInfo: place = dataSnapshot!!.getValue(place::class.java)!!
                    if(pInfo.user_fk.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))
                    {
                        m_strSeq = dataSnapshot!!.key
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

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener{
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
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return
        }
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)

        //LocationRequest var1, LocationCallback var2, @Nullable Looper var3
        LocationServices.getFusedLocationProviderClient(m_Context!!).requestLocationUpdates(mLocationRequest, locationCallback, null)
    }

    //--------------------------------------------------------------------
    //
    var locationCallback = object : LocationCallback()
    {
    override fun onLocationResult(locationResult: LocationResult?)
    {
        locationResult ?: return
        for (location in locationResult.locations)
        {
            Log.i("longitude", location.longitude.toString())
            Log.i("latitude", location.latitude.toString())
        }
    }
}


    //--------------------------------------------------------------
    //
    fun showPlaceInputDialog(pInfo:place)
    {
        if(pInfo == null)
            return

        val builder = AlertDialog.Builder(this@ActMain)
        builder.setMessage(getString(R.string.str_msg_3))
        var editName: EditText? = EditText(m_Context)
        editName!!.hint = getString(R.string.str_msg_4)
        builder.setView(editName)
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            pInfo.name = editName.text.toString()
            var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setPlaceInfo(pInfo)
            //pDbRef.addValueEventListener(addPlaceListener)
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot?)
                {
                    if (dataSnapshot!!.exists())
                    {
                        m_strSeq = dataSnapshot!!.key
                        val pInfo: place = dataSnapshot!!.getValue(place::class.java)!!
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
    @SuppressLint("MissingPermission")
//--------------------------------------------------------------
    //
    fun showDistanceInputDialog()
    {
        val builder = AlertDialog.Builder(this@ActMain)
        builder.setMessage(getString(R.string.str_msg_5))
        //custom view..
        var pLayout:LinearLayout? = LinearLayout(m_Context)
        pLayout!!.orientation = LinearLayout.VERTICAL

        var editTime: EditText? = EditText(m_Context)
        editTime!!.inputType = InputType.TYPE_CLASS_NUMBER
        editTime!!.hint = getString(R.string.str_msg_6)
        editTime!!.limitLength(2)

        pLayout.addView(editTime)

        var editName: EditText? = EditText(m_Context)
        editName!!.hint = getString(R.string.str_msg_4)
        pLayout.addView(editName)

        builder.setView(pLayout)

        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            m_DistanceInfo = distance()//init
            m_DistanceInfo!!.name = editName.text.toString()
            /*
            var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setPlaceItem(pInfo)
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot?)
                {
                    if (dataSnapshot!!.exists())
                    {
                        m_strSeq = dataSnapshot!!.key
                        val pInfo: place = dataSnapshot!!.getValue(place::class.java)!!
                    }
                }

                override fun onCancelled(p0: DatabaseError?)
                {

                }
             })
             */
            var minute:String = editTime.text.toString()
            var minTime:Long = minute.toLong() * 1000 * 60
            //mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, 0f, locationListener)

            mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            mLocationRequest!!.setInterval(minTime)
            mLocationRequest!!.setFastestInterval(minTime) // Ever
            LocationServices.getFusedLocationProviderClient(m_Context!!).requestLocationUpdates(mLocationRequest, locationCallback, null)
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
    fun showLogoutDialog()
    {
        val builder = AlertDialog.Builder(this@ActMain)
        builder.setMessage(getString(R.string.str_msg_2))
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            m_App!!.logoutProc(m_Context as ActMain)
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
    private fun showAlert()
    {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt -> })
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

        ly_SwipeRefresh.setRefreshing(false);

        getPlaceList("")
    }

    /*********************** listener ***********************/
    //--------------------------------------------------------------
    //
    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        var result = false

        when(item.itemId)
        {
            R.id.logout ->
            {
                showLogoutDialog()
            }

            R.id.menu_option_one ->
            {
                Toast.makeText(this@ActMain,"Opcion Uno Seleccionada",Toast.LENGTH_LONG).show()
                result = true
            }

            R.id.menu_option_two ->
            {
                Toast.makeText(this@ActMain,"Opcion Dos Seleccionada", Toast.LENGTH_LONG).show()
                result = true
            }

            R.id.menu_option_three ->
            {
                Toast.makeText(this@ActMain,"Opcion Tres Seleccionada",Toast.LENGTH_LONG).show()
                result = true
            }

            R.id.other_menu_option_one ->
            {
                Toast.makeText(this@ActMain,"Otra Opcion Uno Seleccionada",Toast.LENGTH_LONG).show()
                result = true
            }

            R.id.other_menu_option_two ->
            {
                Toast.makeText(this@ActMain,"Otra Opcion Dos Seleccionada",Toast.LENGTH_LONG).show()
                result = true
            }

            R.id.other_menu_option_three ->
            {
                Toast.makeText(this@ActMain,"Otra Opcion Tres Seleccionada",Toast.LENGTH_LONG).show()
                result = true
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return result
    }

    //----------------------------------------------------------------------
    //
    override fun onRefresh()
    {
        setRefresh()
    }

    /*********************** util ***********************/
    //-----------------------------------------------------------------
    //editText max Length..
    fun EditText.limitLength(maxLength: Int)
    {
        filters = arrayOf(InputFilter.LengthFilter(maxLength))
    }
}
