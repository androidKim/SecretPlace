package com.midas.secretplace.ui.act

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_myinformation.*


/*
내 정보
 */
class ActMyInformation : AppCompatActivity()
{
    /*********************** extentios function ***********************/
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

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
                    tv_Name.text = t!!.name
                    tv_UserKey.text = t!!.user_key
                }
            }
        })


    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        //firebase database reference..
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
                tv_Name.text = pInfo!!.name!!
                tv_UserKey.text = pInfo!!.user_key!!
                var dataUser:data_user = data_user(0, pInfo!!.img_url!!, pInfo!!.name!!, pInfo!!.sns_key!!, pInfo!!.sns_type!!, pInfo!!.user_key!!)

                if(m_UserViewModel?.select() != null)
                    m_UserViewModel?.update(dataUser)//
                else
                    m_UserViewModel?.insert(dataUser)//
            }
        }

        override fun onCancelled(databaseError: DatabaseError)
        {
            // Getting Post failed, log a message

            // ...
        }
    }
    /*********************** User Function ***********************/

    //--------------------------------------------------------------------
    // on click copy button
    fun copyKey(view: View)
    {
        m_Clip = ClipData.newPlainText("text", tv_UserKey.text)
        m_Clipboard?.primaryClip = m_Clip
        Toast.makeText(this, m_Context!!.resources!!.getString(R.string.str_msg_34), Toast.LENGTH_SHORT).show();
    }
    //--------------------------------------------------------------
    //나에게 온 요청리스트
    fun showRequestForMe(view:View)
    {
        //
        Intent(m_Context, ActRequestForMe::class.java).let{
            startActivityForResult(it, 0)
        }
    }
}
