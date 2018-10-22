package com.midas.mytimeline.ui.adapter


import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.place
import java.io.Serializable

class HorizontalPlaceRvAdapter(val context: Context, var placeList: ArrayList<place>, var m_IfCallback:ifCallback) :
        RecyclerView.Adapter<HorizontalPlaceRvAdapter.Holder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder
    {
        val view = LayoutInflater.from(context).inflate(R.layout.row_horizontal_place, parent, false)
        val holder:Holder = Holder(view)
        return holder
    }

    //-----------------------------------------------------------
    //
    override fun getItemCount(): Int
    {
        return placeList.size
    }
    //-----------------------------------------------------------
    //
    override fun onBindViewHolder(holder: Holder, position: Int)
    {
        holder?.bind(placeList[position], context)
    }

    //-----------------------------------------------------------
    //
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var ly_Row = itemView?.findViewById<LinearLayout>(R.id.ly_Row)
        var ly_Delete = itemView?.findViewById<LinearLayout>(R.id.ly_Delete)
        var tv_Nmae = itemView?.findViewById<TextView>(R.id.tv_Name)
        var tv_Lat = itemView?.findViewById<TextView>(R.id.tv_Lat)
        var tv_Lng = itemView?.findViewById<TextView>(R.id.tv_Lng)

        fun bind (pInfo: place, pContext: Context)
        {
            tv_Nmae!!.text = pInfo.name
            tv_Lat!!.text = pInfo.lat
            tv_Lng!!.text = pInfo.lng
            ly_Row!!.setTag(pInfo)
            ly_Row!!.setOnClickListener(onClickPlaceItem)

            ly_Delete!!.setTag(pInfo)
            ly_Delete!!.setOnClickListener(onClickDeleteItem)
        }
    }

    /*********************** User Function ***********************/

    //-----------------------------------------------------------
    //
    fun addData(pInfo:place)
    {
        if(pInfo == null)
            return

        this.placeList.add(pInfo)
        notifyDataSetChanged()
    }
    //-----------------------------------------------------------
    //
    fun clearData()
    {
        this.placeList.clear()
        notifyDataSetChanged()
    }

    //-----------------------------------------------------------
    //
    fun refreshData(pArray:ArrayList<place>)
    {
        this.placeList = pArray
        notifyDataSetChanged()
    }
    //----------------------------------------------------------------------------
    //
    fun selectPlaceItem(view:View)
    {
        if(m_IfCallback != null)
        {
            val pInfo:place = view.getTag() as place
            m_IfCallback!!.selectPlaceItem(pInfo)
        }
    }
    //----------------------------------------------------------------------------
    //
    fun deletePlaceItem(view:View)
    {
        if(m_IfCallback != null)
        {
            var pInfo:place = view.getTag() as place
            val builder = AlertDialog.Builder(context!!)
            builder.setMessage(context.getString(R.string.str_msg_19))
            builder.setPositiveButton(context.getString(R.string.str_ok)){dialog, which ->
                //show dialog..
                m_IfCallback!!.deletePlaceItem(pInfo)
            }

            builder.setNegativeButton(context.getString(R.string.str_no)){dialog,which ->

            }

            builder.setNeutralButton(context.getString(R.string.str_cancel)){_,_ ->

            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
    /*********************** Listener ***********************/
    //----------------------------------------------------------------------------
    //onClick Go Deltail
    val onClickPlaceItem = View.OnClickListener { view ->
        when(view.getId())
        {
            R.id.ly_Row -> selectPlaceItem(view)
        }
    }
    //----------------------------------------------------------------------------
    //delete item
    val onClickDeleteItem = View.OnClickListener { view ->
        when(view.getId())
        {
            R.id.ly_Delete -> deletePlaceItem(view)
        }
    }

    /*********************** interface ***********************/
    interface ifCallback
    {
        fun selectPlaceItem(pInfo:place)
        fun deletePlaceItem(pInfo:place)
    }
}