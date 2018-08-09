
package com.midas.secretplace.ui.act

import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.midas.secretplace.R
import com.midas.secretplace.service.MyJobService
import com.midas.secretplace.structure.core.user
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.adapter.MainPagerAdapter
import kotlinx.android.synthetic.main.act_main.*
import kotlinx.android.synthetic.main.ly_main.*


class ActMain:ActBase(), NavigationView.OnNavigationItemSelectedListener
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App: MyApp? = null
    var m_Context: Context? = null

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


        initValue()
        recvIntentData()
        initLayout()
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
        var v_Header:View? = navigation_view!!.getHeaderView(0)
        m_iv_Profile = v_Header!!.findViewById(R.id.iv_Profile)
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
            R.id.logout ->
            {
                showLogoutDialog()
            }

            R.id.other_menu_option_one ->
            {
                Toast.makeText(this@ActMain,"Otra Opcion Uno Seleccionada",Toast.LENGTH_LONG).show()
                result = true
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return result
    }



    /*********************** util ***********************/
}
