package com.midas.secretplace.ui.act


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.midas.secretplace.R
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.user
import com.midas.secretplace.ui.MyApp
import kotlinx.android.synthetic.main.act_login.*


class ActLogin:AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{
    override fun onConnectionFailed(connectionResult: ConnectionResult)
    {

    }

    /******************* Define *******************/
    private val TAG:String = "ActLogin"
    private val GOOGLE_LOG_IN_RC = 9001
    /******************* Member *******************/
    private var m_App:MyApp? = null
    private var m_Context:Context? = null
    private var googleApiClient: GoogleApiClient? = null
    private var mAuth:FirebaseAuth ?= null
    /******************* Controller *******************/
    private var m_btn_GoogleLogin: SignInButton? = null
    private var callbackManager:CallbackManager? = null//facebookcallback

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
         // Check if user is signed in (non-null) and update UI accordingly.
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
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

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
        setFacebookSign()
        setGoogleSign()
    }

    /******************* facebook login *******************/
    //------------------------------------------------
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
                handleFacebookAccessToken(loginResult.getAccessToken());
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

        LoginManager.getInstance().logOut()
    }
    //------------------------------------------------
    //
    private fun handleFacebookAccessToken(token: AccessToken)
    {
        Log.d(TAG, "handleFacebookAccessToken:" + token)
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful)
                    {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        //val user = mAuth!!.currentUser
                        //startActivity(Intent(this@ActLogin, ActMain::class.java))


                        //handleSignInResult(task)
                        progressBar.visibility = View.VISIBLE

                        var snsKey:String? = token!!.userId
                        var strUserName:String? = mAuth!!.currentUser!!.displayName
                        var strImgUrl: String? = null
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
                                    //first Insert..
                                    var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!.push()//insert..
                                    val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                                    pInfo.user_key = currentFirebaseUser!!.uid
                                    pDbRef!!.setValue(pInfo!!)//insert
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
                        // If sign in fails, display a message to the user.
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
                // Sign in success, update UI with the signed-in user's information

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
                            //first Insert..
                            var pDbRef: DatabaseReference = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_USER)!!.push()//insert..
                            val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                            pInfo.user_key = currentFirebaseUser!!.uid
                            pDbRef!!.setValue(pInfo!!)//insert
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
                // If sign in fails, display a message to the user.
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