
package com.midas.secretplace.ui.act

import android.Manifest
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
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.midas.mytimeline.ui.adapter.PlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import kotlinx.android.synthetic.main.act_main.*
import kotlinx.android.synthetic.main.ly_main.*


class ActMain : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, SwipeRefreshLayout.OnRefreshListener
{

    /*********************** Define ***********************/

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
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    lateinit var locationManager: LocationManager
    /*********************** Controller ***********************/
    private var m_btn_SaveLocation: Button?=null
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
        mLocation = Location("dummyProvider");
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
        m_btn_SaveLocation?.setOnClickListener(onClickListener)

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
                saveLocation()
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
    }
    //--------------------------------------------------------------
    //
    fun settingView()
    {
        getPlaceList()
    }


    //--------------------------------------------------------------
    //


    //--------------------------------------------------------------
    //
    fun saveLocation()
    {
        if(checkLocation())
        {
            onLocationChanged(mLocation)
            var lat:Double = mLocation.latitude
            var lng:Double = mLocation.longitude

            var pInfo:place = place("", String.format("%s",lat), String.format("%s",lng))
            showPlaceInputDialog(pInfo)
        }
    }
    //--------------------------------------------------------------
    //
    fun getPlaceList()
    {
        m_App!!.showLoadingDialog(ly_LoadingDialog!!)

        if(m_strSeq == null)
            m_strSeq = ""

        var pQuery:Query = m_App!!.m_FirebaseDbCtrl!!.getPlaceList(m_strSeq!!)
        pQuery!!.addChildEventListener(getPlaceListener)
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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
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
            var pDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setPlaceItem(pInfo)
            pDbRef.addValueEventListener(addPlaceListener)
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

        getPlaceList()
    }

    /************************* listener *************************/
    //--------------------------------------------------------------
    //
    val getPlaceListener = object : ChildEventListener
    {
        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            // A new message has been added
            // onChildAdded() will be called for each node at the first time
            m_strSeq = dataSnapshot!!.key
            val pInfo: place = dataSnapshot!!.getValue(place::class.java)!!
            m_Adapter!!.addData(pInfo)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            //Log.e(TAG, "onChildChanged:" + dataSnapshot!!.key)

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
    }
    //--------------------------------------------------------------
    //
    val addPlaceListener = object : ValueEventListener
    {
        override fun onDataChange(dataSnapshot: DataSnapshot)
        {
            if (dataSnapshot.exists())
            {
                m_strSeq = dataSnapshot!!.key
                val pInfo: place = dataSnapshot!!.getValue(place::class.java)!!
                m_Adapter!!.addData(pInfo)
            }
        }

        override fun onCancelled(databaseError: DatabaseError)
        {
            // Failed to read value
        }
    }

    /*********************** listener ***********************/
    //-------------------------------------------------------------
    //
    val onClickListener = View.OnClickListener { view ->

        when (view.getId())
        {
            R.id.btn_SaveLocation -> checkPermissionLocation()
        }
    }
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
}
