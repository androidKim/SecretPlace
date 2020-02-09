package com.midas.secretplace.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import com.midas.secretplace.R
import com.midas.secretplace.structure.core.category

class DialogSpinnerAdapter(val context: Context, var list: ArrayList<category>): BaseAdapter() {
    val inflater:LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.spinner_item, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        vh.tvCategory.text = list.get(position).name
        return view
    }

    override fun getItem(position: Int): category? {

        return list.get(position)

    }

    override fun getItemId(position: Int): Long {

        return 0

    }

    override fun getCount(): Int {
        return list.size
    }

    private class ItemRowHolder(view: View?) {
        val baseLayout:RelativeLayout
        val tvCategory: TextView

        init {
            this.baseLayout = view?.findViewById(R.id.baseLayout) as RelativeLayout
            this.tvCategory = view?.findViewById(R.id.tvCategory) as TextView
        }
    }
}