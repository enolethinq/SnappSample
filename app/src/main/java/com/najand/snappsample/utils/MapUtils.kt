package com.najand.snappsample.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.najand.snappsample.R

object MapUtils {

    fun getCarBitmap(context: Context): Bitmap{
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car)
        return Bitmap.createScaledBitmap(bitmap, 50, 100, false)
    }
    fun getLocationBitmap(context: Context): Bitmap {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.location)
        return Bitmap.createScaledBitmap(bitmap, 50, 100, false)
    }
}