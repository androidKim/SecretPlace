
package com.midas.secretplace.ui.setting


import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util

class ActSetting:AppCompatActivity()
{

    /*********************** Define ***********************/

    /*********************** Member ***********************/

    private var m_App: MyApp? = null
    private var m_Context: Context? = null

    /*********************** Controller ***********************/

    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        m_Context = this
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActSetting)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)


        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, FrSetting())
                .commit()
    }

}
