package com.midas.secretplace.ui.act


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.database.*
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.user
import com.midas.secretplace.ui.MyApp
import kotlinx.android.synthetic.main.act_login.*






class ActLogin:AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener
{
    override fun onConnectionFailed(connectionResult: ConnectionResult)
    {

    }

    /******************* Define *******************/
    private val RC_SIGN_IN = 9001
    /******************* Member *******************/
    private var m_App:MyApp? = null
    private var m_Context:Context? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    /******************* Controller *******************/
    private var m_btn_GoogleLogin: Button? = null


    /******************* System Function *******************/
    //------------------------------------------------
    //
    @Override
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)

        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActLogin)

        initValue()
        recvIntentData()
        initLayout()
    }

    //------------------------------------------------
    //
    @Override
    override fun onBackPressed()
    {
        super.onBackPressed()
        finish()
        System.exit(0)
    }

    //-------------------------------------------------------------
    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN)
        {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }
    /******************* User Function *******************/
    //------------------------------------------------
    //
    private fun initValue()
    {

    }
    //------------------------------------------------
    //
    private fun recvIntentData()
    {

    }
    //------------------------------------------------
    //
    private fun initLayout()
    {
        m_btn_GoogleLogin = findViewById(R.id.btn_GoogleLogin)


        setGoogleLoginInit()
    }

    //------------------------------------------------
    //
    private fun setGoogleLoginInit()
    {
        setRefreshUi(false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        m_btn_GoogleLogin?.setOnClickListener(View.OnClickListener
        {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        })

        /*
        m_btn_GoogleLogout?.setOnClickListener(View.OnClickListener
        {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                object : ResultCallback<Status>
                {
                    override fun onResult(status: Status)
                    {
                        setRefreshUi(false)
                    }
                })
        })
        */
    }

    //-----------------------------------------------------------
    //
    private fun setRefreshUi(isLogin: Boolean)
    {
        if (isLogin)
        {

        }
        else
        {

        }
    }
    //-----------------------------------------------------------
    //
    private fun handleSignInResult(result: GoogleSignInResult)
    {
        if (result.isSuccess)
        {
            progressBar.visibility = View.VISIBLE
            setRefreshUi(true)

            val acct = result.signInAccount
            var snsKey:String? = acct!!.id
            var strUserName:String? = acct!!.displayName
            var strImgUrl: String? = acct!!.photoUrl.toString()
            if(strImgUrl == null)
                strImgUrl = ""

            var pInfo: user = user(user.SNS_TYPE_GOOGLE, snsKey!!, "", strUserName!!, strImgUrl)

            var pQuery:Query= m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER).orderByChild("sns_key").equalTo(snsKey)
            pQuery.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
                {
                    if (dataSnapshot!!.exists())//exist..
                    {
                        val pRes:user = dataSnapshot!!.getValue(user::class.java)!!
                        pRes.user_key = dataSnapshot.key
                        m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!.child(pRes.user_key).setValue(pRes)//update..
                        m_App!!.m_SpCtrl!!.setSpUserKey(pRes.user_key!!)
                        m_App!!.m_SpCtrl!!.setSnsType(user.SNS_TYPE_GOOGLE)
                        m_App!!.goMain(m_Context!!)
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
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot?, previousChildName: String?)
                {
                    Log.d("onChildMoved", "")
                }

                override fun onCancelled(databaseError: DatabaseError?)
                {
                    Log.d("onCancelled", "")
                }
            })


            pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?)
                {
                    if(dataSnapshot!!.exists())
                    {

                    }
                    else
                    {
                        //first Insert..
                        var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!.push()//insert..
                        pDbRef!!.setValue(pInfo!!)//insert
                    }
                }

                override fun onCancelled(p0: DatabaseError?)
                {
                    Log.d("onCancelled", "")
                }
            })
        }
    }
}