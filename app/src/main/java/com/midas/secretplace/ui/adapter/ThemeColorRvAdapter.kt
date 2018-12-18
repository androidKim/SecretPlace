package com.midas.mytimeline.ui.adapter


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.theme

class ThemeColorRvAdapter(val context: Context, var themeList: ArrayList<theme>, var m_IfCallback:ifCallback) :
        RecyclerView.Adapter<ThemeColorRvAdapter.Holder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder
    {
        val view = LayoutInflater.from(context).inflate(R.layout.row_theme, parent, false)
        val holder:Holder = Holder(view)
        return holder
    }

    //-----------------------------------------------------------
    //
    override fun getItemCount(): Int
    {
        return themeList.size
    }
    //-----------------------------------------------------------
    //
    override fun onBindViewHolder(holder: Holder, position: Int)
    {
        holder?.bind(themeList[position], context)
    }

    //-----------------------------------------------------------
    //
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var ly_Row = itemView?.findViewById<RelativeLayout>(R.id.ly_Row)
        var tv_ColorName = itemView?.findViewById<TextView>(R.id.tv_ColorName)
        var iv_Color = itemView?.findViewById<ImageView>(R.id.iv_Color)

        fun bind (pInfo: theme, pContext: Context)
        {

            when(pInfo.colorName){
                Constant.THEME_PINK -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryPink))
                    tv_ColorName!!.text = "Pink"
                }
                Constant.THEME_RED -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryRed))
                    tv_ColorName!!.text = "Red"
                }
                Constant.THEME_PUPLE -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryPuple))
                    tv_ColorName!!.text = "Puple"
                }
                Constant.THEME_DEEPPUPLE -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryDeepPuple))
                    tv_ColorName!!.text = "DeepPuple"
                }
                Constant.THEME_INDIGO -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryIndigo))
                    tv_ColorName!!.text = "Indigo"
                }
                Constant.THEME_BLUE -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryBlue))
                    tv_ColorName!!.text = "Blue"
                }
                Constant.THEME_LIGHTBLUE -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryLightBlue))
                    tv_ColorName!!.text = "LightBlue"
                }
                Constant.THEME_CYAN ->
                {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryCyan))
                    tv_ColorName!!.text = "Cyan"
                }
                Constant.THEME_TEAL -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryTeal))
                    tv_ColorName!!.text = "Teal"
                }
                Constant.THEME_GREEN -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryGreen))
                    tv_ColorName!!.text = "Green"
                }
                Constant.THEME_LIGHTGREEN -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryLightGreen))
                    tv_ColorName!!.text = "LightGreen"
                }
                Constant.THEME_LIME -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryLime))
                    tv_ColorName!!.text = "Lime"
                }
                Constant.THEME_YELLOW -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryYellow))
                    tv_ColorName!!.text = "Yellow"
                }
                Constant.THEME_AMBER -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryAmber))
                    tv_ColorName!!.text = "Amber"
                }
                Constant.THEME_ORANGE -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryOrange))
                    tv_ColorName!!.text = "Orange"
                }
                Constant.THEME_DEEPORANGE -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryDeepOrange))
                    tv_ColorName!!.text = "DeepOrange"
                }
                Constant.THEME_BROWN -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryBrown))
                    tv_ColorName!!.text = "Brown"
                }
                Constant.THEME_GRAY -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryGray))
                    tv_ColorName!!.text = "Gray"
                }
                Constant.THEME_BLUEGRAY -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimaryBlueGray))
                    tv_ColorName!!.text = "BlueGray"
                }
                else -> {
                    iv_Color!!.setBackgroundColor(context!!.resources.getColor(R.color.colorPrimary))
                    tv_ColorName!!.text = "Default"
                }
            }

            ly_Row!!.setTag(pInfo)
            ly_Row!!.setOnClickListener(onClickThemeColor)
        }
    }

    /*********************** User Function ***********************/

    //-----------------------------------------------------------
    //
    fun addData(pInfo:theme)
    {
        if(pInfo == null)
            return

        this.themeList.add(pInfo)
        notifyDataSetChanged()
    }
    //-----------------------------------------------------------
    //
    fun clearData()
    {
        this.themeList.clear()
        notifyDataSetChanged()
    }

    //-----------------------------------------------------------
    //
    fun refreshData(pArray:ArrayList<theme>)
    {
        this.themeList = pArray
        notifyDataSetChanged()
    }

    //----------------------------------------------------------------------------
    //
    fun themeSelect(pInfo:theme)
    {
        if(m_IfCallback != null)
            m_IfCallback.themeSelect(pInfo)
    }

    /*********************** Listener ***********************/
    //----------------------------------------------------------------------------
    //onClick Go Deltail
    val onClickThemeColor = View.OnClickListener { view ->
        when(view.getId())
        {
            R.id.ly_Row -> themeSelect(view.tag as theme)
        }
    }

    /*********************** interface ***********************/
    interface ifCallback
    {
        fun themeSelect(pInfo:theme)
    }
}