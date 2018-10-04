package com.midas.secretplace.ui.frag

import android.annotation.SuppressLint
import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.*


class MapFragment : SupportMapFragment(), OnMapReadyCallback
{
    private lateinit var mMap: GoogleMap
    private val m_nZoomLevel = 13.0f //This goes up to 21
    private var m_IfCallback:ifCallback? = null
    /********************** System function **********************/
    //------------------------------------------------------------
    //
    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap?)
    {
        var args = arguments
        var pPlaceInfo:place? = null
        var pPlaceList:ArrayList<place>? = null


        if(args!!.containsKey(Constant.INTENT_DATA_PLACE_OBJECT))
        {
            pPlaceInfo = args!!.getSerializable(Constant.INTENT_DATA_PLACE_OBJECT) as place
        }
        else if(args!!.containsKey(Constant.INTENT_DATA_PLACE_LIST_OBJECT))
        {
            pPlaceList = args!!.getSerializable(Constant.INTENT_DATA_PLACE_LIST_OBJECT) as ArrayList<place>
        }
        else
        {

        }

        mMap = map as GoogleMap
        mMap!!.isMyLocationEnabled = true//현위치 옵션
        if(pPlaceInfo != null)
        {
            var nLat:Double = pPlaceInfo.lat!!.toDouble()
            var nLng:Double = pPlaceInfo.lng!!.toDouble()
            val sydney = LatLng(nLat, nLng)
            mMap.addMarker(MarkerOptions().position(sydney).title(pPlaceInfo.name))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, m_nZoomLevel))
        }
        else if(pPlaceList != null)
        {

            var arrLatLng:ArrayList<LatLng> = ArrayList()
            var pLatLngInfo:LatLng? = null

            for (i in 0 until pPlaceList!!.size)
            {
                var pInfo: place = pPlaceList.get(i)
                var nLat:Double = pInfo.lat!!.toDouble()
                var nLng:Double = pInfo.lng!!.toDouble()
                pLatLngInfo = LatLng(nLat, nLng)
                arrLatLng!!.add(pLatLngInfo)
                var pMarker:Marker = mMap.addMarker(MarkerOptions().position(pLatLngInfo).title(pInfo.name))
                pMarker!!.tag = pInfo

                mMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
                    override fun onMarkerClick(pMarker: Marker): Boolean {
                        var pInfo:place = pMarker.tag as place

                        if(m_IfCallback != null)
                            m_IfCallback!!.selectPlaceItem(pInfo)

                        return false
                    }
                })

                mMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
                    override fun onMapClick(latLng: LatLng) {

                    }
                })
            }

            //poly line..
            if(pLatLngInfo != null && arrLatLng.size > 0)
            {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pLatLngInfo, m_nZoomLevel))

                val line = map.addPolyline(PolylineOptions()
                        .addAll(arrLatLng!!)
                        .width(5f)
                        .color(Color.RED))
            }

        }
        else
        {

        }
    }
    /********************** User function **********************/
    //------------------------------------------------------------
    //
    companion object
    {

    }
    //------------------------------------------------------------
    //
    fun setIfCallback(pCallback:ifCallback)
    {
        m_IfCallback = pCallback
    }
    /********************** interface **********************/
    //------------------------------------------------------------
    //
    interface ifCallback
    {
        fun selectPlaceItem(pInfo:place)
    }


}