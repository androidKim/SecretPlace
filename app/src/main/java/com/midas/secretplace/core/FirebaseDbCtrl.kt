package com.midas.secretplace.core

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.structure.core.user


class FirebaseDbCtrl
{
    companion object
    {
        //Table..
        val TB_USER:String = "tb_user"
        val TB_PLACE:String = "tb_place"
        val TB_IMG:String = "tb_img"
        val TB_GROUP:String = "tb_group"
        val TB_GROUP_PLACE:String = "tb_group_place"
        val TB_COUPLE:String = "tb_couple"
        val TB_COUPLE_PLACE:String = "tb_couple_place"
    }

    var m_FirebaseDb: FirebaseDatabase? = null
    var m_PlaceList:ArrayList<place> = ArrayList<place>()
    constructor()
    {
        m_FirebaseDb = FirebaseDatabase.getInstance()
        //val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
        //"/"+currentFirebaseUser!!.uid
    }

    /************************* DB Setter *************************/
    //---------------------------------------------------------------
    //
    fun setUser(pInfo: user)
    {
        if(pInfo == null)
            return

        //var pMap:Map<String, Any> = pInfo.toMap()

        //val key = m_FirebaseDb!!.getReference(TB_USER)!!.push().key
        //var childUpdates:HashMap<String, Any> = HashMap()
        //childUpdates.put("/"+TB_USER+"/"+key, pMap)
        //childUpdates.put("/user-messages/" + user!!.uid + "/" + key, messageValues)

        //m_FirebaseDb!!.getReference(TB_USER).updateChildren(childUpdates)

        //ef("-Users/"+currentUser.uid).update({ displayName: "New trainer" });

        //pInfo.name="업데이트되나"

        //insert & update..

        m_FirebaseDb!!.getReference(TB_USER)!!.child(pInfo.sns_type+pInfo.sns_key).setValue(pInfo)

        //ref.child("myDb").child("awais@gmailcom").child("leftSpace").setValue("YourDateHere");
        //pDbRef.setValue(pInfo)

        //var pDbRef = m_FirebaseDb!!.getReference(TB_USER)!!.push()
        //pDbRef.updateChildren(pMap)
    }
    //---------------------------------------------------------------
    //user key
    fun getUserDbRef():DatabaseReference
    {
        var pDbRef = m_FirebaseDb!!.getReference(TB_USER)
        return pDbRef
    }
    //---------------------------------------------------------------
    //
    /*
    fun setPlaceInfo(pInfo:place_list_item):DatabaseReference
    {
        var pDbRef:DatabaseReference
        if(pInfo.place_info == null)//update
        {
            //pDbRef =

            return m_FirebaseDb!!.getReference(TB_PLACE)!!.child("place_info").push()!!.setValue(pInfo!!.place_info)//insert
        }
        else//insert
        {
            //pDbRef =
            return m_FirebaseDb!!.getReference(TB_PLACE)!!.child("place_info").setValue(pInfo!!.place_info)

        }
        //return pDbRef
    }
    */


}