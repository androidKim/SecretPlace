package com.midas.secretplace.ui.custom

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View


class SimpleDividerItemDecoration(private val space: Int) : RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State)
    {
        outRect.left = space
        outRect.right = space
        outRect.bottom = space

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = space
        } else {
            outRect.top = 0
        }
    }
}