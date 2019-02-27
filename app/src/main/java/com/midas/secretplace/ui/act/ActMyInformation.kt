package com.midas.secretplace.ui.act

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.ShareCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.user
import com.midas.secretplace.structure.room.data_user
import com.midas.secretplace.structure.vm.vm_user
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.custom.dlg_share_view
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_myinformation.*

/*
key share..
 */
class ActMyInformation : AppCompatActivity()
{
    /*********************** extentios function ***********************/
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    //----------------------------------------------------------
    //pinchToZoom
    inline fun Activity.showShareViewDialog(func: dlg_share_view.() -> Unit): AlertDialog =
            dlg_share_view(this).apply {
                func()
            }.create()

    inline fun Fragment.showShareViewDialog(func: dlg_share_view.() -> Unit): AlertDialog =
            dlg_share_view(this.context!!).apply {
                func()
            }.create()

    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null

    var m_UserDbRef:DatabaseReference? = null//firebase database
    var m_UserViewModel: vm_user?= null//mvvm

    //
    private var m_Clipboard: ClipboardManager? = null
    private var m_Clip: ClipData? = null

    //
    /*********************** Controller ***********************/
    var m_ShareViewDialog:AlertDialog? = null
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActMyInformation)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_myinformation)

        //Clipboard
        m_Clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?

        //ViewModel
        m_UserViewModel = ViewModelProviders.of(this).get(vm_user::class.java)
        m_UserViewModel?.userInfo?.observe(this, object : Observer<data_user> {
            override fun onChanged(t: data_user?) {
                if(t != null)
                {
                    tv_UserKey.text = t!!.user_key
                }
            }
        })

        setInitLayout()
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        //firebase database reference..
        progressBar.visibility = View.VISIBLE
        m_UserDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!.child(m_App!!.m_SpCtrl!!.getSpUserKey())//where
        m_UserDbRef!!.addValueEventListener(userTableRefListener)
        super.onStart()
    }

    /*********************** Firebase DB EventListener ***********************/
    //--------------------------------------------------------------
    //
    var userTableRefListener:ValueEventListener = object : ValueEventListener
    {
        override fun onDataChange(dataSnapshot: DataSnapshot)
        {
            // Get Post object and use the values to update the UI
            if(dataSnapshot!!.exists())
            {
                var pInfo:user = dataSnapshot!!.getValue(user::class.java)!!
                tv_UserKey.text = pInfo!!.user_key!!
                var dataUser:data_user = data_user(0, pInfo!!.img_url!!, pInfo!!.name!!, pInfo!!.sns_key!!, pInfo!!.sns_type!!, pInfo!!.user_key!!)

                if(m_UserViewModel?.select() != null)
                    m_UserViewModel?.update(dataUser)//
                else
                    m_UserViewModel?.insert(dataUser)//
            }

            progressBar.visibility = View.GONE
        }

        override fun onCancelled(databaseError: DatabaseError)
        {
            // Getting Post failed, log a message

            // ...

            progressBar.visibility = View.GONE
        }
    }
    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun setInitLayout()
    {
        toolbar.title = m_Context!!.resources.getString(R.string.str_msg_30)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        var strTheme:String = m_App!!.m_SpCtrl!!.getSpTheme()!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setToolbarBackgroundColor(m_Context!!, this.toolbar, strTheme!!)
        }


        //share btn click event..
        ly_Right.setOnClickListener {
            val shareIntent = ShareCompat.IntentBuilder.from(this)
                    .setText(m_App!!.m_SpCtrl!!.getSpUserKey())
                    .setType("text/plain")
                    .createChooserIntent()
                    .apply {
                        // https://android-developers.googleblog.com/2012/02/share-with-intents.html
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            // If we're on Lollipop, we can open the intent as a document
                            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        } else {
                            // Else, we will use the old CLEAR_WHEN_TASK_RESET flag
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                        }
                    }
            startActivity(shareIntent)
        }
    }

    //--------------------------------------------------------------------
    //show share dialog
    fun onClickSahre(view:View)
    {
        showSahreViewDialog()
    }

    //----------------------------------------------------------
    //showing dialog
    fun showSahreViewDialog()
    {
        m_ShareViewDialog = showShareViewDialog {
            cancelable = true

            //iv_DlgPhoto!!.setImageDrawable(drawable!!)
            closeIconClickListener {
                m_ShareViewDialog!!.dismiss()
            }

            shareCopyClickListener {
                shareCopy()
            }

            shareKakaoClickListener {
                shareKakao()
            }

            shareSmsClickListener{
                shareSms()
            }

        }
        //  and showing
        m_ShareViewDialog?.show()
    }

    //--------------------------------------------------------------------
    //copy
    fun shareCopy()
    {
        m_Clip = ClipData.newPlainText("text", tv_UserKey.text)
        m_Clipboard?.primaryClip = m_Clip
        Toast.makeText(this, m_Context!!.resources!!.getString(R.string.str_msg_34), Toast.LENGTH_LONG).show()

        if(m_ShareViewDialog != null)
            m_ShareViewDialog!!.dismiss()
    }
    //--------------------------------------------------------------------
    //kakao
    fun shareKakao()
    {
        if(m_ShareViewDialog != null)
            m_ShareViewDialog!!.dismiss()
    }
    //--------------------------------------------------------------------
    //sms
    fun shareSms()
    {
        if(m_ShareViewDialog != null)
            m_ShareViewDialog!!.dismiss()
    }

}
