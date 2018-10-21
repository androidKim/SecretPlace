package com.midas.mytimeline.ui.adapter


import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.group
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.act.ActGroupDetail
import com.midas.secretplace.ui.act.ActPlaceDetail
import java.io.Serializable

class GroupRvAdapter(val m_Context: Context, var m_arrGroup: ArrayList<group>, var m_IfCallback:ifCallback) :
        RecyclerView.Adapter<GroupRvAdapter.Holder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder
    {
        val view = LayoutInflater.from(m_Context).inflate(R.layout.row_group, parent, false)
        val holder:Holder = Holder(view)
        return holder
    }

    //-----------------------------------------------------------
    //
    override fun getItemCount(): Int
    {
        return m_arrGroup.size
    }
    //-----------------------------------------------------------
    //
    override fun onBindViewHolder(holder: Holder, position: Int)
    {
        holder?.bind(m_arrGroup[position], m_Context)
    }

    //-----------------------------------------------------------
    //
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var ly_Row = itemView?.findViewById<RelativeLayout>(R.id.ly_Row)
        var ly_Delete = itemView?.findViewById<LinearLayout>(R.id.ly_Delete)
        var tv_Name = itemView?.findViewById<TextView>(R.id.tv_Name)

        fun bind (pInfo: group, pContext: Context)
        {
            tv_Name!!.text = pInfo.name
            ly_Row!!.setTag(pInfo)
            ly_Row!!.setOnClickListener(onClickGoDetail)

            ly_Delete!!.setTag(pInfo)
            ly_Delete!!.setOnClickListener(onClickDelete)
        }
    }

    /*********************** User Function ***********************/

    //-----------------------------------------------------------
    //
    fun addData(pInfo:group)
    {
        if(pInfo == null)
            return

        this.m_arrGroup.add(pInfo)
        notifyDataSetChanged()
    }
    //-----------------------------------------------------------
    //
    fun clearData()
    {
        this.m_arrGroup.clear()
        notifyDataSetChanged()
    }

    //-----------------------------------------------------------
    //
    fun refreshData(pArray:ArrayList<group>)
    {
        this.m_arrGroup = pArray
        notifyDataSetChanged()
    }
    //----------------------------------------------------------------------------
    //
    fun goDetail(view:View)
    {
        if(m_IfCallback != null)
        {
            var bPermission:Boolean = m_IfCallback.checkPermission()
            if(bPermission)
            {
                val pInfo:group = view.getTag() as group
                if(m_IfCallback != null)
                {
                    m_IfCallback!!.moveDetailActivity(pInfo!!)
                }

                return
            }
            else
            {
                Toast.makeText(m_Context, m_Context.resources.getString(R.string.str_msg_7), Toast.LENGTH_SHORT).show()
                return
            }
        }
    }

    //----------------------------------------------------------------------------
    //
    fun deleteGroupInfo(pInfo:group)
    {
        if(pInfo == null)
            return

        //using interface..
        if(m_IfCallback != null)
            m_IfCallback.deleteGroupProc(pInfo)
    }

    /*********************** Listener ***********************/
    //----------------------------------------------------------------------------
    //onClick Go Deltail
    val onClickGoDetail = View.OnClickListener { view ->
        when(view.getId())
        {
            R.id.ly_Row -> goDetail(view)
        }
    }
    //----------------------------------------------------------------------------
    //onClick Delete
    val onClickDelete = View.OnClickListener{view->
        val pInfo:group = view.getTag() as group

        val builder = AlertDialog.Builder(m_Context!!)
        builder.setMessage("Delete?")
        builder.setPositiveButton(m_Context.getString(R.string.str_ok)){dialog, which ->
            //show dialog..
            deleteGroupInfo(pInfo)
        }

        builder.setNegativeButton(m_Context.getString(R.string.str_no)){dialog,which ->

        }

        builder.setNeutralButton(m_Context.getString(R.string.str_cancel)){_,_ ->

        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /*********************** interface ***********************/
    interface ifCallback
    {
        fun deleteGroupProc(pInfo:group)
        fun checkPermission():Boolean
        fun moveDetailActivity(pInfo:group)
    }
}