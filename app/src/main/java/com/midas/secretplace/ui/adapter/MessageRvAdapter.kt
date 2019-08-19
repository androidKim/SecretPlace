package com.midas.mytimeline.ui.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.midas.secretplace.R
import com.midas.secretplace.structure.core.message

class MessageRvAdapter(val context: Context, var messageList: ArrayList<message>, var user_key:String, var m_IfCallback:ifCallback) :
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
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView!!)
    {
        //left user 1(me)
        var ly_User1 = itemView?.findViewById<RelativeLayout>(R.id.ly_User1)
        var tv_Name1 = itemView?.findViewById<TextView>(R.id.tv_Name1)
        var tv_Message1 = itemView?.findViewById<TextView>(R.id.tv_Message1)
        //right user 2(target)
        var ly_User2 = itemView?.findViewById<RelativeLayout>(R.id.ly_User2)
        var tv_Name2 = itemView?.findViewById<TextView>(R.id.tv_Name2)
        var tv_Message2 = itemView?.findViewById<TextView>(R.id.tv_Message2)



        fun bind (pInfo: message, pContext: Context)
        {
            if(user_key.equals(pInfo.user_key))
            {
                ly_User1!!.visibility = View.VISIBLE
                ly_User2!!.visibility = View.GONE
                tv_Name1!!.text = pInfo.name
                tv_Message1!!.text = pInfo.msssage
            }
            else
            {
                ly_User1!!.visibility = View.GONE
                ly_User2!!.visibility = View.VISIBLE
                tv_Name2!!.text = pInfo.name
                tv_Message2!!.text = pInfo.msssage
            }
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