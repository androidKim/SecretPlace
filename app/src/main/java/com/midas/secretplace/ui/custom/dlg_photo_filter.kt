package com.midas.secretplace.ui.custom

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import com.github.chrisbanes.photoview.PhotoView
import com.midas.secretplace.R

/*
photo pinch to zoom view
 */
class dlg_photo_filter(context: Context):BaseDialogHelper()
{
    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dlg_photo_filter, null)
    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)

    //..
    private val iBtn_Close: ImageButton by lazy {
        dialogView.findViewById<ImageButton>(R.id.iBtn_Close)
    }

    private val iBtn_Rotate: ImageButton by lazy {
        dialogView.findViewById<ImageButton>(R.id.iBtn_Rotate)
    }

    private val iBtn_Send: ImageButton by lazy {
        dialogView.findViewById<ImageButton>(R.id.iBtn_Send)
    }

    private val iv_Photo: PhotoView by lazy {
        dialogView.findViewById<PhotoView>(R.id.iv_Photo)
    }


    //...
    fun closeIconClickListener(func: (() -> Unit)? = null) =
            with(iBtn_Close) {
                setClickListenerClose(func)
            }

    //  view click listener as extension function....
    private fun View.setClickListenerClose(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
            }
}