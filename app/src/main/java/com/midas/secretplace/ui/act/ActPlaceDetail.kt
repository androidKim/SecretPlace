
package com.midas.secretplace.ui.act

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
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
import com.midas.secretplace.ui.custom.dlg_photo_view
import kotlinx.android.synthetic.main.act_place_detail.*
import kotlinx.android.synthetic.main.dlg_photo_view.view.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class ActPlaceDetail : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,PhotoRvAdapter.ifCallback{
    //extention functions..
    inline fun Activity.showPhotoViewDialog(func: dlg_photo_view.() -> Unit): AlertDialog =
            dlg_photo_view(this).apply {
                func()
            }.create()

    inline fun Fragment.showPhotoViewDialog(func: dlg_photo_view.() -> Unit): AlertDialog =
            dlg_photo_view(this.context!!).apply {
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
    var m_App: MyApp? = null
    var m_Context: Context? = null
    var m_RequestManager:RequestManager? = null
    var m_PlaceInfo:place? = place()
    var m_LayoutInflater:LayoutInflater? = null
    var m_Adapter: PhotoRvAdapter? = null
    var selectedImage: Uri? = null
    var imageUri: Uri? = null
    var m_strImgpath:String = ""
    //var m_strImgLastSeq:String? = null
    var m_bRunning:Boolean = false
    var m_bFinish:Boolean = false
    var m_bModify:Boolean = false
    var m_bScrollTouch:Boolean = false
    var m_arrItem:ArrayList<String> = ArrayList<String>()
    /*********************** Controller ***********************/
    var m_PhotoViewDialog:AlertDialog? = null
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_place_detail)
        m_Context = this
        m_RequestManager = Glide.with(this)
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActPlaceDetail)

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
        if(requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM)//select gallery
        {
            if (data != null) {
                progressBar.visibility = View.VISIBLE
                tv_Progress.visibility = View.VISIBLE
                val contentURI = data!!.data
                try {
                    val imageReference = FirebaseStorage.getInstance("gs://secretplace-29d5e.appspot.com")
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                    //메모리데이터 업로드 방식
                    val baos = ByteArrayOutputStream()
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 15, baos)//압축 0~100사이 품질 조절가능
                    m_strImgpath = saveImage(bitmap)
                    val byteArr: ByteArray = baos.toByteArray()
                    var timestamp: Long = System.currentTimeMillis()
                    var fileName: String = String.format("%s_%s", timestamp, "img")

                    val fileRef = imageReference!!.reference.child(fileName)
                    fileRef.putBytes(byteArr)
                            .addOnSuccessListener { taskSnapshot ->
                                //val uri = taskSnapshot.downloadUrl

                                //update
                                var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child(m_PlaceInfo!!.place_key).child("img_list").push()//where
                                pDbRef!!.setValue(taskSnapshot.downloadUrl.toString())//insert

                                //var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setPlaceInfo(m_PlaceInfo!!)
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
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            else
            {
                Toast.makeText(this, "No File!", Toast.LENGTH_LONG).show()
            }
        }
        else if (requestCode == REQUEST_TAKE_PHOTO)//take photo
        {
            progressBar.visibility = View.VISIBLE
            tv_Progress.visibility = View.VISIBLE

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

                val imageReference = FirebaseStorage.getInstance("gs://secretplace-29d5e.appspot.com")
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                //메모리데이터 업로드 방식
                val baos = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 15, baos)//압축 0~100사이 품질 조절가능
                m_strImgpath = saveImage(bitmap)
                val byteArr: ByteArray = baos.toByteArray()
                var timestamp: Long = System.currentTimeMillis()
                var fileName: String = String.format("%s_%s", timestamp, "img")

                val fileRef = imageReference!!.reference.child(fileName)
                fileRef.putBytes(byteArr)
                        .addOnSuccessListener { taskSnapshot ->
                            //val uri = taskSnapshot.downloadUrl

                            //update
                            var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child(m_PlaceInfo!!.place_key).child("img_list").push()//where
                            pDbRef!!.setValue(taskSnapshot.downloadUrl.toString())//insert

                            //var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setPlaceInfo(m_PlaceInfo!!)
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
                    selectImageInAlbum()
                    return
                }
                Constant.PERMISSION_CAMERA ->
                {
                    takePhoto()
                    return
                }
            }
        }
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
        m_LayoutInflater = LayoutInflater.from(m_Context)

        //listener..
        ly_SwipeRefresh.setOnRefreshListener(this)//refresh..


        //map dialog
        ly_ShowMap.setOnClickListener(View.OnClickListener {
            goMapDetail()
        })

        //addPhoto..
        ly_AddPhoto.setOnClickListener(View.OnClickListener {
            addPhoto()
        })

        //modify name.
        ly_EditContent.setOnClickListener(View.OnClickListener {
            editContent()
        })
        settingView()
    }
    //--------------------------------------------------------------
    //
    fun settingView()
    {
        ly_NoData.visibility = View.GONE
        settingPlaceView()
    }
    //--------------------------------------------------------------
    //
    fun settingPlaceView()
    {
        //setTitle..
        tv_Title.text = m_PlaceInfo!!.name

        //if(m_Adapter == null)
        //{
            m_Adapter = PhotoRvAdapter(m_Context!!, m_RequestManager!!, m_PlaceInfo!!, m_arrItem!!, this, supportFragmentManager)
            recyclerView.adapter = m_Adapter
            //recyclerView!!.addItemDecoration(SimpleDividerItemDecoration(20))//set recyclerview grid Item spacing
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
                        //if(!m_bFinish!!)
                            //getImageListProc()
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
        //}
        //else//addData
        //{
            //m_Adapter!!.addData(null)
        //}

        if(!m_bRunning!!)
            getImageListProc()//imageList
    }

    //-------------------------------------------------------------
    //
    fun getPlaceInfoProc(seq:String)
    {
        //place Object
        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!.child(seq)//where
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

        //recyclerView!!.addItemDecoration(SimpleDividerItemDecoration(-20))//init

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
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.PERMISSION_WRITE_EXTERNAL_STORAGE )
    }
    //-------------------------------------------------------------
    //
    private fun permissionCamerra()
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
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            dialogView!!.iv_DlgNone!!.visibility = View.GONE
                            dialogView!!.iv_DlgNone!!.visibility = View.VISIBLE
                        }
                        override fun onResourceReady(p0: Drawable?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean
                        {
                            //do something when picture already loaded
                            dialogView!!.iv_DlgNone!!.visibility = View.GONE
                            dialogView!!.iv_DlgNone!!.visibility = View.GONE


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


    /************************* listener *************************/


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
