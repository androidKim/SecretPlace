package com.midas.mytimeline.ui.adapter


import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.midas.secretplace.R
import com.midas.secretplace.structure.core.place
import java.lang.Exception


class PlaceRvAdapter(val context: Context, var requestManager:RequestManager, var placeList: ArrayList<place>, var m_IfCallback:ifCallback) :
RecyclerView.Adapter<PlaceRvAdapter.ViewHolder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(context).inflate(R.layout.row_place, parent, false)
        val holder:ViewHolder = ViewHolder(view)
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
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        var pInfo: place = placeList[position]

        if(!pInfo.group_key.equals(""))//그룹 위치 리스트
        {

        }
        else//단일 위치리스트..
        {

        }

        //..
        if(!pInfo.img_url.equals(""))
        {
            requestManager
                    .load(pInfo.img_url)
                    .listener(object : RequestListener<Drawable>
                    {
                        override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean
                        {
                            holder.ivThumbnail!!.setBackgroundResource(R.drawable.ic_image_black_100dp)
                            return false
                        }
                        override fun onResourceReady(p0: Drawable?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean
                        {
                            //do something when picture already loaded

                            return false
                        }
                    })
                    .into(holder.ivThumbnail)
        }
        else
        {
            requestManager.clear(holder.ivThumbnail)
            holder.ivThumbnail!!.setBackgroundResource(R.drawable.ic_image_black_100dp)
        }

        if(!pInfo.name.equals(""))//위치명
        {
            holder.tv_Name!!.text = pInfo.name
            holder.tv_Name!!.visibility = View.VISIBLE
        }
        else
        {
            holder.tv_Name!!.visibility = View.INVISIBLE
        }

        if(!pInfo.address.equals(""))//주소
        {
            holder.tvAddress!!.text = pInfo.address
            holder.tvAddress!!.visibility = View.VISIBLE
        }
        else
        {
            holder.tvAddress!!.visibility = View.INVISIBLE
        }

        //위도경도
        if(!pInfo.lat.equals("") && !pInfo.lng.equals(""))
        {
            var strLatLng:String = String.format(context!!.resources.getString(R.string.str_latlng_format), pInfo.lat, pInfo.lng)
            holder.tvLatLng!!.text = strLatLng
            holder.tvLatLng!!.visibility = View.VISIBLE
        }
        else
        {
            holder.tvLatLng!!.visibility = View.INVISIBLE
        }
        holder.ly_Row!!.setTag(pInfo)
        holder.ly_Row!!.setOnClickListener(onClickGoDetail)
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
        this.placeList = ArrayList()
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
                val pInfo:place = view.tag as place
                if(m_IfCallback != null)
                {
                    m_IfCallback!!.moveDetailActivity(pInfo)
                }
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
    fun removeAt(position: Int) {
        var pInfo:place = placeList.get(position)
        placeList.removeAt(position)
        if(m_IfCallback != null)
            m_IfCallback.deleteProc(pInfo)

        notifyItemRemoved(position)
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
    /*********************** interface ***********************/
    interface ifCallback
    {
        fun deleteProc(pInfo:place)
        fun checkPermission():Boolean
        fun moveDetailActivity(pInfo:place)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var ly_Row: RelativeLayout
        var tv_Name: TextView
        var tvAddress: TextView
        var tvLatLng: TextView
        var ivThumbnail:AppCompatImageView
        init {
            ly_Row = itemView?.findViewById<RelativeLayout>(R.id.ly_Row)
            tv_Name = itemView?.findViewById<TextView>(R.id.tv_Name)
            tvAddress = itemView?.findViewById<TextView>(R.id.tvAddress)
            tvLatLng = itemView?.findViewById<TextView>(R.id.tvLatLng)
            ivThumbnail = itemView?.findViewById<AppCompatImageView>(R.id.ivThumbnail)
        }
    }
}