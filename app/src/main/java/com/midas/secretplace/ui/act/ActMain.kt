
package com.midas.secretplace.ui.act

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.view.MenuItem
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
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
import com.midas.secretplace.ui.custom.dlg_theme_setting
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_main.*
import kotlinx.android.synthetic.main.dlg_theme_setting.view.*
import kotlinx.android.synthetic.main.ly_main.*


class ActMain:ActBase(), NavigationView.OnNavigationItemSelectedListener, ThemeColorRvAdapter.ifCallback
{

    //----------------------------------------------------------
    //filter
    inline fun Activity.showThemeSettingDialog(func: dlg_theme_setting.() -> Unit): AlertDialog =
            dlg_theme_setting(this).apply {
                func()
            }.create()

    inline fun Fragment.showThemeSettingDialog(func: dlg_theme_setting.() -> Unit): AlertDialog =
            dlg_theme_setting(this.context!!).apply {
                func()
            }.create()

    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App: MyApp? = null
    var m_Context: Context? = null

    private val mScaleGestureDetector: ScaleGestureDetector? = null
    private val mScaleFactor = 1.0f
    /*********************** Controller ***********************/
    private var m_iv_Profile: ImageView? = null
    private var m_iv_None:ImageView? = null
    var m_ThemeSettingDialog:AlertDialog? = null
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

        initValue()
        recvIntentData()
        initLayout()
    }
    //--------------------------------------------------------------
    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
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
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        if (viewPager != null)
        {
            val adapter = MainPagerAdapter(m_Context!!, supportFragmentManager)
            viewPager.adapter = adapter
        }

        settingDrawerView()
        settingView()
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
        var v_Header:View? = navigation_view!!.getHeaderView(0)
        m_iv_Profile = v_Header!!.findViewById(R.id.iv_Profile)
        m_iv_None = v_Header!!.findViewById(R.id.iv_None)
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
            {
                Glide.with(this).load(pInfo.img_url).into(m_iv_Profile)
                m_iv_Profile!!.visibility = View.VISIBLE
                m_iv_None!!.visibility = View.GONE
            }
            else
            {
                m_iv_Profile!!.visibility = View.GONE
                m_iv_None!!.visibility = View.VISIBLE
            }
        }
        else
        {
            m_iv_Profile!!.visibility = View.GONE
            m_iv_None!!.visibility = View.VISIBLE
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
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE).orderByChild("user_key").equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())//.limitToFirst(ReqBase.ITEM_COUNT)
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

                        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)!!.child(pInfo.place_key)//where
                        pDbRef!!.removeValue()

                        //file storage remove
                        storageDeleteItemProc(pInfo.place_key!!)

                        //file data remove
                        pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child(pInfo.place_key)//where
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
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP_PLACE).orderByChild("user_key").equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())//.limitToFirst(ReqBase.ITEM_COUNT)
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

                        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP_PLACE)!!.child(pInfo.place_key)//where
                        pDbRef!!.removeValue()

                        //file storage remove
                        storageDeleteItemProc(pInfo.place_key!!)

                        //file data remove
                        pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child(pInfo.place_key)//where
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
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP).orderByChild("user_key").equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())//.limitToFirst(ReqBase.ITEM_COUNT)
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

                        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_GROUP)!!.child(pInfo.group_key)//where
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
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_IMG)!!.child(placeKey).child("img_list")//where
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
                        var fileNm:String = it!!.getValue(String::class.java)!!

                        //split ?
                        var arrTemp:List<String> = fileNm.split("?")
                        fileNm = arrTemp.get(0)
                        //split "/"  get lastItem is FileName
                        arrTemp = fileNm.split("/")
                        fileNm = arrTemp.get(arrTemp.size - 1)

                        // Create a reference to the file to delete
                        var desertRef = storageRef.reference.child(fileNm)//test..
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
        pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER).child(m_App!!.m_SpCtrl!!.getSpUserKey())//.limitToFirst(ReqBase.ITEM_COUNT)
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

    /*********************** listener ***********************/
    //--------------------------------------------------------------
    //
    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        var result = false

        when(item.itemId)
        {
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
            R.id.theme ->//테마설정
            {
                showThmeSelectDialog()
                return true
            }
            R.id.logout ->//로그아웃
            {
                showLogoutDialog()
                return true
            }
            R.id.delete ->//회원탈퇴
            {
                showMemberDeleteDialog()
                return true
            }
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
    /*********************** util ***********************/
}
