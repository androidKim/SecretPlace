
package com.midas.secretplace.ui.act

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.midas.mytimeline.ui.adapter.PlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.ReqBase
import com.midas.secretplace.structure.core.photo
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.adapter.PhotoRvAdapter
import com.midas.secretplace.ui.custom.SimpleDividerItemDecoration
import com.midas.secretplace.ui.frag.MapFragment
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_place_detail.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class ActPlaceDetail : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,PhotoRvAdapter.ifCallback
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
    var m_App: MyApp? = null
    var m_Context: Context? = null
    var m_PlaceInfo:place? = null
    var m_LayoutInflater:LayoutInflater? = null
    var m_Adapter: PhotoRvAdapter? = null
    var m_strImgpath:String ?= null;

    /*********************** Controller ***********************/
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_place_detail)
        m_Context = this
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActPlaceDetail)

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
                val contentURI = data!!.data
                try
                {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    m_strImgpath = saveImage(bitmap)
                    Toast.makeText(m_Context, "Image Saved!", Toast.LENGTH_SHORT).show()
                    //iv_Attach!!.setImageBitmap(bitmap)

                    val data = FirebaseStorage.getInstance("gs://secretplace-29d5e.appspot.com")
                    var value = 0.0

                    var timestamp:Long = System.currentTimeMillis()
                    var fileName:String = String.format("%s_%s",timestamp, "img")
                    var storage = data.getReference().child(fileName).putFile(contentURI)
                            .addOnProgressListener { taskSnapshot ->
                                value = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                                Log.v("value","value=="+value)

                            }
                            .addOnSuccessListener {
                                taskSnapshot ->
                                val uri = taskSnapshot.downloadUrl
                                Log.v("Download File","File.." +uri)

                                if(m_PlaceInfo!!.img_list == null)
                                    m_PlaceInfo!!.img_list = ArrayList<photo>()

                                var photoInfo:photo = photo()
                                photoInfo.img_url = taskSnapshot.downloadUrl.toString()
                                m_PlaceInfo!!.img_list!!.add(photoInfo)

                                if(m_PlaceInfo!!.img_list!! != null)//remove header
                                    m_PlaceInfo!!.img_list!!.removeAt(0)

                                //update
                                var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setPlaceInfo(m_PlaceInfo!!)
                                pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot?)
                                    {
                                        if (dataSnapshot!!.exists())
                                        {
                                            setRefresh()
                                        }
                                    }

                                    override fun onCancelled(p0: DatabaseError?)
                                    {

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
                    Toast.makeText(m_Context, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (requestCode == REQUEST_TAKE_PHOTO)//take photo
        {
            if (data != null)
            {
                val thumbnail = data!!.extras!!.get("data") as Bitmap
                //iv_Attach!!.setImageBitmap(thumbnail)
                m_strImgpath = saveImage(thumbnail)
                Toast.makeText(m_Context, "Image Saved!", Toast.LENGTH_SHORT).show()
            }
        }
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

        //event..
        ly_SwipeRefresh.setOnRefreshListener(this)

        //map expand
        ly_MapExpand.setOnClickListener(View.OnClickListener {
            ly_MapExpand.visibility = View.GONE
            ly_MapCollapse.visibility = View.VISIBLE

            //expand map..
            val params = mapFragment!!.getView()!!.getLayoutParams()
            params.height = RelativeLayout.LayoutParams.MATCH_PARENT
            mapFragment!!.getView()!!.setLayoutParams(params)
        })

        //map collapse
        ly_MapCollapse.setOnClickListener(View.OnClickListener {

            ly_MapExpand.visibility = View.VISIBLE
            ly_MapCollapse.visibility = View.GONE

            val params = mapFragment!!.getView()!!.getLayoutParams()
            params.height = 600
            mapFragment!!.getView()!!.setLayoutParams(params)
        })


        settingView()
    }
    //--------------------------------------------------------------
    //
    fun settingView()
    {
        //map..
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        mapFragment!!.getMapAsync(mapFragment)
        val mArgs = Bundle()
        mArgs.putSerializable(Constant.INTENT_DATA_PLACE_OBJECT, m_PlaceInfo)
        mapFragment.arguments = mArgs


        //getPlaceInfoProc(m_PlaceInfo!!.seq!!)

        settingPlaceView()
    }
    //--------------------------------------------------------------
    //
    fun settingPlaceView()
    {
        //if(m_Adapter == null)
        //{
            //img list
            if(m_PlaceInfo!!.img_list == null)
                m_PlaceInfo!!.img_list = ArrayList()

            var pHeader:photo = photo()
            pHeader.isHeader = true
            pHeader.img_url = ""

            m_PlaceInfo!!.img_list!!.add(0, pHeader)//setHeader

            m_Adapter = PhotoRvAdapter(m_Context!!, m_PlaceInfo!!, m_PlaceInfo!!.img_list!!, this, supportFragmentManager)
            recyclerView.adapter = m_Adapter
            recyclerView!!.addItemDecoration(SimpleDividerItemDecoration(20))//set recyclerview grid Item spacing
            var nSpanCnt = 1
            /*
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)//landspace mode.
                nSpanCnt = 4
            */

            val pLayoutManager = GridLayoutManager(m_Context, nSpanCnt)
            recyclerView!!.layoutManager = pLayoutManager
            recyclerView!!.setHasFixedSize(true)
        //}
        //else//addData
        //{
            //m_Adapter!!.addData(null)
        //}
    }

    //-------------------------------------------------------------
    //
    fun getPlaceInfoProc(seq:String)
    {
        /*
        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!.child(seq).child("img_list").limitToFirst(ReqBase.ITEM_COUNT)
        pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?)
            {
                val pInfo: place = p0!!.getValue(place::class.java)!!
                if(pInfo != null)
                {
                    m_PlaceInfo = pInfo

                    settingPlaceView()
                }
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
        */

        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!.child(seq)//where
        pDbRef!!.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                val pInfo: place = dataSnapshot!!.getValue(place::class.java)!!
                if(pInfo != null)
                {
                    m_PlaceInfo = pInfo

                    settingPlaceView()
                }
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }
    //-------------------------------------------------------------
    //
    fun getImageListProc(index:Int)
    {

    }
    //-------------------------------------------------------------
    //
    fun setRefresh()
    {
        if(m_Adapter != null)
            m_Adapter!!.clearData()

        ly_SwipeRefresh!!.setRefreshing(false)

        getPlaceInfoProc(m_PlaceInfo!!.seq!!)
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
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
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
        if(m_PlaceInfo!!.img_list!! != null)//remove header
            m_PlaceInfo!!.img_list!!.removeAt(0)

        m_PlaceInfo!!.name = strName
        var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.setPlaceInfo(m_PlaceInfo!!)
        pDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?)
            {
                if (dataSnapshot!!.exists())
                {
                    setRefresh()
                }
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
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
    //adapter ifCallback
    override fun addPhoto()
    {
        //show dialog..
        val pAlert = AlertDialog.Builder(this@ActPlaceDetail).create()
        pAlert.setTitle("Do you want add photo?")
        pAlert.setMessage("you can choice!!")
        pAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Gallery",{
            dialogInterface, i ->
            checkPermissionWriteStorage();
            pAlert.dismiss();
        })
        pAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "Take Photo",{
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
        val pAlert = AlertDialog.Builder(this@ActPlaceDetail).create()
        pAlert.setTitle("Do you want edit content?")
        pAlert.setMessage("you can choice!!")
        var editName: EditText? = EditText(m_Context)
        editName!!.hint = getString(R.string.str_msg_4)
        pAlert.setView(editName)
        pAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",{
            dialogInterface, i ->
            var name:String = editName.text.toString()
            editContentProc(name)
            pAlert.dismiss()
        })
        pAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "No",{
            dialogInterface, i ->
            pAlert.dismiss()
        })
        pAlert.show()
    }

    /*********************** interface ***********************/

}
