package com.midas.secretplace.util

import android.content.Context
import android.util.DisplayMetrics
import android.provider.MediaStore
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files.delete
import java.nio.file.Files.exists
import android.widget.Toast
import android.os.Environment.MEDIA_MOUNTED_READ_ONLY
import com.midas.secretplace.R.string.app_name
import android.os.Environment.getExternalStorageDirectory
import android.os.Environment.MEDIA_MOUNTED
import com.midas.secretplace.R
import android.R.attr.bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import android.text.TextUtils
import java.io.FileOutputStream
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
        //-----------------------------------------------------------------
        //samsung device ..etc rotation issue 방지
        fun getRotateBitmap(photoPath:String, bitmap:Bitmap):Bitmap
        {
            val ei = ExifInterface(photoPath)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

            var rotatedBitmap: Bitmap? = null
            when (orientation) {
                ExifInterface.ORIENTATION_UNDEFINED ->rotatedBitmap = rotateImage(bitmap!!, 0)
                ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(bitmap!!, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(bitmap!!, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(bitmap!!, 270)
                ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
                else -> rotatedBitmap = bitmap
            }

            return rotatedBitmap!!
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
    }

    fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()
    fun Int.pxToDp(displayMetrics: DisplayMetrics): Int = (this / displayMetrics.density).toInt()


}