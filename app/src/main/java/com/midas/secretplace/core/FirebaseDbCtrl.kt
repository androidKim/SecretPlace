package com.midas.secretplace.core

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.midas.secretplace.structure.core.ReqBase
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.structure.core.user
import java.util.*



class FirebaseDbCtrl
{
    companion object
    {
        //Table..
        val TB_USER:String = "tb_user"
        val TB_PLACE:String = "tb_place"
    }

    public var m_FirebaseDb: FirebaseDatabase? = null
    var m_PlaceList:ArrayList<place> = ArrayList<place>()
    constructor()
    {
        m_FirebaseDb = FirebaseDatabase.getInstance()
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
        var pDbRef = m_FirebaseDb!!.getReference(TB_USER)!!.child(pInfo.join_type+pInfo.key).setValue(pInfo)


        //ref.child("myDb").child("awais@gmailcom").child("leftSpace").setValue("YourDateHere");
        //pDbRef.setValue(pInfo)





        //var pDbRef = m_FirebaseDb!!.getReference(TB_USER)!!.push()
        //pDbRef.updateChildren(pMap)
    }

    //---------------------------------------------------------------
    //
    fun setPlaceItem(pInfo:place)
    {
        var pDbRef = m_FirebaseDb!!.getReference(TB_PLACE)!!.push()
        pDbRef.setValue(pInfo)
    }

    /************************* DB Getter *************************/
    //---------------------------------------------------------------
    //
    fun getPlaceList(startKey:String) : Query
    {
        //var pDbRefResult:DatabaseReference = m_FirebaseDb!!.getReference("place_list")
        var pQuery:Query? = null
        if(!startKey.equals(""))
            pQuery = m_FirebaseDb!!.getReference(TB_PLACE).orderByKey().startAt(startKey).limitToFirst(ReqBase.ITEM_COUNT)
        else
            pQuery = m_FirebaseDb!!.getReference(TB_PLACE).orderByKey().limitToFirst(ReqBase.ITEM_COUNT)

        return pQuery
    }

}