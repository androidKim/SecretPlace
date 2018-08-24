package com.midas.secretplace.ui.adapter


import android.content.Context

import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.midas.secretplace.R
import com.midas.secretplace.structure.core.photo
import com.midas.secretplace.ui.frag.MapFragment
import android.view.InflateException



class PhotoRvAdapter(val context: Context, var photoList: ArrayList<photo>, var m_IfCallback:ifCallback, var m_FrManager: FragmentManager) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        if (viewType == TYPE_HEADER)
        {
            val view = LayoutInflater.from(context).inflate(R.layout.ly_place_detail_header, parent, false)
            val holder:HeaderHolder = HeaderHolder(view)
            return holder
        }
        else if (viewType == TYPE_ITEM)
        {
            val view = LayoutInflater.from(context).inflate(R.layout.row_photo, parent, false)
            val holder:Holder = Holder(view)
            return holder
        }
        throw RuntimeException("No match for $viewType.")
    }

    //-----------------------------------------------------------
    //
    override fun getItemCount(): Int
    {
        return photoList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val mObject = photoList[position]
        if (holder is HeaderHolder)
        {
            holder?.bind(photoList[position], context)
        }
        else if (holder is Holder)
        {
            holder?.bind(photoList[position], context)
        }
    }

    //-----------------------------------------------------------
    //
    override fun getItemViewType(position: Int): Int
    {
        return if(photoList[position].isHeader)
        {
            TYPE_HEADER
        }
        else
        {
            TYPE_ITEM
        }
    }
    //-----------------------------------------------------------
    //Header..
    inner class HeaderHolder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var tv_Name = itemView?.findViewById<TextView>(R.id.tv_Name)
        var iBtn_AddPhoto = itemView?.findViewById<ImageButton>(R.id.iBtn_AddPhoto)

        fun bind (pInfo: photo, pContext: Context)
        {
            tv_Name!!.text = pInfo.img_url

            //event..
            iBtn_AddPhoto!!.setOnClickListener(View.OnClickListener {
                if(m_IfCallback != null)
                    m_IfCallback.addPhoto()
            })
        }
    }


    //-----------------------------------------------------------
    //
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var ly_Row = itemView?.findViewById<RelativeLayout>(R.id.ly_Row)
        var tv_Name = itemView?.findViewById<TextView>(R.id.tv_Name)
        var iv_Photo = itemView?.findViewById<ImageView>(R.id.iv_Photo)

        fun bind (pInfo: photo, pContext: Context)
        {
            Glide.with(context).load(pInfo.img_url).into(iv_Photo)
            //ly_Row?.setTag(pInfo)
            //ly_Row?.setOnClickListener(onClickGoDetail)
        }
    }
    /*********************** User Function ***********************/
    //-----------------------------------------------------------
    //
    companion object
    {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    //-----------------------------------------------------------
    //
    fun addData(pInfo:photo)
    {
        if(pInfo == null)
            return

        this.photoList.add(pInfo)
        notifyDataSetChanged()
    }
    //-----------------------------------------------------------
    //
    fun clearData()
    {
        photoList.clear()
        notifyDataSetChanged()
    }

    //-----------------------------------------------------------
    //
    fun refreshData(pArray:ArrayList<photo>)
    {
        this.photoList = pArray
        notifyDataSetChanged()
    }
    //----------------------------------------------------------------------------
    //
    fun goDetail(view:View)
    {
        /*
        val pInfo:photo = view.getTag() as photo
        var pIntent = Intent(view.context, ActPlaceDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_PLACE_OBJECT, pInfo as Serializable)
        view.context.startActivity(pIntent)
        */
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
    /*********************** interface ***********************/
    //----------------------------------------------------------------------------
    //
    interface ifCallback
    {
        fun addPhoto()
    }
}