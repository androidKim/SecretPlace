package com.midas.mytimeline.ui.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.midas.secretplace.R
import com.midas.secretplace.structure.core.couple

class RequestForMeRvAdapter(val context: Context, var requestList: ArrayList<couple>, var pIfCallback:ifCallback) :
        RecyclerView.Adapter<RequestForMeRvAdapter.Holder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder
    {
        val view = LayoutInflater.from(context).inflate(R.layout.row_request_for_me, parent, false)
        val holder:Holder = Holder(view)
        return holder
    }

    //-----------------------------------------------------------
    //
    override fun getItemCount(): Int
    {
        return requestList.size
    }
    //-----------------------------------------------------------
    //
    override fun onBindViewHolder(holder: Holder, position: Int)
    {
        holder?.bind(requestList[position], context)
    }

    //-----------------------------------------------------------
    //
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView!!)
    {
        var ly_Row = itemView?.findViewById<RelativeLayout>(R.id.ly_Row)
        var tv_UserKey = itemView?.findViewById<TextView>(R.id.tv_RequesterKey)
        var iv_Accept = itemView?.findViewById<ImageView>(R.id.iv_Accept)

        fun bind (pInfo: couple, pContext: Context)
        {
            tv_UserKey!!.text = pInfo.requester_key
            if(pInfo.accept.equals(couple.APPCET_Y))
            {
                iv_Accept!!.setBackgroundResource(R.drawable.baseline_favorite_black_48)
            }
            else
            {
                iv_Accept!!.setBackgroundResource(R.drawable.baseline_favorite_border_black_48)
            }

            ly_Row!!.tag = pInfo
            ly_Row!!.setOnClickListener { onClickAcceptOnOff(pInfo) }
        }
    }

    /*********************** User Function ***********************/

    //-----------------------------------------------------------
    //
    fun addData(pInfo:couple)
    {
        if(pInfo == null)
            return

        this.requestList.add(pInfo)
        notifyDataSetChanged()
    }
    //-----------------------------------------------------------
    //
    fun clearData()
    {
        this.requestList.clear()
        notifyDataSetChanged()
    }

    //-----------------------------------------------------------
    //
    fun refreshData(pArray:ArrayList<couple>)
    {
        this.requestList = pArray
        notifyDataSetChanged()
    }
    //----------------------------------------------------------------------------
    //
    fun onClickAcceptOnOff(pInfo: couple)
    {
        if(pIfCallback != null)
        {
            pIfCallback.setRequestOnOffProc(pInfo)
        }
    }

    /*********************** Listener ***********************/
    /*********************** interface ***********************/
    interface ifCallback
    {
        fun setRequestOnOffProc(pInfo: couple)
    }
}