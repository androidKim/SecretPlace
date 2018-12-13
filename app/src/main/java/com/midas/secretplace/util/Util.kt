package com.midas.secretplace.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.widget.Toast
import com.midas.secretplace.R
import com.midas.secretplace.common.Constant
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


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
                Constant.THEME_BLUE -> theme.applyStyle(R.style.AppThemePink, true)
                Constant.THEME_PINK -> theme.applyStyle(R.style.AppThemePink, true)
                else -> theme.applyStyle(R.style.AppThemePink, true)
            }
            return theme
        }

    }

    fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()
    fun Int.pxToDp(displayMetrics: DisplayMetrics): Int = (this / displayMetrics.density).toInt()


}