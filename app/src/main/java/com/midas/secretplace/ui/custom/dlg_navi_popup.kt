package com.midas.secretplace.ui.custom

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.midas.secretplace.R

class dlg_navi_popup(context: Context):BaseDialogHelper()
{
    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dlg_navi_popup, null)

    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)


    //..
    private val iBtn_Close: ImageButton by lazy {
        dialogView.findViewById<ImageButton>(R.id.iBtn_Close)
    }
    private val tvKakaoNavi: TextView by lazy {
        dialogView.findViewById<TextView>(R.id.tvKakaoNavi)
    }

    //...
    fun closeIconClickListener(func: (() -> Unit)? = null) =
            with(iBtn_Close) {
                setClickListenerClose(func)
            }


    fun kakaoNaviClickListener(func: (() -> Unit)? = null) =
            with(tvKakaoNavi) {
                setClickListenerKakaoNavi(func)
            }

    //  view click listener as extension function....
    private fun View.setClickListenerClose(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
                dialog?.dismiss()
            }

    private fun View.setClickListenerKakaoNavi(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
            }
}