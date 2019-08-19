package com.midas.secretplace.ui.custom

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.midas.secretplace.R

/*
user key share dialog
 */
class dlg_share_view(context: Context):BaseDialogHelper()
{
    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dlg_share_view, null)
    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)

    //..
    private val iBtn_Close: ImageButton by lazy {
        dialogView.findViewById<ImageButton>(R.id.iBtn_Close)
    }

    private val tv_ShareCopy: TextView by lazy {
        dialogView.findViewById<TextView>(R.id.tv_ShareCopy)
    }

    private val tv_ShareKakao: TextView by lazy {
        dialogView.findViewById<TextView>(R.id.tv_ShareKakao)
    }

    private val tv_ShareSms: TextView by lazy {
        dialogView.findViewById<TextView>(R.id.tv_ShareSms)
    }

    //...
    fun closeIconClickListener(func: (() -> Unit)? = null) =
            with(iBtn_Close) {
                setClickListenerClose(func)
            }

    fun shareCopyClickListener(func: (() -> Unit)? = null) =
            with(tv_ShareCopy) {
                setClickListenerShareCopy(func)
            }

    fun shareKakaoClickListener(func: (() -> Unit)? = null) =
            with(tv_ShareKakao) {
                setClickListenerShareKakao(func)
            }

    fun shareSmsClickListener(func: (() -> Unit)? = null) =
            with(tv_ShareSms) {
                setClickListenerShareSms(func)
            }

    //  view click listener as extension function....
    private fun View.setClickListenerClose(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
            }

    private fun View.setClickListenerShareCopy(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
            }

    private fun View.setClickListenerShareKakao(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
            }

    private fun View.setClickListenerShareSms(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
            }
}