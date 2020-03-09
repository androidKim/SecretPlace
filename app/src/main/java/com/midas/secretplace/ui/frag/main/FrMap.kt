package com.midas.secretplace.ui.frag.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import com.midas.secretplace.core.FirebaseDbCtrl
import com.midas.secretplace.structure.core.place
import com.midas.secretplace.ui.MyApp
import com.midas.secretplace.ui.act.ActPlaceDetail
import java.io.Serializable

class FrMap : Fragment(), OnMapReadyCallback
{
    private val MY_LOCATION_REQUEST_CODE:Int = 9999
    private lateinit var mMap: GoogleMap
    private val m_nZoomLevel = 13.0f //This goes up to 21
    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap?)
    {
        mMap = map as GoogleMap
        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap!!.isMyLocationEnabled = true//현위치 옵션
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_LOCATION_REQUEST_CODE)
        }
        getPlaceListProc("")//내가 저장한 모든 위치 가져오기
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.size == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap!!.isMyLocationEnabled = true
            } else {
                // Permission was denied. Display an error message.
                mMap!!.isMyLocationEnabled = false
            }
        }
    }
    //
    fun getPlaceListProc(categoryCode:String)
    {
        var pQuery: Query? = null
        if(categoryCode.equals("")){
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE).child(m_App!!.m_SpCtrl!!.getSpUserKey()!!).orderByKey()
        }else{
            pQuery = m_App!!.m_FirebaseDbCtrl!!.m_FirebaseDb!!.getReference(FirebaseDbCtrl.TB_PLACE)
                    .child(m_App!!.m_SpCtrl!!.getSpUserKey()!!).orderByChild("categoryCode").equalTo(categoryCode)
        }

        pQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if(dataSnapshot!!.exists())
                {
                    val children = dataSnapshot!!.children
                    children.forEach {
                        val placeItem:place = it!!.getValue(place::class.java)!!
                        if(placeItem != null){
                            placeList!!.add(placeItem)
                        }
                    }

                    if(placeList!!.size > 0) {
                        settingPlaceListData(placeList)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError)
            {

            }
        })
    }

    //리스트 장소 마커
    fun settingPlaceListData(pPlaceList:ArrayList<place>)
    {
        if(pPlaceList == null)
            return

        if(pPlaceList.size == 0)
            return

        mMap!!.clear()
        var arrLatLng:ArrayList<LatLng> = ArrayList()
        var pLatLngInfo: LatLng? = null

        //add Marker..
        for (i in 0 until pPlaceList!!.size)
        {
            var pInfo: place = pPlaceList.get(i)
            if(pInfo.lat != null && pInfo.lng != null && pInfo.name != null)
            {
                var nLat:Double = pInfo.lat!!.toDouble()
                var nLng:Double = pInfo.lng!!.toDouble()
                pLatLngInfo = LatLng(nLat, nLng)
                arrLatLng!!.add(pLatLngInfo)
                var pMarker: Marker = mMap.addMarker(MarkerOptions().position(pLatLngInfo).title(pInfo.name))
                pMarker!!.tag = pInfo

                if(i ==0)
                {
                    //move firtst item
                    val sydney = LatLng(nLat, nLng)
                    mMap.addMarker(MarkerOptions().position(sydney).title(pInfo.name))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, m_nZoomLevel))
                }
            }
        }

        //marker click
        mMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(pMarker: Marker): Boolean {
                var pInfo:place = pMarker.tag as place
                return false
            }
        })

        //marker text click
        mMap!!.setOnInfoWindowClickListener(object: GoogleMap.OnInfoWindowClickListener{
            override fun onInfoWindowClick(p0: Marker?) {
                var pInfo:place = p0!!.tag as place
                movePlaceDetail(pInfo)
                return
            }

        })

        mMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {

            }
        })

        //poly line..
        /*
        if(pLatLngInfo != null && arrLatLng.size > 0)
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pLatLngInfo, m_nZoomLevel))
            val line = mMap.addPolyline(PolylineOptions()
                    .addAll(arrLatLng!!)
                    .width(5f)
                    .color(Color.RED))
        }
        */
    }

    fun movePlaceDetail(pInfo:place) {
        var pIntent = Intent(activity, ActPlaceDetail::class.java)
        pIntent.putExtra(Constant.INTENT_DATA_PLACE_OBJECT, pInfo as Serializable)
        startActivityForResult(pIntent, 0)
    }

    var m_Context: Context? = null
    var m_Activity:Activity? = null
    var m_App:MyApp? = null
    var m_RequestManager: RequestManager? = null
    var placeList:ArrayList<place> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView:View = inflater.inflate(R.layout.frag_map, container, false)
        val mapView = rootView.findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        m_Context = context
        m_Activity = activity
        m_App = MyApp()
        if(m_App!!.m_binit == false)
            m_App!!.init(m_Context!!)

        m_RequestManager = Glide.with(m_Context)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}
