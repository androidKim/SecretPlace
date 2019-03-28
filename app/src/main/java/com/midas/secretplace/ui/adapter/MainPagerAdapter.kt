package com.midas.secretplace.ui.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup
import com.midas.secretplace.R
import com.midas.secretplace.ui.frag.main.FrGroup
import com.midas.secretplace.ui.frag.main.FrPlace




class MainPagerAdapter internal constructor(pContext: Context, fm: FragmentManager) : FragmentPagerAdapter(fm)
{
    companion object {
        val TAB_INDEX_FRPALCE = 0;
        val TAB_INDEX_FRGROUP = 0;
    }

    /************************** Define **************************/

    private var TAB_NAME_0 = pContext.resources.getString(R.string.str_tab_title_my)
    private var TAB_NAME_1 = pContext.resources.getString(R.string.str_tab_title_group)
    private var TAB_NAME_2 = pContext.resources.getString(R.string.str_tab_title_couple)
    private var COUNT = 2

    /************************** Member **************************/
    private var fragPlace:FrPlace? = null
    private var fragGroup:FrGroup? = null
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

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position)

        val createdFragment = super.instantiateItem(container, position) as Fragment
        // save the appropriate reference depending on position
        when (position) {
            TAB_INDEX_FRPALCE -> fragPlace = createdFragment as FrPlace
            TAB_INDEX_FRGROUP -> fragGroup = createdFragment as FrGroup
        }
        return createdFragment
    }
}
