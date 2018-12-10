package com.midas.secretplace.common

class Constant
{
    companion object
    {
        //Intent
        val INTENT_DATA_PLACE_OBJECT:String = "INTENT_DATA_PLACE_OBJECT"
        val INTENT_DATA_GROUP_OBJECT:String = "INTENT_DATA_GROUP_OBJECT"
        val INTENT_DATA_PLACE_LIST_OBJECT:String = "INTENT_DATA_PLACE_LIST_OBJECT"
        val INTENT_DATA_DIRECT_OBJECT:String = "INTENT_DATA_DIRECT_OBJECT"
        val INTENT_DATA_INTERFACE:String = "INTENT_DATA_INTERFACE"

        //Permission
        val REQUEST_ID_MULTIPLE_PERMISSIONS:Int = 1000
        val PERMISSION_ACCESS_FINE_LOCATION:Int = 1001
        val PERMISSION_ACCESS_COARSE_LOCATION:Int = 1002
        val PERMISSION_WRITE_EXTERNAL_STORAGE:Int = 1003
        val PERMISSION_CAMERA = 1004

        //ActivityForResult
        val FOR_RESULT_IS_REFRESH:Int = 2000

        //Define..
    }
}