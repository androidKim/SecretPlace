
package com.midas.secretplace.ui.act

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.util.Util
import kotlinx.android.synthetic.main.act_memo.*

/*
내가 좋아요 한 위치 리스트
 */
class ActMemo : AppCompatActivity()
{

    /*********************** Define ***********************/

    /*********************** Member ***********************/
    private var m_App: MyApp? = null
    private var m_Context: Context? = null
    private var m_PlaceInfo:place? = null
    /*********************** Controller ***********************/

    /*********************** System Function ***********************/
    //--------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        m_Context = this
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context as ActMemo)

        Util.setTheme(m_Context!!, m_App!!.m_SpCtrl!!.getSpTheme()!!)
        setContentView(R.layout.act_memo)

        initValue()
        recvIntentData()
        initLayout()
    }
    //--------------------------------------------------------------
    //
    override fun onStart()
    {
        super.onStart()
    }
    //--------------------------------------------------------------
    //
    override fun onStop()
    {
        super.onStop()
    }
    //---------------------------------------------------------------------------------------------------
    //
    override fun onBackPressed()
    {
        super.onBackPressed()
    }
    //---------------------------------------------------------------------------------------------------
    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)//Intent?  <-- null이 올수도있다
    {
        super.onActivityResult(requestCode, resultCode, data)
    }

    //--------------------------------------------------------------
    //
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        when (requestCode)
        {
            0 -> {

            }
        }
    }
    /*********************** Menu Function ***********************/
    //--------------------------------------------------------------
    //툴바 옵션 메뉴
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        var menuItem1: MenuItem = menu!!.findItem(R.id.action_share).setVisible(false)
        var menuItem2: MenuItem = menu!!.findItem(R.id.share_location).setVisible(false)
        var menuItem3: MenuItem = menu!!.findItem(R.id.show_map).setVisible(false)
        var menuItem4: MenuItem = menu!!.findItem(R.id.edit).setVisible(false)
        var menuItem5: MenuItem = menu!!.findItem(R.id.add_photo).setVisible(false)

        return super.onCreateOptionsMenu(menu)
    }
    //--------------------------------------------------------------
    //툴바 옵션메뉴 이벤트
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_map -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    /*********************** User Function ***********************/
    //--------------------------------------------------------------
    //변수 초기화..
    fun initValue()
    {

    }
    //--------------------------------------------------------------
    //
    fun recvIntentData()
    {
        var pIntent: Intent = intent

        if(pIntent == null)
            return

        if(pIntent.hasExtra(Constant.INTENT_DATA_PLACE_OBJECT))
        {
            m_PlaceInfo =  pIntent.extras.get(Constant.INTENT_DATA_PLACE_OBJECT) as place
            if(m_PlaceInfo!!.memo == null)
                m_PlaceInfo!!.memo = ""

            if(m_PlaceInfo!!.memo.equals(""))
            {
                ly_Content.visibility = View.GONE
                ly_NoData.visibility = View.VISIBLE
                ly_Edit.visibility = View.VISIBLE
            }
            else
            {
                tvMemo.setText(m_PlaceInfo!!.memo+"")
                ly_Content.visibility = View.VISIBLE
                ly_NoData.visibility = View.GONE
                ly_Edit.visibility = View.GONE
            }
        }
    }
    //--------------------------------------------------------------
    //레이아웃 초기동작
    fun initLayout()
    {
        toolbar.title = m_Context!!.resources!!.getString(R.string.title_memo)
        setSupportActionBar(toolbar)//enable app bar
        var actionBar: ActionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        toolbar.setNavigationOnClickListener { view -> onBackPressed() }
        var strTheme:String = m_App!!.m_SpCtrl!!.getSpTheme()!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setToolbarBackgroundColor(m_Context!!, this.toolbar, strTheme!!)
        }

        //메모 업로드 버튼..
        ivUploadMemo.setOnClickListener {
            val strMsg:String = editMemo.text.toString().trim()
            if(!strMsg.equals(""))//입력된 메모가 있으면..
            {
                m_PlaceInfo?.memo = strMsg//메모
                var pDbRef:DatabaseReference? = null
                pDbRef =  m_App?.m_FirebaseDbCtrl?.m_FirebaseDb?.getReference(FirebaseDbCtrl.TB_PLACE)!!
                        .child(m_App?.m_SpCtrl?.getSpUserKey())!!
                        .child(m_PlaceInfo?.place_key)

                pDbRef!!.setValue(m_PlaceInfo)//insert
                pDbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot?)
                    {
                        if (dataSnapshot!!.exists())
                        {
                            //update compelte
                            ly_Edit.visibility = View.GONE
                            ly_NoData.visibility = View.GONE
                            ly_Content.visibility = View.VISIBLE
                            tvMemo.setText(m_PlaceInfo?.memo+"")
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?)
                    {

                    }
                })
            }
        }

        //메모 수정버튼
        ivEditIcon.setOnClickListener{
            ly_Edit.visibility = View.VISIBLE
            editMemo.setText(m_PlaceInfo?.memo+"")
            ly_NoData.visibility = View.GONE
            ly_Content.visibility = View.GONE
        }
    }
    /************************* listener *************************/
    //--------------------------------------------------------------
    //메모 업로드 하기
    fun onClickUploadMemo(view:View)
    {

    }
    /************************* callback function *************************/
    /*********************** interface ***********************/
}
