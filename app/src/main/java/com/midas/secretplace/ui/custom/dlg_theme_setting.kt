package com.midas.secretplace.ui.custom

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import com.midas.secretplace.R

/*
photo pinch to zoom view
 */
class dlg_theme_setting(context: Context):BaseDialogHelper()
{


    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dlg_theme_setting, null)


    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)

    //..
    private val iBtn_Close: ImageButton by lazy {
        dialogView.findViewById<ImageButton>(R.id.iBtn_Close)
    }

    //..
    private val themeRecyclerView: RecyclerView by lazy {
        dialogView.findViewById<RecyclerView>(R.id.themeRecyclerView)
    }


    //----------------------------------------------------------
    //close..
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