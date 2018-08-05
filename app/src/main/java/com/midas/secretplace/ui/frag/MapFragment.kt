package com.midas.secretplace.ui.frag

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.midas.secretplace.common.Constant
import com.midas.secretplace.structure.core.place

class MapFragment : SupportMapFragment(), OnMapReadyCallback
{
    private lateinit var mMap: GoogleMap
    private val m_nZoomLevel = 13.0f //This goes up to 21
    /********************** System function **********************/
    //------------------------------------------------------------
    //
    override fun onMapReady(map: GoogleMap?)
    {
        var args = arguments
        var pInfo:place = args!!.getSerializable(Constant.INTENT_DATA_PLACE_OBJECT) as place



        mMap = map as GoogleMap



        var nLat:Double = pInfo.lat!!.toDouble()
        var nLng:Double = pInfo.lng!!.toDouble()
        val sydney = LatLng(nLat, nLng)
        mMap.addMarker(MarkerOptions().position(sydney).title(pInfo.name))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, m_nZoomLevel))
    }
    /********************** User function **********************/
    //------------------------------------------------------------
    //
    companion object
    {

    }



}