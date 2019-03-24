package com.midas.secretplace.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.widget.Toast
import com.midas.secretplace.common.Constant
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import android.location.Geocoder
import android.location.Address
import com.midas.secretplace.R
import java.util.*


class Util
{
    //-----------------------------------------------------------------
    //static
    companion object
    {
        private val TMP_FILE_NAME = "ImageFile"
        private val fileFormate = ".JPEG"
        //-----------------------------------------------------------------
        //
        fun getTempFilePath(context: Context): File
        {
            var file: File? = null
            file = File(getExternalFolder(context), TMP_FILE_NAME + fileFormate)
            if (file!!.exists()) {
                file!!.delete()
            }
            file = File(getExternalFolder(context), TMP_FILE_NAME + fileFormate)
            return file
        }
        //-----------------------------------------------------------------
        //
        fun getExternalFolder(context: Context): File?
        {
            try
            {
                val state = Environment.getExternalStorageState()
                if (Environment.MEDIA_MOUNTED.equals(state))
                {
                    val file = File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name))
                    file.mkdir()
                    return file
                }
                else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
                {
                    Toast.makeText(context, "Can not write on external storage.",
                            Toast.LENGTH_LONG).show()
                    return null
                }
                else
                {
                    return null
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            return null
        }

        //-----------------------------------------------------------------
        //getUri From bitmap..
        fun getImageUri(context: Context, inImage: Bitmap): Uri
        {
            val bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null)
            return Uri.parse(path)
        }
        /****************************** toolbar ******************************/
        //--------------------------------------------------------------
        //set theme color
        fun setToolbarBackgroundColor(pContext:Context, toolbar: android.support.v7.widget.Toolbar, strTheme:String)
        {
            when(strTheme)
            {
                Constant.THEME_PINK -> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDarkPink))
                Constant.THEME_RED -> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDarkRed))
                Constant.THEME_PUPLE -> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDarkPuple))
                Constant.THEME_DEEPPUPLE -> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDarkDeepPuple))
                Constant.THEME_INDIGO-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDarkIndigo))
                Constant.THEME_BLUE-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDarkBlue))
                Constant.THEME_LIGHTBLUE-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDarkLightBlue))
                Constant.THEME_CYAN-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDarkCyan))
                Constant.THEME_TEAL-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryTeal))
                Constant.THEME_GREEN-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryGreen))
                Constant.THEME_LIGHTGREEN-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryLightGreen))
                Constant.THEME_LIME-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryLime))
                Constant.THEME_YELLOW-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryYellow))
                Constant.THEME_AMBER-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryAmber))
                Constant.THEME_ORANGE-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryOrange))
                Constant.THEME_DEEPORANGE-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDeepOrange))
                Constant.THEME_BROWN-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryBrown))
                Constant.THEME_GRAY-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryGray))
                Constant.THEME_BLUEGRAY-> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryBlueGray))
                else -> toolbar.background = ColorDrawable(ContextCompat.getColor(pContext, R.color.colorPrimaryDark))
            }
        }


        /****************************** Bitmap ******************************/
        fun getBitmapFromPath(filePath:String):Bitmap
        {
            val imgFile = File(filePath)
            if (imgFile.exists())
            {
                val myBitmap:Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                //Drawable d = new BitmapDrawable(getResources(), myBitmap);
                return  myBitmap
            }
            else
                return null!!
        }

        //-----------------------------------------------------------------
        //samsung device ..etc rotation issue 방지
        fun getRotatePathBitmap(photoPath:String, bitmap:Bitmap):Bitmap
        {

            val exif = ExifInterface(photoPath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

            try {
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_NORMAL -> return bitmap
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                        matrix.setRotate(180f)
                        matrix.postScale(-1f, 1f)
                    }
                    ExifInterface.ORIENTATION_TRANSPOSE -> {
                        matrix.setRotate(90f)
                        matrix.postScale(-1f, 1f)
                    }
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
                    ExifInterface.ORIENTATION_TRANSVERSE -> {
                        matrix.setRotate(-90f)
                        matrix.postScale(-1f, 1f)
                    }
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
                    else -> return bitmap
                }
                try
                {
                    val bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    bitmap.recycle()
                    return bmRotated
                } catch (e: OutOfMemoryError) {
                    e.printStackTrace()
                    return null!!
                }

            } catch (e: IOException) {
                e.printStackTrace()
                return null!!
            }

            return bitmap!!
        }
        //-----------------------------------------------------------------
        //
        fun getRotateBitmap(bitmap:Bitmap):Bitmap
        {
            try {
                val matrix = Matrix()
                matrix.setRotate(90f)

                try
                {
                    val bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    bitmap.recycle()
                    return bmRotated
                } catch (e: OutOfMemoryError) {
                    e.printStackTrace()
                    return null!!
                }

            } catch (e: IOException) {
                e.printStackTrace()
                return null!!
            }

            return bitmap!!
        }

        //-----------------------------------------------------------------
        //
        fun rotateImage(source: Bitmap, angle: Float): Bitmap
        {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
                    matrix, true)
        }

        //-----------------------------------------------------------------
        //
        fun setTheme(context:Context, strTheme:String): Resources.Theme
        {
            val theme = context!!.theme
            when (strTheme) {
                Constant.THEME_PINK -> theme.applyStyle(R.style.AppThemePink, true)
                Constant.THEME_RED -> theme.applyStyle(R.style.AppThemeRed, true)
                Constant.THEME_PUPLE -> theme.applyStyle(R.style.AppThemePuple, true)
                Constant.THEME_DEEPPUPLE -> theme.applyStyle(R.style.AppThemeDeepPuple, true)
                Constant.THEME_INDIGO-> theme.applyStyle(R.style.AppThemeIndigo, true)
                Constant.THEME_BLUE-> theme.applyStyle(R.style.AppThemeBlue, true)
                Constant.THEME_LIGHTBLUE-> theme.applyStyle(R.style.AppThemeLightBlue, true)
                Constant.THEME_CYAN-> theme.applyStyle(R.style.AppThemeCyan, true)
                Constant.THEME_TEAL-> theme.applyStyle(R.style.AppThemeTeal, true)
                Constant.THEME_GREEN-> theme.applyStyle(R.style.AppThemeGreen, true)
                Constant.THEME_LIGHTGREEN-> theme.applyStyle(R.style.AppThemeLightGreen, true)
                Constant.THEME_LIME-> theme.applyStyle(R.style.AppThemeLime, true)
                Constant.THEME_YELLOW-> theme.applyStyle(R.style.AppThemeYello, true)
                Constant.THEME_AMBER-> theme.applyStyle(R.style.AppThemeAmber, true)
                Constant.THEME_ORANGE-> theme.applyStyle(R.style.AppThemeOrange, true)
                Constant.THEME_DEEPORANGE-> theme.applyStyle(R.style.AppThemeDeepOrange, true)
                Constant.THEME_BROWN-> theme.applyStyle(R.style.AppThemeBrown, true)
                Constant.THEME_GRAY-> theme.applyStyle(R.style.AppThemeGray, true)
                Constant.THEME_BLUEGRAY-> theme.applyStyle(R.style.AppThemeBlueGray, true)
                else -> theme.applyStyle(com.midas.secretplace.R.style.AppTheme, true)
            }
            return theme
        }
        //-----------------------------------------------------------------
        //
        fun getAddress(pContext:Context, latitude:Double, longitude: Double):String
        {
            val geocoder: Geocoder
            val addresses: List<Address>
            geocoder = Geocoder(pContext, Locale.getDefault())

            addresses = geocoder.getFromLocation(latitude, longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if(addresses.size > 0)
            {
                val address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                val city = addresses[0].getLocality()
                val state = addresses[0].getAdminArea()
                val country = addresses[0].getCountryName()
                val postalCode = addresses[0].getPostalCode()
                val knownName = addresses[0].getFeatureName() // Only if available else return NULL
                return address
            }
            else
            {
                return ""
            }
        }
    }

    fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()
    fun Int.pxToDp(displayMetrics: DisplayMetrics): Int = (this / displayMetrics.density).toInt()
}