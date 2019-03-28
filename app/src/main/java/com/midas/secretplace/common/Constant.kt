package com.midas.secretplace.common

class Constant
{
    companion object
    {
        //URL
        val FIRE_STORE_URL = "gs://secretplace-29d5e.appspot.com"
        val TERM_URL = "http://54.180.109.122:8081/terms"

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
        val FOR_RESULT_IS_REFRESH:Int = 2000//새로고침 필요
        val FOR_RESULT_REQUEST_FOR_ME:Int = 2001//나에게 온 요청 Activity 종료

        //theme
        val THEME_PINK:String = "THEME_PINK"
        val THEME_RED:String = "THEME_RED"
        val THEME_PUPLE:String = "THEME_PUPLE"
        val THEME_DEEPPUPLE:String = "THEME_DEEPPUPLE"
        val THEME_INDIGO:String = "THEME_INDIGO"
        val THEME_BLUE:String = "THEME_BLUE"
        val THEME_LIGHTBLUE:String = "THEME_LIGHTBLUE"
        val THEME_CYAN:String = "THEME_CYAN"
        val THEME_TEAL:String = "THEME_TEAL"
        val THEME_GREEN:String = "THEME_GREEN"
        val THEME_LIGHTGREEN:String = "THEME_LIGHTGREEN"
        val THEME_LIME:String = "THEME_LIME"
        val THEME_YELLOW:String = "THEME_YELLOW"
        val THEME_AMBER:String = "THEME_AMBER"
        val THEME_ORANGE:String = "THEME_ORANGE"
        val THEME_DEEPORANGE:String = "THEME_DEEPORANGE"
        val THEME_BROWN:String = "THEME_BROWN"
        val THEME_GRAY:String = "THEME_GRAY"
        val THEME_BLUEGRAY:String = "THEME_BLUEGRAY"
        //Define..

        //image
        val IMAGE_DIRECTORY = "/scplace"
        val REQUEST_TAKE_PHOTO = 1001
        val REQUEST_SELECT_IMAGE_IN_ALBUM = 1002
    }
}