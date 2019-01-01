package com.midas.secretplace.ui.act

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
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
import kotlinx.android.synthetic.main.act_myinformation.*


class ActMyInformation : AppCompatActivity()
{
    /*********************** extentios function ***********************/
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null
    //realtime database
    var m_UserDbRef:DatabaseReference? = null

    var m_UserViewModel: vm_user?= null
    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_myinformation)
        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActMyInformation)


        m_UserViewModel = ViewModelProviders.of(this).get(vm_user::class.java)

        m_UserViewModel?.userInfo?.observe(this, object : Observer<data_user> {
            override fun onChanged(t: data_user?) {
                if(t != null)
                {
                    edit_Name.text = t!!.name.toEditable()
                    edit_UserKey.text = t!!.user_key.toEditable()
                }
            }
        })
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
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
                edit_Name.text = pInfo!!.name!!.toEditable()
                edit_UserKey.text = pInfo!!.user_key!!.toEditable()
                var dataUser:data_user = data_user(0, pInfo!!.img_url!!, pInfo!!.name!!, pInfo!!.sns_key!!, pInfo!!.sns_type!!, pInfo!!.user_key!!)

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



}
