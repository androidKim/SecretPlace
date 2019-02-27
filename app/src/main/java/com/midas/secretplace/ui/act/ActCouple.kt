package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import com.google.firebase.database.*
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
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
    var m_bExistCouple:Boolean = false//
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
        initLayout()
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        m_pCoupleDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!
        m_pCoupleDbRef!!.addChildEventListener(coupleTableChildEventListener)
        super.onStart()
    }

    //--------------------------------------------------------------
    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Constant.FOR_RESULT_REQUEST_FOR_ME)
        {
            m_bExistCouple = false
            //refresh..
            m_pCoupleDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!
            m_pCoupleDbRef!!.addChildEventListener(coupleTableChildEventListener)
        }
    }

    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //
    fun initLayout()
    {
        toolbar.title = m_Context!!.resources.getString(R.string.str_msg_29)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        var strTheme:String = m_App!!.m_SpCtrl!!.getSpTheme()!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setToolbarBackgroundColor(m_Context!!, this.toolbar, strTheme!!)
        }

        //refresh..
        m_bExistCouple = false
        m_pCoupleDbRef = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!
        m_pCoupleDbRef!!.addChildEventListener(coupleTableChildEventListener)
    }
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

        var pInfo:couple = couple(m_App!!.m_SpCtrl!!.getSpUserKey()!! , edit_UserKey.text.toString(), couple.APPCET_N)
        m_pCoupleDbRef!!.push().setValue(pInfo!!)
        m_pCoupleDbRef!!.addChildEventListener(coupleTableChildEventListener)

    }
    //--------------------------------------------------------------
    //요청취소
    fun cancelProc(view: View)
    {
        ly_RequestStatusOk.visibility = View.GONE
        ly_RequestStatusNot.visibility = View.VISIBLE

        var pCoupleDbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!
        pCoupleDbRef!!.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot?) {
                val children = p0!!.children
                children.forEach {
                    val pInfo: couple = it!!.getValue(couple::class.java)!!

                    if(pInfo.requester_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))//내가 요청인 데이터  삭제
                    {
                        var dbRef:DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_COUPLE)!!.child(it.key)
                        dbRef!!.removeValue()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }
    //--------------------------------------------------------------
    //
    fun goChatActivity(view:View)
    {
        //
        Intent(m_Context, ActChat::class.java).let{
            startActivity(it)
        }
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

    /*********************** Firebase DB EventListener ***********************/
    //--------------------------------------------------------------
    //childEventListener..
    var coupleTableChildEventListener:ChildEventListener = object : ChildEventListener
    {
        override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
        {
            // Get Post object and use the values to update the UI
            if(dataSnapshot!!.exists())
            {
                if(m_bExistCouple)
                    return

                m_pCoupleDbRef!!.removeEventListener(this)//중복 진입 방지..
                val pInfo:couple = dataSnapshot!!.getValue(couple::class.java)!!
                if(pInfo.requester_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))// 요청자가 나일떄
                {
                    ly_RequestStatusOk.visibility = View.VISIBLE
                    ly_RequestStatusNot.visibility = View.GONE

                    if(pInfo.accept.equals(couple.APPCET_Y))
                    {
                        m_bExistCouple = true
                        tv_CurrentRequestUser.text = pInfo.responser_key + "와 커플입니다."
                    }
                    else
                    {
                        tv_CurrentRequestUser.text = "내가" + pInfo.responser_key + "님 에게 커플 요청중입니다."
                        ly_Request.visibility = View.VISIBLE
                    }
                }
                else if(pInfo.responser_key.equals(m_App!!.m_SpCtrl!!.getSpUserKey()))//응답자가 나일떄
                {
                    if(pInfo.accept.equals(couple.APPCET_Y))
                    {
                        m_bExistCouple = true
                        ly_RequestStatusOk.visibility = View.VISIBLE
                        ly_RequestStatusNot.visibility = View.GONE
                        tv_CurrentRequestUser.text = pInfo.requester_key + "와 커플입니다."
                    }
                    else
                    {
                        ly_RequestStatusOk.visibility = View.GONE
                        ly_RequestStatusNot.visibility = View.VISIBLE
                    }
                }
                else
                {
                    ly_RequestStatusOk.visibility = View.GONE
                    ly_RequestStatusNot.visibility = View.VISIBLE
                }
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
