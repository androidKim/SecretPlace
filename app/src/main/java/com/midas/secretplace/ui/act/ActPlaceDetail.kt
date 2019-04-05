
package com.midas.secretplace.ui.act

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AbsListView
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.adapter.PhotoRvAdapter
import com.midas.secretplace.ui.custom.dlg_photo_filter
import com.midas.secretplace.ui.custom.dlg_photo_view
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_place_detail.*
import kotlinx.android.synthetic.main.dlg_photo_filter.view.*
import kotlinx.android.synthetic.main.dlg_photo_view.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


class ActPlaceDetail : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,PhotoRvAdapter.ifCallback{
    /*********************** extention fun ***********************/
    //----------------------------------------------------------
    //pinchToZoom
    inline fun Activity.showPhotoViewDialog(func: dlg_photo_view.() -> Unit): AlertDialog =
            dlg_photo_view(this).apply {
                func()
            }.create()

    inline fun Fragment.showPhotoViewDialog(func: dlg_photo_view.() -> Unit): AlertDialog =
            dlg_photo_view(this.context!!).apply {
                func()
            }.create()

    //----------------------------------------------------------
    //filter
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
    var m_App: MyApp? = null
    var m_Context: Context? = null
    var m_RequestManager:RequestManager? = null
    var m_PlaceInfo:place? = place()
    var m_LayoutInflater:LayoutInflater? = null
    var m_Adapter: PhotoRvAdapter? = null
    var imageUri: Uri? = null
    var m_bitmapRotateBitmap:Bitmap? = null
    var m_UploadImgFile:Bitmap? = null
    var mCurrentPhotoPath:String? = null
    var m_bRunning:Boolean = false
    var m_bFinish:Boolean = false
    var m_bModify:Boolean = false
    var m_bScrollTouch:Boolean = false
    var m_bCamera:Boolean = false
    var m_arrItem:ArrayList<String> = ArrayList<String>()
    var m_nUploadPhotoType = 0//TYPE_ALBUM,  TYPE_CAMERA
    /*********************** Controller ***********************/
    var m_PhotoViewDialog:AlertDialog? = null
    var m_PhotoFilterDialog:AlertDialog? = null
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
            m_App!!.init(m_Context as ActPlaceDetail)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_place_detail)

        initValue()
        recvIntentData()
        initLayout()
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
        if(requestCode == Constant.REQUEST_SELECT_IMAGE_IN_ALBUM)//select gallery
        {
            if (data != null) {
                m_bitmapRotateBitmap = null
                m_nUploadPhotoType = Constant.REQUEST_SELECT_IMAGE_IN_ALBUM
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
            var file:File  = File(mCurrentPhotoPath)
            var bitmap:Bitmap = MediaStore.Images.Media.getBitmap(m_Context!!.contentResolver, Uri.fromFile(file))
            m_bitmapRotateBitmap = null //
            try
            {
                try
                {
                    m_nUploadPhotoType = Constant.REQUEST_TAKE_PHOTO
                }
                catch (e: Exception)
                {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show()
                }
                showPhotoFilterDialog(imageUri!!)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    //--------------------------------------------------------------
    //permission checking callback
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)//거부
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
            }
        }
    }
    //--------------------------------------------------------------
    //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        var menuItem1: MenuItem = menu!!.findItem(R.id.action_share).setVisible(false)
        var menuItem2: MenuItem = menu!!.findItem(R.id.share_location).setVisible(true)
        var menuItem3: MenuItem = menu!!.findItem(R.id.show_map).setVisible(true)
        var menuItem4: MenuItem = menu!!.findItem(R.id.edit).setVisible(true)
        var menuItem5: MenuItem = menu!!.findItem(R.id.add_photo).setVisible(true)

        return super.onCreateOptionsMenu(menu)
    }
    //--------------------------------------------------------------
    //
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.share_location -> {
                Util.sharePlaceLocationInfo(this, this, m_PlaceInfo!!)
                return true
            }
            R.id.show_map -> {
                goMapDetail()
                return true
            }
            R.id.edit -> {
                editContent()
                return true
            }
            R.id.add_photo -> {
                addPhoto()
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
        m_arrItem = ArrayList<String>()
        //m_strImgLastSeq = null
    }
    //--------------------------------------------------------------
    //
    fun recvIntentData()
    {
        var pIntent: Intent = intent

        if(pIntent == null)
            return

        if(pIntent.hasExtra(Constant.INTENT_DATA_PLACE_OBJECT))
            m_PlaceInfo =  pIntent.extras.get(Constant.INTENT_DATA_PLACE_OBJECT) as place
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

        m_LayoutInflater = LayoutInflater.from(m_Context)

        //listener..
        ly_SwipeRefresh.setOnRefreshListener(this)//refresh..
        ly_NoData.visibility = View.GONE
        getPlaceInfoProc()
    }
    //--------------------------------------------------------------
    //
    fun getPlaceInfoProc(){
        var dbQuery:Query = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .child(m_PlaceInfo!!.place_key)

        dbQuery.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists())
                {
                    m_PlaceInfo = p0!!.getValue(place::class.java)!!
                    settingPlaceView()
                }
                else
                {

                }
            }

        })
    }
    //--------------------------------------------------------------
    //
    fun settingPlaceView()
    {
        if(m_PlaceInfo!! == null)
            return

        //setTitle..
        toolbar.title = m_PlaceInfo!!.name

        //favorite status
        if(m_PlaceInfo!!.favorite.equals("Y"))
            ivFavorite.setImageResource(R.drawable.ic_favorite_black_24dp)
        else
            ivFavorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)

        m_Adapter = PhotoRvAdapter(m_Context!!, m_RequestManager!!, m_PlaceInfo!!, m_arrItem!!, this, supportFragmentManager)
        recyclerView.adapter = m_Adapter

        var nSpanCnt = 1
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

                if(firstVisible == 0)
                {

                }
                else
                {
                    if(m_bScrollTouch)
                    {
                        /*
                        if (dy > 0)
                            slideUp()    // Scrolling up
                        else
                            slideDown()// Scrolling down
                        */

                        m_bScrollTouch = false
                    }
                }

                /*
                if(!m_bRunning!! && (visibleItemCount + firstVisible) >= totalItemCount)//최하단
                {
                    // Call your API to load more items
                }
                */
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {//손을 떼었지만 움직이는중
                    // Do something
                    m_bScrollTouch = true
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {//터치되어있는중
                    // Do something
                    m_bScrollTouch = true

                } else if(newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){//정지된상태
                    m_bScrollTouch = false
                }
                else {
                    // Do something
                    m_bScrollTouch = false
                }
            }
        })

        if(!m_bRunning!!)
            getImageListProc()//imageList
    }

    //-------------------------------------------------------------
    //
    fun getPlaceInfoProc(seq:String)
    {
        //place Object
        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .child(seq)//where

        pDbRef!!.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if(dataSnapshot!!.exists())
                {
                    val pInfo: place = dataSnapshot!!.getValue(place::class.java)!!
                    if(pInfo != null)
                    {
                        m_PlaceInfo = pInfo
                        settingPlaceView()
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

        if(!m_bRunning!!)
            getImageListProc()//imageList
    }
    //-------------------------------------------------------------
    //
    fun getImageListProc()
    {
        m_bRunning = true
        progressBar.visibility = View.VISIBLE
        //image list..
        var pQuery:Query?= null
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .child(m_PlaceInfo!!.place_key).orderByKey()

        pQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
            {
                // A new message has been added
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

                if(m_Adapter!!.itemCount > 0)//
                    ly_NoData.visibility = View.GONE
                else
                    ly_NoData.visibility = View.VISIBLE

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
    fun setRefresh()
    {
        initValue()
        if(m_Adapter != null)
            m_Adapter!!.clearData()

        ly_SwipeRefresh.isRefreshing = false
        getPlaceInfoProc(m_PlaceInfo!!.place_key!!)
    }
    //-------------------------------------------------------------
    //
    fun slideUp(){
        ViewCompat.animate(ly_SwipeRefresh)
                .translationX(0f)
                .translationY(-ly_Top.height.toFloat())
                .setDuration(1000)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setStartDelay(50)
                .setListener(null)
    }
    //-------------------------------------------------------------
    //
    fun slideDown(){
        ViewCompat.animate(ly_Top)
                .translationX(0f)
                .translationY(0f)
                .setDuration(1000)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setStartDelay(50)
                .setListener(null)
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
            startActivityForResult(pIntent, Constant.REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }
    //-------------------------------------------------------------
    //
    fun takePhoto()
    {
        var takePictureIntent:Intent  = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile:File? = null
            try {
                photoFile = createFile()

            } catch (ex:IOException ) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(m_Context!!,"com.midas.secretplace.fileprovider",photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, Constant.REQUEST_TAKE_PHOTO);
            }
        }
    }
    //-------------------------------------------------------------
    //
    fun createFile():File
    {
        // Create an image file name
        var timeStamp:String  = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName:String  = "JPEG_" + timeStamp + "_";
        var storageDir:File = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        var image:File = File.createTempFile(imageFileName,  /* prefix */".jpg",         /* suffix */
        storageDir      /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    //-------------------------------------------------------------
    //
    fun editContentProc(strName:String)
    {
        //update
        m_PlaceInfo!!.name = strName

        var pDbRef: DatabaseReference? = null
        pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)
                .child(m_App!!.m_SpCtrl!!.getSpUserKey()).child(m_PlaceInfo!!.place_key)
        pDbRef!!.setValue(m_PlaceInfo!!)

        pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if (dataSnapshot!!.exists())
                {
                    setRefresh()
                    m_bModify = true
                }
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }
    //-----------------------------------------------------
    //show map dialog
    fun goMapDetail()
    {
        var pIntent = Intent(m_Context, ActMapDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_PLACE_OBJECT, m_PlaceInfo as Serializable)
        startActivityForResult(pIntent, 0)
    }
    //-----------------------------------------------------
    //photo  adapter ifCallback
    fun addPhoto()
    {
        //show dialog..
        val pAlert = AlertDialog.Builder(this@ActPlaceDetail).create()
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
    //-----------------------------------------------------
    //photo adapter ifCallback
    fun editContent()
    {
        //show dialog..
        val pAlert = AlertDialog.Builder(this@ActPlaceDetail).create()
        pAlert.setTitle("["+m_PlaceInfo!!.name +"] "+m_Context!!.resources.getString(R.string.str_msg_8))
        pAlert.setMessage(m_Context!!.resources.getString(R.string.str_msg_17))
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

    //----------------------------------------------------------
    //  showing dialog
    fun showPhotoViewDialog(url: String)
    {
        m_PhotoViewDialog = showPhotoViewDialog {
            cancelable = true

            Glide.with(applicationContext)
                    .load(url)
                    .listener(object : RequestListener<Drawable>
                    {
                        override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean
                        {
                            m_PhotoViewDialog!!.dismiss()
                            return false
                        }
                        override fun onResourceReady(p0: Drawable?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean
                        {
                            if(p0 != null)
                                dialogView!!.iv_DlgNone!!.tag = url

                            return false
                        }
                    })
                    .into(dialogView!!.iv_DlgPhoto)

            //iv_DlgPhoto!!.setImageDrawable(drawable!!)
            closeIconClickListener {
                m_PhotoViewDialog!!.dismiss()
            }

        }
        //  and showing
        m_PhotoViewDialog?.show()
    }

    //----------------------------------------------------------
    //  showing dialog
    fun showPhotoFilterDialog(pUri:Uri)
    {
        m_PhotoFilterDialog = showPhotoFilterDialog {
            cancelable = true
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, pUri)
            dialogView!!.iv_Photo.setImageBitmap(bitmap!!)

            //close..
            closeIconClickListener {
                m_PhotoFilterDialog!!.dismiss()
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
                m_PhotoFilterDialog!!.dismiss()
                if(m_UploadImgFile == null)//회전을안했을때..
                    m_UploadImgFile = bitmap

                when(m_nUploadPhotoType)
                {
                    Constant.REQUEST_SELECT_IMAGE_IN_ALBUM -> uploadAlbumPhoto(m_UploadImgFile!!)
                    Constant.REQUEST_TAKE_PHOTO -> uploadCameraPhoto(m_UploadImgFile!!)
                }

            }
        }
        //  and showing
        m_PhotoFilterDialog?.show()
    }
    //--------------------------------------------------------------------------------
    //
    fun uploadAlbumPhoto(bitmap:Bitmap)
    {
        progressBar.visibility = View.VISIBLE
        tv_Progress.visibility = View.VISIBLE

        val imageReference = FirebaseStorage.getInstance(Constant.FIRE_STORE_URL)

        //메모리데이터 업로드 방식
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 30, baos)//압축 0~100사이 품질 조절가능
        val byteArr: ByteArray = baos.toByteArray()
        var timestamp: Long = System.currentTimeMillis()
        var fileName: String = String.format("%s_%s", timestamp, "img")

        val fileRef = imageReference!!.reference.child(fileName)
        fileRef.putBytes(byteArr)
        .addOnSuccessListener { taskSnapshot ->
            //val uri = taskSnapshot.downloadUrl

            //img table update
            var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                    .child(m_PlaceInfo!!.place_key).push()//where
            pDbRef!!.setValue(taskSnapshot.downloadUrl.toString())//insert
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (dataSnapshot!!.exists()) {
                        m_bModify = true
                        setRefresh()
                        progressBar.visibility = View.GONE
                        tv_Progress.visibility = View.GONE
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {
                    progressBar.visibility = View.GONE
                    tv_Progress.visibility = View.GONE
                }
            })

            //place table update
            var pPlaceDb: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                    .child(m_PlaceInfo!!.place_key)

            pPlaceDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (dataSnapshot!!.exists()) {

                        val pInfo:place = dataSnapshot!!.getValue(place::class.java)!!
                        pInfo!!.img_url = taskSnapshot.downloadUrl.toString()
                        pPlaceDb.setValue(pInfo)
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {

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
        progressBar.visibility = View.VISIBLE
        tv_Progress.visibility = View.VISIBLE
        val imageReference = FirebaseStorage.getInstance(Constant.FIRE_STORE_URL)
        //메모리데이터 업로드 방식
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 30, baos)//압축 0~100사이 품질 조절가능
        val byteArr: ByteArray = baos.toByteArray()
        var timestamp: Long = System.currentTimeMillis()
        var fileName: String = String.format("%s_%s", timestamp, "img")

        val fileRef = imageReference!!.reference.child(fileName)
        fileRef.putBytes(byteArr)
        .addOnSuccessListener { taskSnapshot ->
            //update
            var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                    .child(m_PlaceInfo!!.place_key).push()//where

            pDbRef!!.setValue(taskSnapshot.downloadUrl.toString())//insert
            pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (dataSnapshot!!.exists()) {
                        m_bModify = true
                        setRefresh()
                        progressBar.visibility = View.GONE
                        tv_Progress.visibility = View.GONE
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {
                    progressBar.visibility = View.GONE
                    tv_Progress.visibility = View.GONE
                }
            })

            //place table update
            var pPlaceDb: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                    .child(m_PlaceInfo!!.place_key)

            pPlaceDb.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (dataSnapshot!!.exists()) {

                        val pInfo:place = dataSnapshot!!.getValue(place::class.java)!!
                        pInfo!!.img_url = taskSnapshot.downloadUrl.toString()
                        pPlaceDb.setValue(pInfo)
                    }
                }

                override fun onCancelled(p0: DatabaseError?) {

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



    /************************* listener *************************/
    //-----------------------------------------------------
    //좋아요 클릭
    fun onClickFavorite(view:View){
        if(m_PlaceInfo!! == null)
            return

        var strFavoritStatus:String = ""
        if(m_PlaceInfo!!.favorite.equals("Y"))
        {
            strFavoritStatus = "N"
            ivFavorite.setImageResource(R.drawable.ic_favorite_border_black_24dp)
        }
        else
        {
            strFavoritStatus = "Y"
            ivFavorite.setImageResource(R.drawable.ic_favorite_black_24dp)
        }

        m_PlaceInfo!!.favorite = strFavoritStatus

        //db update..
        var dbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .child(m_PlaceInfo!!.place_key)
        dbRef!!.setValue(m_PlaceInfo!!)//db update
        dbRef!!.addListenerForSingleValueEvent(object: ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.exists())
                {
                    m_PlaceInfo = p0!!.getValue(place::class.java)!!
                }
            }

        })
    }


    /************************* callback function *************************/
    //-----------------------------------------------------
    //Swipe Refresh Listener
    override fun onRefresh()
    {
        setRefresh()
    }
    //-----------------------------------------------------
    //photo adapter ifCallback
    override fun showPhotoDialog(url:String)
    {
        showPhotoViewDialog(url)
    }
    /*********************** interface ***********************/

}
