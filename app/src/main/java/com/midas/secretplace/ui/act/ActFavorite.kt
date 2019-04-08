
package com.midas.secretplace.ui.act

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
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
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.midas.mytimeline.ui.adapter.PlaceRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_favorite.*
import java.io.Serializable
import kotlin.collections.ArrayList

/*
내가 좋아요 한 위치 리스트
 */
class ActFavorite : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,PlaceRvAdapter.ifCallback
{

    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App: MyApp? = null
    var m_Context: Context? = null
    var m_RequestManager: RequestManager? = null
    var m_arrPlace:ArrayList<place>? = ArrayList()//좋아요 한 장소리스트
    var m_LayoutInflater:LayoutInflater? = null
    var m_PlaceAdapter:PlaceRvAdapter? = null
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
            m_App!!.init(m_Context as ActFavorite)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_favorite)

        initValue()
        recvIntentData()
        initLayout()
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        super.onStart()
    }
    //--------------------------------------------------------------
    //
    override fun onStop()
    {
        super.onStop()
    }
    //---------------------------------------------------------------------------------------------------
    //
    override fun onBackPressed()
    {
        super.onBackPressed()
    }
    //---------------------------------------------------------------------------------------------------
    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)//Intent?  <-- null이 올수도있다
    {
        super.onActivityResult(requestCode, resultCode, data)
    }

    //--------------------------------------------------------------
    //
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        when (requestCode)
        {
            0 -> {

            }
        }
    }
    /*********************** Menu Function ***********************/
    //--------------------------------------------------------------
    //툴바 옵션 메뉴
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        var menuItem1: MenuItem = menu!!.findItem(R.id.action_share).setVisible(false)
        var menuItem2: MenuItem = menu!!.findItem(R.id.share_location).setVisible(false)
        var menuItem3: MenuItem = menu!!.findItem(R.id.show_map).setVisible(true)
        var menuItem4: MenuItem = menu!!.findItem(R.id.edit).setVisible(false)
        var menuItem5: MenuItem = menu!!.findItem(R.id.add_photo).setVisible(false)

        return super.onCreateOptionsMenu(menu)
    }
    //--------------------------------------------------------------
    //툴바 옵션메뉴 이벤트
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_map -> {//좋아요 장소리스트 위치만 지도로 보여주기
                menuItemShowMap()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //변수 초기화..
    fun initValue()
    {
        m_arrPlace = ArrayList<place>()//placelist
    }
    //--------------------------------------------------------------
    //전달받은 인텐트 데이터
    fun recvIntentData()
    {
        var pIntent: Intent = intent

        if(pIntent == null)
            return

    }
    //--------------------------------------------------------------
    //레이아웃 초기동작
    fun initLayout()
    {
        toolbar.title = m_Context!!.resources!!.getString(R.string.favorite_place)
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

        getFavoriteList()
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
        getFavoriteList()
    }
    //-------------------------------------------------------------
    //
    fun getFavoriteList()
    {
        progressBar.visibility = View.VISIBLE


        var pQuery:Query = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)
                .child(m_App!!.m_SpCtrl!!.getSpUserKey())
                .orderByChild("favorite").equalTo("Y")//favorite 상태가 Y인것만 호출

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
                }
                else
                {
                    ly_NoData.visibility = View.VISIBLE
                }
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(p0: DatabaseError?)
            {

            }
        })
    }

    //-----------------------------------------------------
    //
    fun menuItemShowMap(){
        if(m_PlaceAdapter == null)
        {
            Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.str_no_exit_location), Toast.LENGTH_SHORT).show()
            return
        }

        if(m_PlaceAdapter!!.itemCount <= 0)
        {
            Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.str_no_exit_location), Toast.LENGTH_SHORT).show()
            return
        }

        if(m_arrPlace == null)
            return

        if(m_arrPlace!!.size == 0)
            return

        //지도화면으로 이동.
        var pIntent = Intent(m_Context, ActMapDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_PLACE_LIST_OBJECT, m_arrPlace!! as Serializable)
        startActivity(pIntent)
        return
    }

    /************************* listener *************************/

    /************************* callback function *************************/
    //-----------------------------------------------------
    //SwipeRefresh Callback
    override fun onRefresh()
    {
        setRefresh()
    }
    //-----------------------------------------------------
    //리스트 어댑터 callback
    override fun checkPermission(): Boolean {
        return true
    }
    //----------------------------------------------------------------------
    //리스트 어댑터 callback
    override fun deleteProc(pInfo: place, position:Int)
    {
        return
    }
    //----------------------------------------------------------------------
    //리스트 어댑터 callback
    override fun moveDetailActivity(pInfo: place)
    {
        var pIntent = Intent(m_Context, ActPlaceDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_PLACE_OBJECT, pInfo as Serializable)
        startActivityForResult(pIntent, 0)
    }
    /*********************** interface ***********************/
}
