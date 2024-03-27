package com.example.colorphone.util.ext

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.makeramen.roundedimageview.RoundedImageView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


const val FOLDER_NAME = "Clean Folder" //TODO: change folder name

fun RequestManager.getBitmap(url: String): Bitmap =
    asBitmap().load(url).submit().get()

fun RequestManager.getBitmap(file: File): Bitmap =
    asBitmap().load(file).submit().get()

fun RequestManager.getBitmap(id: Int): Bitmap =
    asBitmap().load(id).submit().get()

fun RequestManager.getBitmap(uri: Uri): Bitmap =
    asBitmap().load(uri).submit().get()

fun RequestManager.getBitmap(url: String, listener: GlideBitmapListener?) {
    asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            listener?.onLoadSuccess(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    })
}

fun RequestManager.getBitmap(id: Int, success: (bitmap: Bitmap) -> Unit) {
    asBitmap().load(id).into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            success(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    })
}

fun RequestManager.getBitmap(file: File, success: (bitmap: Bitmap) -> Unit) {
    asBitmap().load(file).into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            success(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    })
}

fun RequestManager.getBitmap(url: String, success: (bitmap: Bitmap) -> Unit) {
    asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            success(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    })
}

fun RequestManager.getBitmap(url: InputStream, success: (bitmap: Bitmap) -> Unit) {
    asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            success(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    })
}

fun RequestManager.getBitmap(
    url: String,
    width: Int,
    height: Int,
    success: (bitmap: Bitmap) -> Unit
) {
    asBitmap().load(url).apply(RequestOptions().override(width, height))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    success(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
}

/*
    Get bitmap override width height
 */
fun RequestManager.getBitmap(url: String, width: Int, height: Int): Bitmap =
    asBitmap().load(url).apply(RequestOptions().override(width, height)).submit().get()

fun RequestManager.getBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap =
    asBitmap().load(bitmap).apply(RequestOptions().override(width, height)).submit().get()

fun RequestManager.getBitmapNullable(url: String, width: Int, height: Int): Bitmap? =
    try {
        asBitmap().load(url).apply(RequestOptions().override(width, height)).submit().get()
    } catch (e: java.lang.Exception) {
        null
    }


fun RequestManager.downloadImage(
    context: Context,
    url: String,
    folderName: String,
    fileName: String,
    listener: DownloadImageListener?
) {
    getBitmap(url, object : GlideBitmapListener {
        override fun onLoadSuccess(bitmap: Bitmap) {
            saveBitmap(context, bitmap, folderName, fileName, listener)
        }
    })
}

fun saveBitmap(
    context: Context,
    resource: Bitmap,
    folderName: String,
    fileName: String,
    listener: DownloadImageListener?
) {
    var fos: OutputStream? = null
    var pathUnderAndroidQ = ""
    val contentValues: ContentValues?

    try {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //android Q
            contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DCIM}/$folderName"
                )
            }

            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null)
                fos = resolver.openOutputStream(imageUri)
        } else {
            //android 9 below
            val file =
                File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/$FOLDER_NAME")
            if (!file.exists()) {
                file.mkdir()
            }

            val dest = File(file.path, "$fileName.png")
            pathUnderAndroidQ = dest.path

            fos = FileOutputStream(dest)
        }

        if (resource.compress(Bitmap.CompressFormat.PNG, 100, fos!!)) {
            listener?.onDownloadSuccess()
        } else {
            listener?.onDownloadFailed("UNKNOWN_ERROR")
        }
        //scan for android 9 below
        MediaScannerConnection.scanFile(
            context,
            arrayOf(pathUnderAndroidQ),
            null,
            null
        )
        //close fos
        fos?.flush()
        fos?.close()
    } catch (e: Exception) {
        listener?.onDownloadFailed("${e.message}")
    }
}

interface DownloadImageListener {
    fun onDownloadFailed(error: String)
    fun onDownloadSuccess()
}

interface GlideBitmapListener {
    fun onLoadSuccess(bitmap: Bitmap)
}

fun AppCompatImageView.loadUrl(url: Any, radius: Int) {
    Glide.with(this)
            .load(url)
            .apply(
                RequestOptions().transform(CenterCrop())
                        .transform(RoundedCorners(radius))
            )
            .into(this)
            .waitForLayout()
}

fun AppCompatImageView.loadUrl(url: Any) {
    Glide.with(this)
            .load(url)
            .into(this)
}

fun RoundedImageView.loadUrl(url: Int) {
    Glide.with(this)
            .load(url)
            .into(this)
}