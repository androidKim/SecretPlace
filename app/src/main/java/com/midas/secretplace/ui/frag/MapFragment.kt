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
import com.midas.secretplace.structure.core.*


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
        var pGroupInfo: group? = null


        if(args!!.containsKey(Constant.INTENT_DATA_PLACE_OBJECT))
        {
            pPlaceInfo = args!!.getSerializable(Constant.INTENT_DATA_PLACE_OBJECT) as place
        }
        else if(args!!.containsKey(Constant.INTENT_DATA_GROUP_OBJECT))
        {
            pGroupInfo = args!!.getSerializable(Constant.INTENT_DATA_GROUP_OBJECT) as group
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
        else if(pGroupInfo != null)
        {
            /*
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
            */
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