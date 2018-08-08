package com.midas.mytimeline.ui.adapter


import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.distance
import com.midas.secretplace.ui.act.ActDistanceDetail
import java.io.Serializable

class DistanceRvAdapter(val context: Context, var distanceList: ArrayList<distance>) :
        RecyclerView.Adapter<DistanceRvAdapter.Holder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder
    {
        val view = LayoutInflater.from(context).inflate(R.layout.row_distance, parent, false)
        val holder:Holder = Holder(view)
        return holder
    }

    //-----------------------------------------------------------
    //
    override fun getItemCount(): Int
    {
        return distanceList.size
    }
    //-----------------------------------------------------------
    //
    override fun onBindViewHolder(holder: Holder, position: Int)
    {
        holder?.bind(distanceList[position], context)


    }
    //-----------------------------------------------------------
    //
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var ly_Row = itemView?.findViewById<RelativeLayout>(R.id.ly_Row)
        var tv_Name = itemView?.findViewById<TextView>(R.id.tv_Name)

        fun bind (pInfo: distance, pContext: Context)
        {
            tv_Name!!.text = pInfo.name
            ly_Row?.setTag(pInfo)
            ly_Row?.setOnClickListener(onClickGoDetail)
        }
    }
    /*********************** User Function ***********************/
    //-----------------------------------------------------------
    //
    fun addData(pInfo:distance)
    {
        if(pInfo == null)
            return

        this.distanceList.add(pInfo)
        notifyDataSetChanged()
    }
    //-----------------------------------------------------------
    //
    fun clearData()
    {
        this.distanceList.clear()
        notifyDataSetChanged()
    }
    //-----------------------------------------------------------
    //
    fun refreshData(pArray:ArrayList<distance>)
    {
        this.distanceList = pArray
        notifyDataSetChanged()
    }
    //----------------------------------------------------------------------------
    //
    fun goDetail(view:View)
    {
        val pInfo:distance = view.getTag() as distance
        var pIntent = Intent(view.context, ActDistanceDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_DISTANCE_OBJECT, pInfo as Serializable)
        view.context.startActivity(pIntent)
    }
    /*********************** Listener ***********************/
    //----------------------------------------------------------------------------
    //
    val onClickGoDetail = View.OnClickListener { view ->
        when(view.getId())
        {
            R.id.ly_Row -> goDetail(view)
        }
    }
}