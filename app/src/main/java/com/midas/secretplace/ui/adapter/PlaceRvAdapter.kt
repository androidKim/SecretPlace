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
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.act.ActPlaceDetail
import java.io.Serializable

class PlaceRvAdapter(val context: Context, var placeList: ArrayList<place>, var m_IfCallback:ifCallback) :
        RecyclerView.Adapter<PlaceRvAdapter.Holder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder
    {
        val view = LayoutInflater.from(context).inflate(R.layout.row_place, parent, false)
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
        var ly_Row = itemView?.findViewById<RelativeLayout>(R.id.ly_Row)
        var ly_Delete = itemView?.findViewById<LinearLayout>(R.id.ly_Delete)
        var tv_Name = itemView?.findViewById<TextView>(R.id.tv_Name)

        fun bind (pInfo: place, pContext: Context)
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
    fun goDetail(view:View)
    {
        if(m_IfCallback != null)
        {
            var bPermission:Boolean = m_IfCallback.checkPermission()
            if(bPermission)
            {
                val pInfo:place = view.getTag() as place
                var pIntent = Intent(view.context, ActPlaceDetail::class.java)
                pIntent.putExtra(Constant.INTENT_DATA_PLACE_OBJECT, pInfo as Serializable)
                view.context.startActivity(pIntent)
                return
            }
            else
            {
                Toast.makeText(context, context.resources.getString(R.string.str_msg_7), Toast.LENGTH_SHORT).show()
                return
            }
        }
    }

    //----------------------------------------------------------------------------
    //
    fun deleteInfo(pInfo:place)
    {
        if(pInfo == null)
            return

        //using interface..
        if(m_IfCallback != null)
            m_IfCallback.deleteProc(pInfo)
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
        val pInfo:place = view.getTag() as place

        val builder = AlertDialog.Builder(context!!)
        builder.setMessage("Delete?")
        builder.setPositiveButton(context.getString(R.string.str_ok)){dialog, which ->
            //show dialog..
            deleteInfo(pInfo)
        }

        builder.setNegativeButton(context.getString(R.string.str_no)){dialog,which ->

        }

        builder.setNeutralButton(context.getString(R.string.str_cancel)){_,_ ->

        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /*********************** interface ***********************/
    interface ifCallback
    {
        fun deleteProc(pInfo:place)
        fun checkPermission():Boolean
    }
}