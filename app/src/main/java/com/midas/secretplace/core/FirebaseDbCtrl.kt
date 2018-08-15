package com.midas.secretplace.core

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.midas.secretplace.structure.ReqBase
import com.midas.secretplace.structure.core.direct
import com.midas.secretplace.structure.core.distance
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.structure.core.user


class FirebaseDbCtrl
{
    companion object
    {
        //Table..
        val TB_USER:String = "tb_user"
        val TB_PLACE:String = "tb_place"
        val TB_DISTANCE:String = "tb_distance"
        val TB_DIRECT:String = "tb_direct"
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
    fun setPlaceInfo(pInfo:place):DatabaseReference
    {
        var pDbRef = m_FirebaseDb!!.getReference(TB_PLACE)!!.push()
        pDbRef.setValue(pInfo)
        return pDbRef
    }
    //---------------------------------------------------------------
    //
    fun setDistanceInfo(pInfo:distance):DatabaseReference
    {
        var pDbRef = m_FirebaseDb!!.getReference(TB_DISTANCE)!!.push()
        pDbRef.setValue(pInfo)
        return pDbRef
    }
    //---------------------------------------------------------------
    //
    fun updateDistanceLocation():DatabaseReference
    {
        var pDbRef:DatabaseReference = m_FirebaseDb!!.getReference(TB_DISTANCE)
        return pDbRef
    }
    //---------------------------------------------------------------
    //
    fun setDirectInfo(pInfo: direct):DatabaseReference
    {
        var pDbRef = m_FirebaseDb!!.getReference(TB_DIRECT)!!.push()
        pDbRef.setValue(pInfo)
        return pDbRef
    }
    //---------------------------------------------------------------
    //
    fun updateDirectLocation():DatabaseReference
    {
        var pDbRef:DatabaseReference = m_FirebaseDb!!.getReference(TB_DIRECT)
        return pDbRef
    }
    /************************* DB Getter *************************/
    //---------------------------------------------------------------
    //
    fun getPlaceList(seq:String) : Query
    {
        //var pDbRefResult:DatabaseReference = m_FirebaseDb!!.getReference("place_list")
        var pQuery:Query? = null
        if(!seq.equals(""))
            pQuery = m_FirebaseDb!!.getReference(TB_PLACE).orderByKey().startAt(seq).limitToFirst(ReqBase.ITEM_COUNT)
        else
            pQuery = m_FirebaseDb!!.getReference(TB_PLACE).orderByKey().limitToFirst(ReqBase.ITEM_COUNT)

        return pQuery
    }

    //---------------------------------------------------------------
    //
    fun getDistanceList(seq:String) : Query
    {
        var pQuery:Query? = null
        if(!seq.equals(""))
            pQuery = m_FirebaseDb!!.getReference(TB_DISTANCE).orderByKey().startAt(seq).limitToFirst(ReqBase.ITEM_COUNT)
        else
            pQuery = m_FirebaseDb!!.getReference(TB_DISTANCE).orderByKey().limitToFirst(ReqBase.ITEM_COUNT)

        return pQuery
    }
    //---------------------------------------------------------------
    //
    fun getDirectList(seq:String) : Query
    {
        var pQuery:Query? = null
        if(!seq.equals(""))
            pQuery = m_FirebaseDb!!.getReference(TB_DIRECT).orderByKey().startAt(seq).limitToFirst(ReqBase.ITEM_COUNT)
        else
            pQuery = m_FirebaseDb!!.getReference(TB_DIRECT).orderByKey().limitToFirst(ReqBase.ITEM_COUNT)

        return pQuery
    }
}