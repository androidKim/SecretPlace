package com.midas.secretplace.ui.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.midas.secretplace.ui.frag.main.FrGroup
import com.midas.secretplace.ui.frag.main.FrPlace

class MainPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm)
{
    /************************** Define **************************/
    private val TAB_NAME_0 = "One Place"
    private val TAB_NAME_1 = "Group Place"
    private val COUNT = 2
    /************************** System Fucntion **************************/
    //--------------------------------------------------------
    //
    override fun getItem(position: Int): Fragment?
    {
        var fragment: Fragment? = null
        when (position)
        {
            0 -> fragment = FrPlace()
            1 -> fragment = FrGroup()
        }

        return fragment
    }
    //--------------------------------------------------------
    //
    override fun getCount(): Int
    {
        return COUNT
    }
    //--------------------------------------------------------
    //
    override fun getPageTitle(position: Int): CharSequence?
    {
        if(position == 0)
        {
            return TAB_NAME_0
        }
        else if(position == 1)
        {
            return TAB_NAME_1
        }
        else
        {
            return ""
        }
    }
}
