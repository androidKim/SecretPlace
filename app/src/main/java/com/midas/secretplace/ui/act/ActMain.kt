
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
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.distance
import com.midas.secretplace.structure.core.location_info
import com.midas.secretplace.structure.core.user
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.adapter.MainPagerAdapter
import com.midas.secretplace.ui.frag.main.FrDistance
import com.midas.secretplace.ui.frag.main.FrPlace
import kotlinx.android.synthetic.main.act_main.*
import kotlinx.android.synthetic.main.ly_main.*


class ActMain : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        FrPlace.ifCallback, FrDistance.ifCallback
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App: MyApp? = null
    var m_Context: Context? = null

    //location_info..
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
    var m_DistanceInfo:distance? = null
    /*********************** Controller ***********************/
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
        Toast.makeText(m_Context, "onLocationChanged", Toast.LENGTH_SHORT).show()
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
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        if (viewPager != null)
        {
            val adapter = MainPagerAdapter(supportFragmentManager)
            viewPager.adapter = adapter
        }

        settingDrawerView()
        settingView()
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
                bResult = true
            }
        }


        return bResult
    }
    //--------------------------------------------------------------
    //
    fun settingDrawerView()
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
        getUserDataProc()
    }
    //--------------------------------------------------------------
    //
    fun getUserDataProc()
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

        LocationServices.getFusedLocationProviderClient(m_Context!!).requestLocationUpdates(mLocationRequest, locationCallback, null)
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

    //--------------------------------------------------------------------
    //distance save listener
    var locationCallback = object : LocationCallback()
    {
        override fun onLocationResult(locationResult: LocationResult?)
        {
            locationResult ?: return
            for (location in locationResult.locations)
            {
                if(m_DistanceInfo != null)
                {
                    var pInfo:location_info = location_info(location.latitude.toString(), location.longitude.toString())
                    m_DistanceInfo!!.location_list!!.add(pInfo)
                }
            }
        }
    }

    /*********************** Interface Callback ***********************/
    //--------------------------------------------------------------
    //frPlace, frDistance
    override fun checkPermission(): Boolean
    {
        var bResult:Boolean = false
        bResult = checkPermissionLocation()
        return bResult
    }
    //--------------------------------------------------------------
    //frPlace, frDistance
    override fun checkLocationInfo(): Boolean
    {
        var bResult:Boolean = false
        bResult = checkLocation()
        return bResult
    }
    //--------------------------------------------------------------
    //frPlace
    override fun getLocation(): Location
    {
        onLocationChanged(mLocation)
        var lat:Double = mLocation.latitude
        var lng:Double = mLocation.longitude

        return mLocation
    }
    //--------------------------------------------------------------
    //frDistance
    @SuppressLint("MissingPermission")
    override fun setLocationManagerInterval(nInterval: Long)
    {
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest!!.setInterval(nInterval)
        mLocationRequest!!.setFastestInterval(nInterval)
        LocationServices.getFusedLocationProviderClient(m_Context!!).requestLocationUpdates(mLocationRequest, locationCallback, null)
    }
    //--------------------------------------------------------------
    //frDistance
    override fun setDistanceInfo(pInfo: distance)
    {
        if(pInfo == null)
            return

        m_DistanceInfo = pInfo!!
    }
    //--------------------------------------------------------------
    //frDistance
    override fun getSavedDistanceInfo(): distance
    {
        return m_DistanceInfo!!
    }
    //--------------------------------------------------------------
    //frDistance
    override fun disableDistanceSave()
    {
        m_DistanceInfo = null
    }


    /*********************** util ***********************/

}
