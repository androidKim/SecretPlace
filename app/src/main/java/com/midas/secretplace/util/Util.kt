package com.midas.secretplace.util

import android.util.DisplayMetrics

class Util
{
    companion object
    {

    }


    fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()
    fun Int.pxToDp(displayMetrics: DisplayMetrics): Int = (this / displayMetrics.density).toInt()

}