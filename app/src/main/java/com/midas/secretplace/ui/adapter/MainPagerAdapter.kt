package com.midas.secretplace.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.midas.secretplace.ui.frag.main.FrMap
import com.midas.secretplace.ui.frag.main.FrPlace




class MainPagerAdapter internal constructor(pContext: Context, fm: FragmentManager) : FragmentPagerAdapter(fm)
{
    companion object {
        val TAB_INDEX_FRPALCE = 0
        val TAB_INDEX_FRMAP = 1
    }
    /************************** Define **************************/

    private var TAB_NAME_0 = "리스트"
    private var TAB_NAME_1 = "지도"
    private var COUNT = 2

    /************************** Member **************************/
    private var fragPlace:FrPlace? = null
    private var fragMap: FrMap?=null
    /************************** System Fucntion **************************/
    //--------------------------------------------------------
    //
    override fun getItem(position: Int): Fragment?
    {
        var fragment: Fragment? = null
        when (position)
        {
            0 -> {
                fragment = FrPlace()
            }
            1 -> {
                fragment = FrMap()
            }
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
    //탭 뷰 상단 제목
    override fun getPageTitle(position: Int): CharSequence?
    {
        if(position == 0) {
            return TAB_NAME_0
        }else if(position == 1){
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
            TAB_INDEX_FRPALCE -> {
                fragPlace = createdFragment as FrPlace
            }
            TAB_INDEX_FRMAP -> {
                fragMap = createdFragment as FrMap
            }
        }
        return createdFragment
    }
}
