package com.midas.secretplace.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.midas.secretplace.R
import com.midas.secretplace.structure.core.place

class PhotoRvAdapter(val context: Context, var m_RequestManager:RequestManager, var m_PlaceInfo: place, var photoList: ArrayList<String>, var m_IfCallback:ifCallback, var m_FrManager: FragmentManager) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    {
        /*
        if (viewType == TYPE_HEADER)
        {
            val view = LayoutInflater.from(context).inflate(R.layout.ly_place_detail_header, parent, false)
            val holder:HeaderHolder = HeaderHolder(view)
            return holder
        }
        else
        */
        if (viewType == TYPE_ITEM)
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
        /*
        if (holder is HeaderHolder)
        {
            holder?.bind(photoList[position], context)
        }
        else if (holder is Holder)
        {
            holder?.bind(photoList[position], context)
        }
        */
        if (holder is Holder)
            holder?.bind(photoList[position], context)
    }

    //-----------------------------------------------------------
    //
    override fun getItemViewType(position: Int): Int
    {
        /*
        return if(photoList[position].equals("header"))
        {
            TYPE_HEADER
        }
        else
        {
            TYPE_ITEM
        }
        */
        return TYPE_ITEM
    }
    //-----------------------------------------------------------
    //Header..
    /*
    inner class HeaderHolder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var tv_Name = itemView?.findViewById<TextView>(R.id.tv_Name)
        var ly_AddPhoto = itemView?.findViewById<LinearLayout>(R.id.ly_AddPhoto)
        var ly_EditContent = itemView?.findViewById<LinearLayout>(R.id.ly_EditContent)
        var iv_Photo = itemView?.findViewById<ImageView>(R.id.iv_Photo)

        fun bind (pInfo: String, pContext: Context)
        {
            tv_Name!!.text = m_PlaceInfo.name

            //event..
            ly_AddPhoto!!.setOnClickListener(View.OnClickListener {
                if(m_IfCallback != null)
                    m_IfCallback.addPhoto()
            })

            ly_EditContent!!.setOnClickListener({
                if(m_IfCallback != null)
                    m_IfCallback.editContent()
            })
        }
    }
    */

    //-----------------------------------------------------------
    //
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var ly_Row = itemView?.findViewById<RelativeLayout>(R.id.ly_Row)
        var iv_Photo = itemView?.findViewById<ImageView>(R.id.iv_Photo)
        var iv_None = itemView?.findViewById<ImageView>(R.id.iv_None)
        var iv_Fail= itemView?.findViewById<ImageView>(R.id.iv_Fail)

        fun bind (url: String, pContext: Context)
        {
            m_RequestManager
            .load(url)
            .listener(object : RequestListener<Drawable>
            {
                override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean
                {
                    iv_None!!.visibility = View.GONE
                    iv_Fail!!.visibility = View.VISIBLE
                    return false
                }
                override fun onResourceReady(p0: Drawable?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean
                {
                    //do something when picture already loaded
                    iv_Fail!!.visibility = View.GONE
                    iv_None!!.visibility = View.GONE
                    return false
                }
            })
            .into(iv_Photo)

            if(url != null)
                itemView!!.tag = url

            itemView!!.setOnClickListener({
                if(m_IfCallback != null)
                {
                    var url:String = itemView!!.tag as String
                    m_IfCallback.showPhotoDialog(url)
                }

            })
        }


    }

    /*********************** User Function ***********************/
    //-----------------------------------------------------------
    //
    companion object
    {
        //private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 0
    }
    //-----------------------------------------------------------
    //
    fun setPlaceInfo(pInfo:place)
    {
        if(pInfo == null)
            return

        m_PlaceInfo = pInfo
    }
    //-----------------------------------------------------------
    //
    fun addItem(pInfo:String)
    {
        if(pInfo == null)
            return

        this.photoList.add(pInfo)
        notifyDataSetChanged()
    }

    //-----------------------------------------------------------
    //
    fun addList(pArray:ArrayList<String>)
    {
        if(pArray == null)
            return

        this.photoList.addAll(pArray)
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
    fun refreshData(pArray:ArrayList<String>)
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
        fun showPhotoDialog(url:String)
    }
}