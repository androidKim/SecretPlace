package com.midas.secretplace.ui.custom

import android.content.Context
import android.os.Build
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.midas.secretplace.R

class dlg_terms_agree(context: Context):BaseDialogHelper()
{
    var m_bCheck:Boolean = false

    //  dialog view
    override val dialogView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dlg_agree, null)

    }

    override val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(dialogView)


    //..
    private val iBtn_Close: ImageButton by lazy {
        dialogView.findViewById<ImageButton>(R.id.iBtn_Close)
    }


    private val ly_TermsAgree: RelativeLayout by lazy {
        dialogView.findViewById<RelativeLayout>(R.id.ly_TermsAgree)
    }

    private val iv_CheckBox: ImageView by lazy {
        dialogView.findViewById<ImageView>(R.id.iv_CheckBox)
    }


    private val tv_Detail: TextView by lazy {
        dialogView.findViewById<TextView>(R.id.tv_Detail)
    }


    private val btn_Comfirm: Button by lazy {
        dialogView.findViewById<Button>(R.id.btn_Confirm)
    }

    //...
    fun closeIconClickListener(func: (() -> Unit)? = null) =
            with(iBtn_Close) {
                setClickListenerClose(func)
            }


    fun agreeClickListener(func: (() -> Unit)? = null) =
            with(ly_TermsAgree) {
                setClickListenerAgree(func)
            }

    fun termsDetailClickListener(func: (() -> Unit)? = null) =
            with(tv_Detail) {
                setClickListenerTermsDetail(func)
            }

    fun confirmClickListener(func: (() -> Unit)? = null) =
            with(btn_Comfirm) {
                setClickListenerConfirm(func)
            }

    //  view click listener as extension function....
    private fun View.setClickListenerClose(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
                dialog?.dismiss()
            }

    private fun View.setClickListenerAgree(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
                //change image button background
                if(!m_bCheck)
                {
                    m_bCheck= true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        iv_CheckBox.background = resources.getDrawable(R.drawable.outline_check_box_black_24, null)
                    }
                }
                else
                {
                    m_bCheck = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        iv_CheckBox.background = resources.getDrawable(R.drawable.outline_check_box_outline_blank_black_24, null)
                    }
                }

            }

    private fun View.setClickListenerTermsDetail(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()

            }

    private fun View.setClickListenerConfirm(func: (() -> Unit)?) =
            setOnClickListener {
                func?.invoke()
                if(!m_bCheck)
                {
                    Toast.makeText(context, resources.getString(R.string.str_msg_23), Toast.LENGTH_SHORT).show()
                }
                else
                {
                    dialog?.dismiss()

                }
            }
}