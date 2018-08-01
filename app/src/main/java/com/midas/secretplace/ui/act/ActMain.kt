
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
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.midas.secretplace.R
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import kotlinx.android.synthetic.main.act_main.*
import kotlinx.android.synthetic.main.ly_main.*


class ActMain : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    private var m_App: MyApp? = null
    private var m_Context: Context? = null
    private var m_FirebaseDb:FirebaseDatabase ?= null
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

        checkLocation()

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
    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun initValue()
    {
        m_FirebaseDb = FirebaseDatabase.getInstance()
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
        m_btn_SaveLocation = this.findViewById(R.id.btn_SaveLocation)

        //listener..
        m_btn_SaveLocation?.setOnClickListener(onClickListener)

        settingDrawer()
        settingView()
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

    }
    //--------------------------------------------------------------
    //
    fun showLogoutDialog()
    {
        val builder = AlertDialog.Builder(this@ActMain)
        builder.setTitle(getString(R.string.str_msg_1))
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
    public fun saveLocation()
    {
        Toast.makeText(m_Context, "save lat lng", Toast.LENGTH_SHORT).show()

        var pDbRefList = this.m_FirebaseDb!!.getReference("place_list")

        var pInfo:place = place()
        pInfo.place("12387128937","222asdfasfdsaf2","safdsafasfasdf")

        var newRef:DatabaseReference  = pDbRefList!!.push()
        newRef.setValue(pInfo)


        var pDbRefResult = FirebaseDatabase.getInstance().getReference("place_list")
        pDbRefResult!!.addChildEventListener(childEventListener)

        onLocationChanged(mLocation)
    }


    var m_pArr:ArrayList<place> = ArrayList()
    val childEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?) {
            // A new message has been added
            // onChildAdded() will be called for each node at the first time

            val pInfo: place = dataSnapshot!!.getValue(place::class.java)!!
            m_pArr.add(pInfo)
            Toast.makeText(m_Context, "onDataChange", Toast.LENGTH_SHORT).show()
            //val message = dataSnapshot!!.getValue(Message::class.java)
            //messageList.add(message!!)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?) {
            //Log.e(TAG, "onChildChanged:" + dataSnapshot!!.key)

            // A message has changed
            //val message = dataSnapshot.getValue(Message::class.java)
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            //Log.e(TAG, "onChildRemoved:" + dataSnapshot!!.key)

            // A message has been removed
            //val message = dataSnapshot.getValue(Message::class.java)
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?) {
            //Log.e(TAG, "onChildMoved:" + dataSnapshot!!.key)

            // A message has changed position
            //val message = dataSnapshot.getValue(Message::class.java)
        }

        override fun onCancelled(databaseError: DatabaseError?) {
            //Log.e(TAG, "postMessages:onCancelled", databaseError!!.toException())
        }
    }


    //--------------------------------------------------------------
    //
    val messageListener = object : ValueEventListener
    {
        override fun onDataChange(dataSnapshot: DataSnapshot)
        {
            if (dataSnapshot.exists())
            {

                Toast.makeText(m_Context, "onDataChange", Toast.LENGTH_SHORT).show()
                // ...
            }
        }

        override fun onCancelled(databaseError: DatabaseError)
        {
            // Failed to read value
        }
    }




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
        txt_latitude.setText(""+location.latitude);
        txt_longitude.setText(""+location.longitude);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
                        mLocation = location;
                        txt_latitude.setText("" + mLocation.latitude)
                        txt_longitude.setText("" + mLocation.longitude)
                    }
                })
    }

    private fun checkLocation(): Boolean
    {
        if(!isLocationEnabled())
            showAlert();

        return isLocationEnabled();
    }

    private fun isLocationEnabled(): Boolean
    {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

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

    /*********************** listener ***********************/
    //-------------------------------------------------------------
    //
    val onClickListener = View.OnClickListener { view ->

        when (view.getId())
        {
            R.id.btn_SaveLocation -> saveLocation()
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
}
