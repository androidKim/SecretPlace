package com.midas.secretplace.ui.frag

import android.annotation.SuppressLint
import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.direct
import com.midas.secretplace.structure.core.distance
import com.midas.secretplace.structure.core.location_info
import com.midas.secretplace.structure.core.place



class MapFragment : SupportMapFragment(), OnMapReadyCallback
{
    private lateinit var mMap: GoogleMap
    private val m_nZoomLevel = 13.0f //This goes up to 21
    /********************** System function **********************/
    //------------------------------------------------------------
    //
    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap?)
    {
        var args = arguments
        var pPlaceInfo:place? = null
        var pDistanceInfo:distance? = null
        var pDirectInfo:direct? = null

        if(args!!.containsKey(Constant.INTENT_DATA_PLACE_OBJECT))
        {
            pPlaceInfo = args!!.getSerializable(Constant.INTENT_DATA_PLACE_OBJECT) as place
        }
        else if(args!!.containsKey(Constant.INTENT_DATA_DISTANCE_OBJECT))
        {
            pDistanceInfo = args!!.getSerializable(Constant.INTENT_DATA_DISTANCE_OBJECT) as distance
        }
        else if(args!!.containsKey(Constant.INTENT_DATA_DIRECT_OBJECT))
        {
            pDirectInfo = args!!.getSerializable(Constant.INTENT_DATA_DIRECT_OBJECT) as direct
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
        else if(pDistanceInfo != null)
        {
            var arrLatLng:ArrayList<LatLng> = ArrayList()
            var pLatLngInfo:LatLng? = null
            if(pDistanceInfo.location_list != null)
            {
                for (i in 0 until pDistanceInfo.location_list!!.size)
                {
                    var pInfo: location_info = pDistanceInfo.location_list!!.get(i)
                    var nLat:Double = pInfo.lat!!.toDouble()
                    var nLng:Double = pInfo.lng!!.toDouble()
                    pLatLngInfo = LatLng(nLat, nLng)
                    arrLatLng!!.add(pLatLngInfo)
                    mMap.addMarker(MarkerOptions().position(pLatLngInfo).title(pDistanceInfo.name))
                }
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
        else if(pDirectInfo != null)
        {
            var arrLatLng:ArrayList<LatLng> = ArrayList()
            var pLatLngInfo:LatLng? = null
            if(pDirectInfo.location_list != null)
            {
                for (i in 0 until pDirectInfo.location_list!!.size)
                {
                    var pInfo: location_info = pDirectInfo.location_list!!.get(i)
                    var nLat:Double = pInfo.lat!!.toDouble()
                    var nLng:Double = pInfo.lng!!.toDouble()
                    pLatLngInfo = LatLng(nLat, nLng)
                    arrLatLng!!.add(pLatLngInfo)
                    mMap.addMarker(MarkerOptions().position(pLatLngInfo).title(pDirectInfo.name))
                }
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



}