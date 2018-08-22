package com.midas.secretplace.ui.adapter


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.midas.secretplace.R

class PhotoRvAdapter(val context: Context, var photoList: ArrayList<String>) :
        RecyclerView.Adapter<PhotoRvAdapter.Holder>()
{
    /*********************** System Function ***********************/
    //-----------------------------------------------------------
    //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder
    {
        val view = LayoutInflater.from(context).inflate(R.layout.row_photo, parent, false)
        val holder:Holder = Holder(view)
        return holder
    }

    //-----------------------------------------------------------
    //
    override fun getItemCount(): Int
    {
        return photoList.size
    }
    //-----------------------------------------------------------
    //
    override fun onBindViewHolder(holder: Holder, position: Int)
    {
        holder?.bind(photoList[position], context)
    }
    //-----------------------------------------------------------
    //
    inner class Holder(itemView:View?) : RecyclerView.ViewHolder(itemView)
    {
        var ly_Row = itemView?.findViewById<RelativeLayout>(R.id.ly_Row)
        var tv_Name = itemView?.findViewById<TextView>(R.id.tv_Name)
        var iv_Photo = itemView?.findViewById<ImageView>(R.id.iv_Photo)

        fun bind (url: String, pContext: Context)
        {
            Glide.with(context).load(url).into(iv_Photo)

            //ly_Row?.setTag(pInfo)
            //ly_Row?.setOnClickListener(onClickGoDetail)
        }
    }
    /*********************** User Function ***********************/
    //-----------------------------------------------------------
    //
    fun addData(url:String)
    {
        if(url == null)
            return

        this.photoList.add(url)
        notifyDataSetChanged()
    }
    //-----------------------------------------------------------
    //
    fun clearData()
    {
        this.photoList.clear()
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
}