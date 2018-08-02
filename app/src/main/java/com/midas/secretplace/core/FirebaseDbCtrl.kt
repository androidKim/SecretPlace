package com.midas.secretplace.core

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.midas.secretplace.structure.core.ReqBase
import com.midas.secretplace.structure.core.place

class FirebaseDbCtrl
{
    public var m_FirebaseDb: FirebaseDatabase? = null
    var m_PlaceList:ArrayList<place> = ArrayList<place>()
    constructor()
    {
        m_FirebaseDb = FirebaseDatabase.getInstance()
    }


    /************************* DB Setter *************************/
    //---------------------------------------------------------------
    //
    fun setPlaceItem(pInfo:place)
    {
        var pDbRefList = m_FirebaseDb!!.getReference("place_list")
        var newRef: DatabaseReference = pDbRefList!!.push()
        newRef.setValue(pInfo)
    }

    /************************* DB Getter *************************/
    //---------------------------------------------------------------
    //
    fun getPlaceList(startKey:String) : Query
    {
        //var pDbRefResult:DatabaseReference = m_FirebaseDb!!.getReference("place_list")
        var pQuery:Query? = null
        if(!startKey.equals(""))
            pQuery = m_FirebaseDb!!.getReference("place_list").orderByKey().startAt(startKey).limitToFirst(ReqBase.ITEM_COUNT)
        else
            pQuery = m_FirebaseDb!!.getReference("place_list").orderByKey().limitToFirst(ReqBase.ITEM_COUNT)

        return pQuery
    }

}