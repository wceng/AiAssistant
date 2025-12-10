package com.wceng.app.aiassistant.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.ceil

object ImageUtils {
    /**
     * 将 Uri 转为 Base64 字符串（先按 maxWidth 缩放，再压缩为 JPEG）
     * 返回 null 表示失败或 Uri 为空
     */
    suspend fun uriToBase64(
        context: Context,
        uri: Uri?,
        maxWidth: Int = 1024,
        quality: Int = 85
    ): String? = withContext(Dispatchers.IO) {
        if (uri == null) return@withContext null

        try {
            val resolver = context.contentResolver

            // 获取图片的EXIF方向信息
            val orientation = resolver.openInputStream(uri).use { input ->
                if (input == null) return@withContext null
                val exif = ExifInterface(input)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL

            // 第一次读取图片尺寸
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri).use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // 计算采样率
            val inSampleSize = calculateInSampleSize(options, maxWidth)

            // 第二次读取并解码图片
            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val bitmap = resolver.openInputStream(uri).use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions) ?: return@withContext null
            }

            // 根据EXIF信息旋转图片
            val rotatedBitmap = rotateBitmapBasedOnExif(bitmap, orientation)

            // 压缩为JPEG格式
            val baos = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            val base64String = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

            return@withContext "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, maxWidth: Int): Int {
        val (width, height) = options.outWidth to options.outHeight
        var inSampleSize = 1

        if (maxOf(width, height) > maxWidth) {
            val ratio = maxOf(width, height).toFloat() / maxWidth
            inSampleSize = ceil(ratio.toDouble()).toInt()
        }

        return inSampleSize.coerceAtLeast(1)
    }

    private fun rotateBitmapBasedOnExif(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
