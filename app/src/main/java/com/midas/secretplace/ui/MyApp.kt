package com.midas.secretplace.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.core.SharedPreferenceCtrl
import com.midas.secretplace.ui.act.ActLogin
import com.midas.secretplace.ui.act.ActMain


class MyApp:MultiDexApplication()
{
    var m_Context: Context? = null
    var m_binit:Boolean = false
    var m_SpCtrl:SharedPreferenceCtrl? = null
    var m_FirebaseDbCtrl:FirebaseDbCtrl? = null

    var m_ProgressBar:ProgressBar? = null

    override fun attachBaseContext(base: Context)
    {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun init(pContext:Context)
    {
        if(m_binit == false)
        {
            m_Context = pContext;

            //sharedPreference..
            m_SpCtrl = SharedPreferenceCtrl()
            m_SpCtrl!!.init(m_Context!!)

            //firebase Db Ctrl
            m_FirebaseDbCtrl = FirebaseDbCtrl()

            //localDb.

            //device size

            //packgename

            //etc..

            m_binit = true
        }
    }

    //-----------------------------------------------
    //
    public fun goMain(pContext:Context)
    {
        if(pContext == null)
            return

        var pIntent = Intent(pContext, ActMain::class.java)
        pIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        pIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        pContext.startActivity(pIntent)
    }
    //-----------------------------------------------
    //
    public fun logoutProc(pContext:Context)
    {
        if(pContext == null)
            return

        var pIntent = Intent(pContext, ActLogin::class.java)
        pIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        pIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        pContext.startActivity(pIntent)

        m_SpCtrl?.clearData()
    }


    /*********************** Dialog ***********************/
    //-----------------------------------------------
    //
    fun showLoadingDialog(pView:LinearLayout)
    {
        m_ProgressBar = ProgressBar(m_Context)
        m_ProgressBar!!.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            m_ProgressBar!!.indeterminateDrawable = m_Context!!.getResources().getDrawable(R.drawable.custom_progress_dialog, null)
        else
            m_ProgressBar!!.indeterminateDrawable = m_Context!!.getResources().getDrawable(R.drawable.custom_progress_dialog)

        m_ProgressBar!!.isIndeterminate = true

        // Add ProgressBar to LinearLayout
        pView?.addView(m_ProgressBar)
    }

    //-----------------------------------------------
    //
    fun hideLoadingDialog(pView:LinearLayout)
    {
        pView.removeAllViews()
    }


}