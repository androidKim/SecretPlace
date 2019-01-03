package com.midas.secretplace.ui.act

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.couple
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_couple.*

class ActCouple : AppCompatActivity()
{
    /*********************** Define ***********************/

    /*********************** Member ***********************/
    var m_App:MyApp? = null
    var m_Context: Context? = null

    var m_pCoupleDbRef: DatabaseReference? = null
    /*********************** xwSystem Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActCouple)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_couple)

    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        super.onStart()

        m_pCoupleDbRef =  m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)
        m_pCoupleDbRef!!.addListenerForSingleValueEvent(coupleTableValueEventListener)
        m_pCoupleDbRef!!.orderByChild("requester_key").equalTo(m_App!!.m_SpCtrl!!.getSpUserKey())//내가 요청한것
    }

    /*********************** User Function ***********************/

    /*********************** Listener ***********************/
    //--------------------------------------------------------------
    //커플 요청
    fun coupleRequestProc(view:View)
    {
        if(edit_UserKey.text.toString().equals(m_App!!.m_SpCtrl!!.getSpUserKey()))
        {
            Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.str_msg_40), Toast.LENGTH_SHORT).show()
            return
        }
        else if(edit_UserKey.text.toString().equals(""))
        {
            Toast.makeText(m_Context!!, m_Context!!.resources.getString(R.string.str_msg_41), Toast.LENGTH_SHORT).show()
            return
        }

        m_pCoupleDbRef!!.addChildEventListener(coupleTableChildEventListener)
        var pInfo:couple = couple(m_App!!.m_SpCtrl!!.getSpUserKey()!! , edit_UserKey.text.toString(), false)
        m_pCoupleDbRef!!.child(m_App!!.m_SpCtrl!!.getSpUserKey()!!)!!.setValue(pInfo!!)
    }
    //--------------------------------------------------------------
    //요청취소
    fun cancelProc(view: View)
    {
        m_pCoupleDbRef!!.addChildEventListener(coupleTableChildEventListener)
        var pDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!.child(m_App!!.m_SpCtrl!!.getSpUserKey())
        pDbRef!!.removeValue()
    }

    /*********************** Firebase DB EventListener ***********************/
    //--------------------------------------------------------------
    //valueEventListenre..
    var coupleTableValueEventListener:ValueEventListener = object : ValueEventListener
    {
        override fun onDataChange(dataSnapshot: DataSnapshot)
        {
            // Get Post object and use the values to update the UI
            if(dataSnapshot!!.exists())
            {
                //
                val children = dataSnapshot!!.children
                children.forEach {
                    val pInfo:couple = it!!.getValue(couple::class.java)!!
                    if(pInfo.requester_key!!.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))
                    {
                        if(pInfo.accept!!)
                        {
                            tv_CurrentRequestUser.text = pInfo.responser_key + "과 커플입니다."
                        }
                        else
                        {
                            tv_CurrentRequestUser.text = pInfo.responser_key + "님 에게 커플 요청중입니다."
                        }

                        ly_RequestStatusOk.visibility = View.VISIBLE
                        ly_RequestStatusNot.visibility = View.GONE
                    }
                    else
                    {
                        ly_RequestStatusOk.visibility = View.GONE
                        ly_RequestStatusNot.visibility = View.VISIBLE
                    }
                }
            }
            else
            {
                ly_RequestStatusOk.visibility = View.GONE
                ly_RequestStatusNot.visibility = View.VISIBLE
            }
        }

        override fun onCancelled(databaseError: DatabaseError)
        {
            // Getting Post failed, log a message

            // ...
            if(databaseError != null)
            {

            }
        }
    }

    //--------------------------------------------------------------
    //childEventListener..
    var coupleTableChildEventListener:ChildEventListener = object : ChildEventListener
    {
        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            if (dataSnapshot!!.exists())//exist..
            {
                val pInfo:couple = dataSnapshot!!.getValue(couple::class.java)!!
                if(pInfo.requester_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))
                {
                    if(pInfo.accept!!)
                    {
                        tv_CurrentRequestUser.text = pInfo.responser_key + "과 커플입니다."
                    }
                    else
                    {
                        tv_CurrentRequestUser.text = pInfo.responser_key + "님 에게 커플 요청중입니다."
                    }

                    ly_RequestStatusOk.visibility = View.VISIBLE
                    ly_RequestStatusNot.visibility = View.GONE
                }
                else
                {

                }
            }
            else
            {

            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            Log.d("onChildChanged", "")
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot?)
        {
            Log.d("onChildRemoved", "")
            //UI Update
            ly_RequestStatusOk.visibility = View.GONE
            ly_RequestStatusNot.visibility = View.VISIBLE
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            Log.d("onChildMoved", "")
        }

        override fun onCancelled(databaseError: DatabaseError?)
        {
            Log.d("onCancelled", "")
        }
    }
}
