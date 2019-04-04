package com.midas.secretplace.ui.setting

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import com.midas.secretplace.R
import com.midas.secretplace.ui.MyApp
import android.content.pm.PackageManager


/*
shared preference fragment
ActSetting에서  FrSetting Fragemnt View를 그린다..
 */
class FrSetting : PreferenceFragment()
{
    /**************************** Define ****************************/

    /**************************** Member ****************************/
    private var m_App:MyApp? = null
    private var prefs:SharedPreferences?=null
    /**************************** Controller ****************************/
    /**************************** system funtion ****************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(activity)

        addPreferencesFromResource(R.xml.pref_setting)


        prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        var prefVersion: Preference? = findPreference("pref_version")
        //버전
        try {
            val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            val version = pInfo.versionName
            prefVersion!!.setTitle(String.format("버전 %s", version))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        //로그아웃
        var prefLogout: Preference? = findPreference("pref_logout")
        prefLogout!!.setOnPreferenceClickListener (Preference.OnPreferenceClickListener {
            showLogoutDialog()
        })

    }
    /**************************** user funtion ****************************/
    //--------------------------------------------------------------
    //로그아웃 dialog
    fun showLogoutDialog():Boolean
    {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(getString(R.string.str_msg_2))
        builder.setPositiveButton(getString(R.string.str_ok)){dialog, which ->
            m_App!!.logoutProc(activity)
        }

        builder.setNegativeButton(getString(R.string.str_no)){dialog,which ->

        }

        builder.setNeutralButton(getString(R.string.str_cancel)){_,_ ->

        }

        val dialog: AlertDialog = builder.create()
        dialog.show()

        return false
    }
}
