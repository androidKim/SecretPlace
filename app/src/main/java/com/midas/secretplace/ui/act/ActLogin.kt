package com.midas.secretplace.ui.act


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.user
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.custom.dlg_terms_agree
import com.twitter.sdk.android.core.*
import kotlinx.android.synthetic.main.act_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class ActLogin:AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{
    //extention functions..
    inline fun Activity.showwTermsAgreeDialog(func: dlg_terms_agree.() -> Unit): AlertDialog =
            dlg_terms_agree(this).apply {
                func()
            }.create()

    inline fun Fragment.showwTermsAgreeDialog(func: dlg_terms_agree.() -> Unit): AlertDialog =
            dlg_terms_agree(this.context!!).apply {
                func()
            }.create()

    //callback function..
    override fun onConnectionFailed(connectionResult: ConnectionResult)
    {

    }

    /******************* Define *******************/
    private val TAG:String = "ActLogin"
    private val TWITTER_LOG_IN_RC = 9001
    private val FACEBOOK_LOG_IN_RC = 9002
    private val GOOGLE_LOG_IN_RC = 9003
    /******************* Member *******************/
    private var m_App:MyApp? = null
    private var m_Context:Context? = null
    private var googleApiClient: GoogleApiClient? = null
    private var mAuth:FirebaseAuth ?= null
    /******************* Controller *******************/
    private var m_btn_GoogleLogin: SignInButton? = null
    private var callbackManager:CallbackManager? = null//facebookcallback
    private var m_TermsAgreeDialog: AlertDialog? = null


    /******************* System Function *******************/
    //------------------------------------------------
    //
    @Override
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        //twitter config & init..
        var twitterConfig = TwitterConfig.Builder(this)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(TwitterAuthConfig(resources.getString(R.string.twitter_consumer_key), resources.getString(R.string.twitter_consumer_secret)))
                .debug(false)
                .build()
        Twitter.initialize(twitterConfig)

        m_Context = this
        m_App = MyApp()
        m_App!!.init(m_Context as ActLogin)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.act_login)

        mAuth = FirebaseAuth.getInstance()

        initValue()
        recvIntentData()
        initLayout()
    }
    //------------------------------------------------
    //
    @Override
    override fun onStart()
    {
        super.onStart()
         // Check if vm_user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
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

        twitter_sign_in_button!!.onActivityResult(requestCode, resultCode, data)//twitter

        callbackManager!!.onActivityResult(requestCode, resultCode, data)//facebobok

        if (requestCode == GOOGLE_LOG_IN_RC)
        {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess)
            {
                // Google Sign In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(result.signInAccount!!)

                if (googleApiClient!!.hasConnectedApi(Auth.GOOGLE_SIGN_IN_API))
                    googleApiClient!!.clearDefaultAccountAndReconnect()
            }
            else
            {
                Toast.makeText(this@ActLogin, "Some error occurred.", Toast.LENGTH_SHORT).show()
            }
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
        LoginManager.getInstance().logOut()

        setTwitterSign()
        setFacebookSign()
        setGoogleSign()
        getHashKey()
    }

    //-----------------------------------------------
    //
    fun getHashKey()
    {
        try {
            val info = packageManager.getPackageInfo(
                    "com.midas.secretplace",
                    PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }
    }

    //----------------------------------------------------------
    //  showing dialog
    fun showTermsAgreeDialog(userInfo:user, twSession: TwitterSession?, fbToken: AccessToken?, acct:GoogleSignInAccount?)
    {
        progressBar.visibility = View.GONE

        ////////////////////////////////////////////////////////////////
        //  making Alert dialog - admire beauty of kotlin
        ////////////////////////////////////////////////////////////////
        m_TermsAgreeDialog = showwTermsAgreeDialog {

            cancelable = false
            closeIconClickListener {
                m_bCheck = false
                LoginManager.getInstance().logOut()
            }

            agreeClickListener{

            }

            termsDetailClickListener {
                val url = "http://54.180.109.122:8081/terms"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            confirmClickListener {
                if(m_bCheck)
                {
                    //join..
                    if(userInfo.sns_type.equals(user.SNS_TYPE_TWITTER))
                    {
                        var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!
                        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                        var strUserName:String? = mAuth!!.currentUser!!.displayName
                        var strImgUrl: String? = ""
                        userInfo.user_key = currentFirebaseUser!!.uid
                        userInfo.name = strUserName
                        userInfo.img_url = strImgUrl
                        pDbRef!!.child(userInfo.user_key).setValue(userInfo!!)//insert
                    }
                    else if(userInfo.sns_type.equals(user.SNS_TYPE_FACEBOOK))
                    {
                        var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!
                        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                        var strUserName:String? = mAuth!!.currentUser!!.displayName
                        var strImgUrl: String? = "https://graph.facebook.com/" + fbToken!!.userId + "/picture?type=large"
                        userInfo.user_key = currentFirebaseUser!!.uid
                        userInfo.name = strUserName
                        userInfo.img_url = strImgUrl
                        pDbRef!!.child(userInfo.user_key).setValue(userInfo!!)//insert
                    }
                    else if(userInfo.sns_type.equals(user.SNS_TYPE_GOOGLE))
                    {
                        var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!
                        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                        var strUserName:String? = acct!!.displayName
                        var strImgUrl: String? = acct!!.photoUrl.toString()
                        userInfo.user_key = currentFirebaseUser!!.uid
                        userInfo.name = strUserName
                        userInfo.img_url = strImgUrl
                        pDbRef!!.child(userInfo.user_key).setValue(userInfo!!)//insert
                    }
                }
                else
                {

                }
            }
        }

        //  and showing
        m_TermsAgreeDialog?.show()

    }
    /******************* twitter login *******************/
    //------------------------------------------------------------------
    //
    private fun setTwitterSign()
    {
        twitter_sign_in_button.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                Log.d(TAG, "twitterLogin:success" + result)
                handleTwitterSession(result.data)
            }
            override fun failure(exception: TwitterException) {
                Log.w(TAG, "twitterLogin:failure", exception)
            }
        }
    }
    //------------------------------------------------------------------
    //
    private fun handleTwitterSession(session: TwitterSession) {
        Log.d(TAG, "handleTwitterSession:" + session)

        var credential:AuthCredential  = TwitterAuthProvider.getCredential(
                session.authToken.token,
                session.authToken.secret)

        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in vm_user's information
                        Log.d(TAG, "signInWithCredential:success")
                        //val vm_user = mAuth!!.currentUser
                        //startActivity(Intent(this@ActLogin, ActMain::class.java))
                        //handleSignInResult(task)
                        progressBar.visibility = View.VISIBLE

                        var snsKey:String? = String.format("%s",session!!.userId)
                        var strUserName:String? = mAuth!!.currentUser!!.displayName
                        var strImgUrl: String = ""

                        var pInfo: user = user(user.SNS_TYPE_TWITTER, snsKey!!, "", strUserName!!, strImgUrl)

                        var pQuery:Query= m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER).orderByChild("sns_key").equalTo(snsKey)
                        pQuery.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
                            {
                                if (dataSnapshot!!.exists())//exist..
                                {
                                    val pRes:user = dataSnapshot!!.getValue(user::class.java)!!
                                    val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                                    var strUserName:String? = mAuth!!.currentUser!!.displayName
                                    var strImgUrl: String=""
                                    pRes.user_key = currentFirebaseUser!!.uid
                                    pRes.name = strUserName
                                    pRes.img_url = strImgUrl
                                    m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!.child(pRes.user_key).setValue(pRes)//update..
                                    m_App!!.m_SpCtrl!!.setSpUserKey(pRes.user_key!!)
                                    m_App!!.m_SpCtrl!!.setSnsType(user.SNS_TYPE_TWITTER)
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
                                    showTermsAgreeDialog(pInfo!!, session!!, null, null)
                                }
                            }

                            override fun onCancelled(p0: DatabaseError?)
                            {
                                Log.d("onCancelled", "")
                            }
                        })
                    } else {
                        // If sign in fails, display a message to the vm_user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException())
                        Toast.makeText(this@ActLogin, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    /******************* facebook login *******************/
    //------------------------------------------------------------------
    //
    private fun setFacebookSign()
    {
        callbackManager = CallbackManager.Factory.create()
        facebook_sign_in_button.setReadPermissions("email", "public_profile")
        // If using in a fragment
        facebook_sign_in_button.fragment

        // Callback registration
        facebook_sign_in_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult>
        {
            override fun onSuccess(loginResult: LoginResult)
            {
                // App code
                handleFacebookAccessToken(loginResult.getAccessToken())
            }

            override fun onCancel()
            {
                // App code
            }

            override fun onError(exception: FacebookException)
            {
                // App code
            }
        })
    }
    //----------------------------------------------------------------
    //
    private fun handleFacebookAccessToken(token: AccessToken)
    {
        Log.d(TAG, "handleFacebookAccessToken:" + token)
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful)
                    {
                        // Sign in success, update UI with the signed-in vm_user's information
                        Log.d(TAG, "signInWithCredential:success")
                        //val vm_user = mAuth!!.currentUser
                        //startActivity(Intent(this@ActLogin, ActMain::class.java))
                        //handleSignInResult(task)
                        progressBar.visibility = View.VISIBLE

                        var snsKey:String? = token!!.userId
                        var strUserName:String? = mAuth!!.currentUser!!.displayName
                        var strImgUrl: String? = "https://graph.facebook.com/" + token!!.userId + "/picture?type=large"
                        if(strImgUrl == null)
                            strImgUrl = ""

                        var pInfo: user = user(user.SNS_TYPE_FACEBOOK, snsKey!!, "", strUserName!!, strImgUrl)

                        var pQuery:Query= m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER).orderByChild("sns_key").equalTo(snsKey)
                        pQuery.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(dataSnapshot: DataSnapshot?, previousChildName: String?)
                            {
                                if (dataSnapshot!!.exists())//exist..
                                {
                                    val pRes:user = dataSnapshot!!.getValue(user::class.java)!!
                                    val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                                    var strUserName:String? = mAuth!!.currentUser!!.displayName
                                    var strImgUrl: String? = "https://graph.facebook.com/" + token!!.userId + "/picture?type=large"
                                    pRes.user_key = currentFirebaseUser!!.uid
                                    pRes.name = strUserName
                                    pRes.img_url = strImgUrl
                                    m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!.child(pRes.user_key).setValue(pRes)//update..
                                    m_App!!.m_SpCtrl!!.setSpUserKey(pRes.user_key!!)
                                    m_App!!.m_SpCtrl!!.setSnsType(user.SNS_TYPE_FACEBOOK)
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
                                    showTermsAgreeDialog(pInfo!!, null, token!!, null)
                                }
                            }

                            override fun onCancelled(p0: DatabaseError?)
                            {
                                Log.d("onCancelled", "")
                            }
                        })
                    }
                    else
                    {
                        // If sign in fails, display a message to the vm_user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException())
                        Toast.makeText(this@ActLogin, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
    }
    /******************* google login *******************/
    //------------------------------------------------
    //
    private fun setGoogleSign()
    {
        m_btn_GoogleLogin = findViewById<View>(R.id.google_sign_in_button) as SignInButton
        m_btn_GoogleLogin?.setOnClickListener(this@ActLogin)
        // Configure Google Sign In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        // Creating and Configuring Google Api Client.
        googleApiClient = GoogleApiClient.Builder(this@ActLogin)
                .enableAutoManage(this@ActLogin  /* OnConnectionFailedListener */) { }
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build()
    }

    //-------------------------------------------------------------
    //
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount)
    {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful)
            {
                // Sign in success, update UI with the signed-in vm_user's information

                //handleSignInResult(task)
                progressBar.visibility = View.VISIBLE

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
                            val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                            pRes.user_key = currentFirebaseUser!!.uid
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
                            showTermsAgreeDialog(pInfo!!, null, null, acct!!)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?)
                    {
                        Log.d("onCancelled", "")
                    }
                })
            }
            else
            {
                // If sign in fails, display a message to the vm_user.
            }
        }
    }
    //-----------------------------------------------------------------------------
    //
    private fun googleLogin()
    {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, GOOGLE_LOG_IN_RC)
    }

    /************************* listener *************************/
    //-----------------------------------------------------------------------------
    //
    override fun onClick(view: View?)
    {
        when (view?.id) {
            R.id.google_sign_in_button ->
            {
                googleLogin()
            }
        }
    }
}