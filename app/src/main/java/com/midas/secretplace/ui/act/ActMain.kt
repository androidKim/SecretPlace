
package com.midas.secretplace.ui.act

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.midas.mytimeline.ui.adapter.ThemeColorRvAdapter
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.service.MyJobService
import com.midas.secretplace.structure.core.theme
import com.midas.secretplace.structure.core.user
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
            val adapter = MainPagerAdapter(supportFragmentManager)
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
    fun setToolbarBackgroundColor(strTheme:String)
    {
        when(strTheme)
        {
            Constant.THEME_PINK -> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDarkPink))
            Constant.THEME_RED -> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDarkRed))
            Constant.THEME_PUPLE -> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDarkPuple))
            Constant.THEME_DEEPPUPLE -> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDarkDeepPuple))
            Constant.THEME_INDIGO-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDarkIndigo))
            Constant.THEME_BLUE-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDarkBlue))
            Constant.THEME_LIGHTBLUE-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDarkLightBlue))
            Constant.THEME_CYAN-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDarkCyan))
            Constant.THEME_TEAL-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryTeal))
            Constant.THEME_GREEN-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryGreen))
            Constant.THEME_LIGHTGREEN-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryLightGreen))
            Constant.THEME_LIME-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryLime))
            Constant.THEME_YELLOW-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryYellow))
            Constant.THEME_AMBER-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryAmber))
            Constant.THEME_ORANGE-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryOrange))
            Constant.THEME_DEEPORANGE-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDeepOrange))
            Constant.THEME_BROWN-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryBrown))
            Constant.THEME_GRAY-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryGray))
            Constant.THEME_BLUEGRAY-> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryBlueGray))
            else -> toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        }
    }
    //--------------------------------------------------------------
    //
    fun settingDrawerView()
    {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        var strTheme:String = m_App!!.m_SpCtrl!!.getSpTheme()!!
        setToolbarBackgroundColor(strTheme!!)

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

    /*********************** listener ***********************/
    //--------------------------------------------------------------
    //
    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        var result = false

        when(item.itemId)
        {
            R.id.theme ->
            {
                showThmeSelectDialog()
                return true
            }
            R.id.logout ->
            {
                showLogoutDialog()
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
