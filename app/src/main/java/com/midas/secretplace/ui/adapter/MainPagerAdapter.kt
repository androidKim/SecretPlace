package com.midas.secretplace.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.midas.secretplace.ui.frag.main.FrPlace




class MainPagerAdapter internal constructor(pContext: Context, fm: FragmentManager) : FragmentPagerAdapter(fm)
{
    companion object {
        val TAB_INDEX_FRPALCE = 0
        val TAB_INDEX_FRGROUP = 0
    }

    /************************** Define **************************/

    private var TAB_NAME_0 = "장소 리스트"
    private var COUNT = 1

    /************************** Member **************************/
    private var fragPlace:FrPlace? = null
    /************************** System Fucntion **************************/
    //--------------------------------------------------------
    //
    override fun getItem(position: Int): Fragment?
    {
        var fragment: Fragment? = null
        when (position)
        {
            0 -> fragment = FrPlace()
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
        }
        return createdFragment
    }
}
