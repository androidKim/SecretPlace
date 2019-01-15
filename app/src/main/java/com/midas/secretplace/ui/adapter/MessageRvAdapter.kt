package com.midas.mytimeline.ui.adapter


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.midas.secretplace.R
import com.midas.secretplace.structure.core.message

class MessageRvAdapter(val context: Context, var messageList: ArrayList<message>, var m_IfCallback:ifCallback) :
        RecyclerView.Adapter<MessageRvAdapter.Holder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder
    {
        val view = LayoutInflater.from(context).inflate(R.layout.row_message, parent, false)
        val holder:Holder = Holder(view)
        return holder
    }

    //-----------------------------------------------------------
    //
    override fun getItemCount(): Int
    {
        return messageList.size
    }
    //-----------------------------------------------------------
    //
    override fun onBindViewHolder(holder: Holder, position: Int)
    {
        holder?.bind(messageList[position], context)
    }

    //-----------------------------------------------------------
    //
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var tv_Name = itemView?.findViewById<TextView>(R.id.tv_Name)
        var tv_Message = itemView?.findViewById<TextView>(R.id.tv_Message)

        fun bind (pInfo: message, pContext: Context)
        {
            tv_Name!!.text = pInfo.name
            tv_Message!!.text = pInfo.msssage
        }
    }

    /*********************** User Function ***********************/

    //-----------------------------------------------------------
    //
    fun addData(pInfo:message)
    {
        if(pInfo == null)
            return

        this.messageList.add(pInfo)
        notifyDataSetChanged()
    }
    //-----------------------------------------------------------
    //
    fun clearData()
    {
        this.messageList.clear()
        notifyDataSetChanged()
    }

    //-----------------------------------------------------------
    //
    fun refreshData(pArray:ArrayList<message>)
    {
        this.messageList = pArray
        notifyDataSetChanged()
    }

    /*********************** Listener ***********************/

    /*********************** interface ***********************/
    interface ifCallback
    {

    }
}