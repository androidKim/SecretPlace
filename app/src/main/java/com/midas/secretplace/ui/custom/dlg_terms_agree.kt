package com.midas.secretplace.ui.custom

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.midas.secretplace.R

class dlg_terms_agree(context: Context):BaseDialogHelper()
{
    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dlg_agree, null)
    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)


    private val ly_TermsAgree: RelativeLayout by lazy {
        dialogView.findViewById<RelativeLayout>(R.id.ly_TermsAgree)
    }

    fun closeIconClickListener(func: (() -> Unit)? = null) =
            with(ly_TermsAgree) {
                setClickListenerToDialogAgreeLayout(func)
            }

    //  view click listener as extension function
    private fun View.setClickListenerToDialogAgreeLayout(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
                dialog?.dismiss()
            }
}