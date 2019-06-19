package net.ukr.zubenko.g.criminalintent

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.R.attr.y
import android.R.attr.x
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Point
import android.widget.Toast
import org.jetbrains.anko.configuration


object PictureUtils {
    fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
        // Чтение размеров изображения на диске
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val srcWidth = options.outWidth.toFloat()
        val srcHeight = options.outHeight.toFloat()

        // Вычисление степени масштабирования
        var inSampleSize = 1
        if (srcHeight > destHeight || srcWidth > destWidth) {
            val heightScale = srcHeight / destHeight
            val widthScale = srcWidth / destWidth
            inSampleSize = Math.round(
                if (heightScale > widthScale)
                    heightScale
                else
                    widthScale
            )
        }
        options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize
        // Чтение данных и создание итогового изображения
        return BitmapFactory.decodeFile(path, options)
    }

    fun getScaledBitmap(path: String, activity: Activity): Bitmap {
        var size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        return getScaledBitmap(path, size.x, size.y)
    }

    fun getBitmap(path: String): Bitmap {
        return BitmapFactory.decodeFile(path, BitmapFactory.Options())
    }
}