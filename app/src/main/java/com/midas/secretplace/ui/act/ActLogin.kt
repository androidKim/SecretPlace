package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.midas.secretplace.R
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

            var userKey:String? = String.format("%s%s", user.SNS_TYPE_GOOGLE, snsKey)
            var pInfo: user = user(user.SNS_TYPE_GOOGLE, snsKey!!, userKey!!, strUserName!!, strImgUrl)
            m_App!!.m_FirebaseDbCtrl!!.setUser(pInfo)

            m_App!!.m_SpCtrl!!.setSpUserKey(userKey!!)
            m_App!!.m_SpCtrl!!.setSnsType(user.SNS_TYPE_GOOGLE)
            m_App!!.goMain(m_Context!!)
        }
    }
}