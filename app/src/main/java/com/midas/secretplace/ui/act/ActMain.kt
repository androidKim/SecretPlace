
package com.midas.secretplace.ui.act

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.multidex.MultiDex
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.midas.mytimeline.ui.adapter.ThemeColorRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.service.MyJobService
import com.midas.secretplace.structure.core.*
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.adapter.MainPagerAdapter
import com.midas.secretplace.ui.custom.dlg_photo_filter
import com.midas.secretplace.ui.custom.dlg_theme_setting
import com.midas.secretplace.ui.frag.main.FrGroup
import com.midas.secretplace.ui.frag.main.FrPlace
import com.midas.secretplace.ui.setting.ActSetting
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_main.*
import kotlinx.android.synthetic.main.dlg_photo_filter.view.*
import kotlinx.android.synthetic.main.dlg_theme_setting.view.*
import kotlinx.android.synthetic.main.ly_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class ActMain:AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ThemeColorRvAdapter.ifCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        FrPlace.ifCallback, FrGroup.ifCallback
{

    //----------------------------------------------------------
    //theme
    inline fun Activity.showThemeSettingDialog(func: dlg_theme_setting.() -> Unit): AlertDialog =
            dlg_theme_setting(this).apply {
                func()
            }.create()

    inline fun Fragment.showThemeSettingDialog(func: dlg_theme_setting.() -> Unit): AlertDialog =
            dlg_theme_setting(this.context!!).apply {
                func()
            }.create()


    //----------------------------------------------------------
    //photo filter
    inline fun Activity.showPhotoFilterDialog(func: dlg_photo_filter.() -> Unit): AlertDialog =
            dlg_photo_filter(this).apply {
                func()
            }.create()

    inline fun Fragment.showPhotoFilterDialog(func: dlg_photo_filter.() -> Unit): AlertDialog =
            dlg_photo_filter(this.context!!).apply {
                func()
            }.create()

    /*********************** Define ***********************/

    /*********************** Member ***********************/
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


    private var m_App: MyApp? = null
    private var m_Context: Context? = null
    private var m_FragPagerAdapter:MainPagerAdapter? = null
    private var viewPager:ViewPager? = null
    private val mScaleGestureDetector: ScaleGestureDetector? = null
    private val mScaleFactor = 1.0f

    private var m_UploadImgFile:Bitmap? = null//회전 안시킨 이미지..
    private var m_bitmapRotateBitmap: Bitmap? = null//회전시킨이미지
    private var mUploadPhotoType = 0//TYPE_ALBUM,  TYPE_CAMERA
    private var imageUri: Uri? = null
    private var selectedImage: Uri? = null
    private var m_bCamera:Boolean = false
    /*********************** Controller ***********************/
    private var mLyNoneProfile:LinearLayout?= null
    private var m_iv_Profile: ImageView? = null
    private var m_ThemeSettingDialog:AlertDialog? = null
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        m_Context = this
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActMain)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_main)

        MultiDex.install(this)

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
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect()
    }
    //--------------------------------------------------------------
    //
    override fun onStop()
    {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect()
    }
    //--------------------------------------------------------------
    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constant.REQUEST_SELECT_IMAGE_IN_ALBUM)//select gallery
        {
            if (data != null) {
                m_bitmapRotateBitmap = null
                mUploadPhotoType = Constant.REQUEST_SELECT_IMAGE_IN_ALBUM
                val contentURI = data!!.data
                try {
                    showPhotoFilterDialog(contentURI!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            else
            {
                Toast.makeText(this, m_Context!!.resources.getString(R.string.gallery_file_error), Toast.LENGTH_LONG).show()
            }
        }
        else if (requestCode == Constant.REQUEST_TAKE_PHOTO)//take photo
        {
            m_bitmapRotateBitmap = null //
            try
            {
                try
                {
                    mUploadPhotoType = Constant.REQUEST_TAKE_PHOTO
                    selectedImage = imageUri
                }
                catch (e: Exception)
                {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show()
                }
                showPhotoFilterDialog(selectedImage!!)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    //--------------------------------------------------------------
    //
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)//거부
        {
            Toast.makeText(m_Context, m_Context!!.resources.getString(R.string.str_msg_24), Toast.LENGTH_LONG).show()
            return
        }
        else //허용
        {
            when(requestCode)
            {
                Constant.PERMISSION_WRITE_EXTERNAL_STORAGE ->
                {
                    if(!m_bCamera)
                        selectImageInAlbum()
                    else
                        takePhoto()

                    return
                }
                Constant.PERMISSION_CAMERA ->
                {
                    checkPermissionCamera()
                    return
                }
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
                        else//거부..
                        {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                            {
                                return
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
                    return
                }
            }
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
            finish()
            System.exit(0)
        }
    }
    /*********************** toolbar menu ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        var menuItem1:MenuItem = menu!!.findItem(R.id.action_share).setVisible(false)
        var menuItem2:MenuItem = menu!!.findItem(R.id.share_location).setVisible(true)
        var menuItem3:MenuItem = menu!!.findItem(R.id.show_map).setVisible(true)
        var menuItem4: MenuItem = menu!!.findItem(R.id.edit).setVisible(false)
        var menuItem5: MenuItem = menu!!.findItem(R.id.add_photo).setVisible(false)
        return super.onCreateOptionsMenu(menu)
    }
    //--------------------------------------------------------------
    //
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.show_map -> {
                var frag: FrPlace = m_FragPagerAdapter!!.instantiateItem(viewPager!!,MainPagerAdapter.TAB_INDEX_FRPALCE) as FrPlace
                frag.menuItemShowMap()
                return true
            }
            R.id.share_location -> {
                var frag: FrPlace = m_FragPagerAdapter!!.instantiateItem(viewPager!!,MainPagerAdapter.TAB_INDEX_FRPALCE) as FrPlace
                frag.menuItemShareLocation()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun initValue()
    {

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
        viewPager = findViewById(R.id.viewPager)
        if (viewPager != null)
        {
            m_FragPagerAdapter = MainPagerAdapter(m_Context!!, supportFragmentManager)
            viewPager!!.adapter = m_FragPagerAdapter
        }

        settingDrawerView()//navigation drawer
        getUserDataProc()//user info..

        //
        if(m_App!!.m_SpCtrl!!.getIsAnnonLogin())//익명로그인이면..
        {
            m_App!!.m_SpCtrl!!.setIsAnonLogin(false)//스낵바 알림을 띄운 후 초기화
            //show snackbar..
            var snackbar:Snackbar?=null
            snackbar = Snackbar.make(ly_Base, m_Context!!.resources.getString(R.string.anonymous_login_desc), 10000)
                    .setAction(m_Context!!.resources.getString(R.string.str_ok), View.OnClickListener {
                        snackbar!!.dismiss()
            })
            snackbar.show()
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
                        bResult = true
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
    //
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
    //
    fun setJobDispatcher()
    {
        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
        val job = dispatcher.newJobBuilder()
                .setService(MyJobService::class.java)
                .setTag("my_tag")
                .setConstraints(Constraint.ON_UNMETERED_NETWORK)
                .build()
        dispatcher.mustSchedule(job)
    }
    //--------------------------------------------------------------
    //
    fun settingDrawerView()
    {
        toolbar.title = m_Context!!.resources.getString(R.string.app_name)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        var strTheme:String = m_App!!.m_SpCtrl!!.getSpTheme()!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setToolbarBackgroundColor(m_Context!!, this.toolbar, strTheme!!)
        }

        val toggle = ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        navigation_view.setNavigationItemSelectedListener(this)

        //header..
        var vNavHeader:View? = navigation_view!!.getHeaderView(0)
        mLyNoneProfile = vNavHeader!!.findViewById(R.id.lyNoneProfile)
        mLyNoneProfile!!.setOnClickListener {
            //show photo dialog
            addPhoto()
        }
        m_iv_Profile = vNavHeader!!.findViewById(R.id.iv_Profile)
        m_iv_Profile!!.setOnClickListener(View.OnClickListener{
            addPhoto()
        })
    }

    //-----------------------------------------------------
    //photo  adapter ifCallback
    fun addPhoto()
    {
        //show dialog..
        val pAlert = AlertDialog.Builder(this@ActMain).create()
        pAlert.setTitle(m_Context!!.resources.getString(R.string.str_msg_12))
        pAlert.setMessage(m_Context!!.resources.getString(R.string.str_msg_9))
        pAlert.setButton(AlertDialog.BUTTON_POSITIVE, m_Context!!.resources.getString(R.string.str_msg_11),{
            dialogInterface, i ->
            checkPermissionWriteStorage()
            pAlert.dismiss()
        })
        pAlert.setButton(AlertDialog.BUTTON_NEGATIVE, m_Context!!.resources.getString(R.string.str_msg_10),{
            dialogInterface, i ->
            checkPermissionCamera()
            pAlert.dismiss()
        })
        pAlert.show()
    }
    //-------------------------------------------------------------
    //저장소
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
    //카메라
    private fun checkPermissionCamera()
    {
        val permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(permissionCamera != PackageManager.PERMISSION_GRANTED)
        {

            permissionCamera()
            return
        }

        if(permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED)
        {
            m_bCamera = true
            permissionWriteStorege()
            return
        }


        takePhoto()
    }
    //-------------------------------------------------------------
    //
    fun selectImageInAlbum()
    {
        val pIntent = Intent(Intent.ACTION_GET_CONTENT)
        pIntent.type = "image/*"
        if (pIntent.resolveActivity(packageManager) != null)
        {
            startActivityForResult(pIntent, Constant.REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }
    //-------------------------------------------------------------
    //
    fun takePhoto()
    {
        val pIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        var timestamp:Long = System.currentTimeMillis()
        var fileName:String = String.format("%s_%s",timestamp, "img")
        val photo = File((Environment.getExternalStorageDirectory()).toString() + Constant.IMAGE_DIRECTORY, fileName)

        pIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(m_Context!!, "com.midas.secretplace.fileprovider", photo))
        imageUri = FileProvider.getUriForFile(m_Context!!, "com.midas.secretplace.fileprovider", photo)

        if (pIntent.resolveActivity(packageManager) != null)
        {
            startActivityForResult(pIntent, Constant.REQUEST_TAKE_PHOTO)
        }
    }
    //-------------------------------------------------------------
    //
    private fun permissionWriteStorege()
    {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.PERMISSION_WRITE_EXTERNAL_STORAGE )
    }
    //-------------------------------------------------------------
    //
    private fun permissionCamera()
    {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), Constant.PERMISSION_CAMERA)
    }
    //----------------------------------------------------------
    //  showing dialog
    fun showPhotoFilterDialog(pUri: Uri)
    {
        var photoFilterDialog:AlertDialog?=null
        photoFilterDialog = showPhotoFilterDialog {
            cancelable = true
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, pUri)
            dialogView!!.iv_Photo.setImageBitmap(bitmap!!)

            //close..
            closeIconClickListener {
                photoFilterDialog!!.dismiss()
            }

            //rotate..
            rotateIconClickListener {
                if(m_bitmapRotateBitmap != null)
                {
                    m_bitmapRotateBitmap = Util.getRotateBitmap(m_bitmapRotateBitmap!!)
                    m_UploadImgFile = m_bitmapRotateBitmap
                }
                else
                {
                    m_bitmapRotateBitmap = Util.getRotateBitmap(bitmap)
                    m_UploadImgFile = m_bitmapRotateBitmap
                    bitmap!!.recycle()
                    System.gc()
                }

                dialogView!!.iv_Photo.setImageBitmap(m_bitmapRotateBitmap!!)
            }

            //upload..
            sendIconClickListener {
                photoFilterDialog!!.dismiss()
                if(m_UploadImgFile == null)//회전을안했을때..
                    m_UploadImgFile = bitmap

                when(mUploadPhotoType)
                {
                    Constant.REQUEST_SELECT_IMAGE_IN_ALBUM -> uploadAlbumPhoto(m_UploadImgFile!!)
                    Constant.REQUEST_TAKE_PHOTO -> uploadCameraPhoto(m_UploadImgFile!!)
                }

            }
        }
        //  and showing
        photoFilterDialog?.show()
    }
    //--------------------------------------------------------------------------------
    //앨범이미지 업로드
    fun uploadAlbumPhoto(bitmap:Bitmap)
    {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)//close drawer..

        progressBar.visibility = View.VISIBLE
        tv_Progress.visibility = View.VISIBLE

        val imageReference = FirebaseStorage.getInstance(Constant.FIRE_STORE_URL)

        //메모리데이터 업로드 방식
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 15, baos)//압축 0~100사이 품질 조절가능
        val byteArr: ByteArray = baos.toByteArray()
        var timestamp: Long = System.currentTimeMillis()
        var fileName: String = String.format("%s_%s", timestamp, "img")

        val fileRef = imageReference!!.reference.child(fileName)
        fileRef.putBytes(byteArr)
                .addOnSuccessListener { taskSnapshot ->
                    //img table update
                    var tbUser: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!
                            .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                            .child("img_url")//where

                    tbUser!!.setValue(taskSnapshot.downloadUrl.toString())//insert
                    tbUser.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            if (dataSnapshot!!.exists()) {
                                progressBar.visibility = View.GONE
                                tv_Progress.visibility = View.GONE

                                getUserDataProc()//userdata refresh.
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {
                            progressBar.visibility = View.GONE
                            tv_Progress.visibility = View.GONE
                        }
                    })
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    // progress percentage
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                    // percentage in progress dialog
                    val intProgress = progress.toInt()
                    tv_Progress.text = "Uploaded " + intProgress + "%..."
                }
                .addOnPausedListener { System.out.println("Upload is paused!") }
    }
    //--------------------------------------------------------------------------------
    //
    fun uploadCameraPhoto(bitmap:Bitmap)
    {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)//close drawer..

        progressBar.visibility = View.VISIBLE
        tv_Progress.visibility = View.VISIBLE
        val imageReference = FirebaseStorage.getInstance(Constant.FIRE_STORE_URL)
        //메모리데이터 업로드 방식
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 15, baos)//압축 0~100사이 품질 조절가능
        val byteArr: ByteArray = baos.toByteArray()
        var timestamp: Long = System.currentTimeMillis()
        var fileName: String = String.format("%s_%s", timestamp, "img")

        val fileRef = imageReference!!.reference.child(fileName)
        fileRef.putBytes(byteArr)
                .addOnSuccessListener { taskSnapshot ->
                    //img table update
                    var tbUser: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!
                            .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                            .child("img_url")//where

                    tbUser!!.setValue(taskSnapshot.downloadUrl.toString())//insert
                    tbUser.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            if (dataSnapshot!!.exists()) {
                                progressBar.visibility = View.GONE
                                tv_Progress.visibility = View.GONE

                                getUserDataProc()//userdata refresh.
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {
                            progressBar.visibility = View.GONE
                            tv_Progress.visibility = View.GONE
                        }
                    })
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    // progress percentage
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                    // percentage in progress dialog
                    val intProgress = progress.toInt()
                    tv_Progress.text = "Uploaded " + intProgress + "%..."
                }
                .addOnPausedListener { System.out.println("Upload is paused!") }
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
            {
                Glide.with(this).load(pInfo.img_url).into(m_iv_Profile)
                m_iv_Profile!!.visibility = View.VISIBLE
                mLyNoneProfile!!.visibility = View.GONE
            }
            else
            {
                m_iv_Profile!!.visibility = View.GONE
                mLyNoneProfile!!.visibility = View.VISIBLE
            }
        }
        else
        {
            m_iv_Profile!!.visibility = View.GONE
            mLyNoneProfile!!.visibility = View.VISIBLE
        }
    }
    //--------------------------------------------------------------
    //
    fun goMyInformationActivity()
    {
        Intent(m_Context, ActMyInformation::class.java).let{
            startActivity(it)
        }
    }
    //--------------------------------------------------------------
    //
    fun goCoupleActivity()
    {
        Intent(m_Context, ActCouple::class.java).let{
            startActivity(it)
        }
    }
    //--------------------------------------------------------------
    //테마설정 dialog
    fun showThmeSelectDialog()
    {
        var themeAdapter: ThemeColorRvAdapter? = null
        var arrTheme:ArrayList<theme>? = ArrayList()
        arrTheme!!.add(theme(Constant.THEME_PINK))
        arrTheme!!.add(theme(Constant.THEME_RED))
        arrTheme!!.add(theme(Constant.THEME_PUPLE))
        arrTheme!!.add(theme(Constant.THEME_DEEPPUPLE))
        arrTheme!!.add(theme(Constant.THEME_INDIGO))
        arrTheme!!.add(theme(Constant.THEME_BLUE))
        arrTheme!!.add(theme(Constant.THEME_LIGHTBLUE))
        arrTheme!!.add(theme(Constant.THEME_CYAN))
        arrTheme!!.add(theme(Constant.THEME_TEAL))
        arrTheme!!.add(theme(Constant.THEME_GREEN))
        arrTheme!!.add(theme(Constant.THEME_LIGHTGREEN))
        arrTheme!!.add(theme(Constant.THEME_LIME))
        arrTheme!!.add(theme(Constant.THEME_YELLOW))
        arrTheme!!.add(theme(Constant.THEME_AMBER))
        arrTheme!!.add(theme(Constant.THEME_ORANGE))
        arrTheme!!.add(theme(Constant.THEME_DEEPORANGE))
        arrTheme!!.add(theme(Constant.THEME_BROWN))
        arrTheme!!.add(theme(Constant.THEME_GRAY))
        arrTheme!!.add(theme(Constant.THEME_BLUEGRAY))

        m_ThemeSettingDialog = showThemeSettingDialog {
            cancelable = true

            themeAdapter = ThemeColorRvAdapter(m_Context!!, arrTheme!!, this@ActMain)
            dialogView!!.themeRecyclerView!!.adapter = themeAdapter
            var nSpanCnt = 5
            val pLayoutManager = GridLayoutManager(m_Context!!, nSpanCnt)
            dialogView!!.themeRecyclerView!!.setHasFixedSize(true)
            dialogView!!.themeRecyclerView!!.layoutManager = pLayoutManager
            //close..
            closeIconClickListener {
                m_ThemeSettingDialog!!.dismiss()
            }


        }
        //  and showing
        m_ThemeSettingDialog?.show()
    }
    //--------------------------------------------------------------
    //로그아웃 dialog
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
    //회원 탈퇴 dialog
    fun showMemberDeleteDialog()
    {
        val builder = AlertDialog.Builder(this@ActMain)
        builder.setMessage(getString(R.string.str_msg_27))
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            deleteMemberProc()
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
    fun showFinishDialog()
    {
        val builder = AlertDialog.Builder(this@ActMain)
        builder.setMessage(getString(R.string.str_msg_28))
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            m_App!!.logoutProc(m_Context as ActMain)
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    //--------------------------------------------------------------
    //
    fun deleteMemberProc()
    {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))//open  상태이면 닫고.,
            drawer_layout.closeDrawer(GravityCompat.START)

        progressBar.visibility = View.VISIBLE

        //FIRE AUTH
        deleteFireAuthData()

        //TB_PLACE
        deletePlaceTableData()

        //TB_GROUP_PLACE
        deleteGroupPlaceTableData()

        //TB_GROUP
        deleteGroupTableData()

        //TB_USER
        deleteUserTableData()

        //TB_COUPLE
        deleteCoupleTableData()

        //delay 5seconds..
        Handler().postDelayed({
            progressBar.visibility = View.GONE
            //서비스 종료팝업.. 그동안 이용해주셔서 감사..ㅠㅠ
            showFinishDialog()
        }, 5000)
    }
    //--------------------------------------------------------------
    //
    fun deletePlaceTableData()
    {
        var pQuery: Query? = null
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)
                .orderByKey().equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())

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
                    val children = dataSnapshot!!.children
                    children.forEach {
                        var pInfo:place = it.getValue(place::class.java)!!

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

    //--------------------------------------------------------------
    //TB_GROUP_PLACE
    fun deleteGroupPlaceTableData()
    {
        var pQuery: Query? = null
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)
                .orderByKey().equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())

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
                    val children = dataSnapshot!!.children
                    children.forEach {
                        var pInfo:place = it.getValue(place::class.java)!!

                        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)!!
                                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                                .child(pInfo.group_key)
                                .child("place_list")
                                .child(pInfo.place_key)//where

                        pDbRef!!.removeValue()

                        //file storage remove
                        storageDeleteItemProc(pInfo.place_key!!)

                        //file data remove
                        pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!
                                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                                .child(pInfo.place_key)//where

                        pDbRef!!.removeValue()
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
    //--------------------------------------------------------------
    //TB_GROUP
    fun deleteGroupTableData()
    {
        var pQuery: Query? = null
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)
                .orderByKey().equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())

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
                    val children = dataSnapshot!!.children
                    children.forEach {
                        var pInfo:group = it.getValue(group::class.java)!!

                        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)!!
                                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                                .child(pInfo.group_key)//where

                        pDbRef!!.removeValue()

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

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }
    //--------------------------------------------------------------
    //
    fun deleteUserTableData()
    {
        var pQuery: Query? = null
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())

        pQuery.removeValue()
    }
    //--------------------------------------------------------------
    //
    fun deleteCoupleTableData()
    {
        var pQuery: Query? = null
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE).orderByKey()
        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if(dataSnapshot!!.exists())
                {
                    val children = dataSnapshot!!.children
                    children.forEach {
                        var pInfo: couple = it.getValue(couple::class.java)!!

                        if(pInfo.responser_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey())
                            || pInfo.requester_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))
                        {
                            var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!.child(it.key)//where
                            pDbRef!!.removeValue()
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

    //--------------------------------------------------------------
    //
    fun deleteFireAuthData()
    {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }
    }
    //--------------------------------------------------------------
    //
    fun goSetting(){
        Intent(m_Context, ActSetting::class.java).let{
            startActivity(it)
        }
    }
    /*********************** listener ***********************/
    //--------------------------------------------------------------
    //
    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        var result = false

        when(item.itemId)
        {
            /*
            R.id.my_information ->//내정보
            {
                goMyInformationActivity()
                return true
            }
            R.id.couple ->//커플
            {
                goCoupleActivity()
                return true
            }
            */
            /*
            R.id.open_loaction ->
            {
                Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.msg_will_open), Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.popular_location ->
            {
                Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.msg_will_open), Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.people ->
            {
                Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.msg_will_open), Toast.LENGTH_SHORT).show()
                return true
            }
            */
            R.id.theme ->//테마설정
            {
                showThmeSelectDialog()
                return true
            }
            R.id.setting ->//설정
            {
                goSetting()
                return true
            }

            /*
            R.id.logout ->//로그아웃
            {
                showLogoutDialog()
                return true
            }
            */
            /*
            R.id.delete ->//회원탈퇴
            {
                showMemberDeleteDialog()
                return true
            }
            */
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return result
    }
    //--------------------------------------------------------------
    //theme color adapter callback
    override fun themeSelect(pInfo: theme)
    {
        m_App!!.m_SpCtrl!!.setSpTheme(pInfo!!.colorName)
        m_App!!.goMain(m_Context!!)
    }
    /*********************** interface ***********************/
    /*********************** util ***********************/

}
