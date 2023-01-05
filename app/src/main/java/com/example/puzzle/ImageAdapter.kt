package com.example.puzzle

import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
import java.io.IOException

class ImageAdapter(private val mContext: Context) : BaseAdapter() {

    val am: AssetManager = mContext.assets
    private var files: Array<String>? = null


    override fun getCount(): Int = files!!.size

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        val v = LayoutInflater.from(mContext).inflate(R.layout.grid_element, null)

        val imageView = v.findViewById<ImageView>(R.id.gridImageView)
        imageView.post {
            object : AsyncTask<Any?, Any?, Any>() {
                private var bitmap: Bitmap? = null

                //A verfifier poition
                override fun doInBackground(vararg poition: Any?): Any? {
                    // Toast.makeText(mContext,position.toString(),Toast.LENGTH_LONG).show()
                    bitmap = getPicFromAsset(imageView, files!![position])

                    Log.d("files!![position  ] :", files!![position])
                    return null
                }

                override fun onPostExecute(result: Any?) {
                    super.onPostExecute(result)
                    imageView.setImageBitmap(bitmap)
                }

            }.execute()
        }
        return v
    }

    private fun getPicFromAsset(imageView: ImageView?, assetName: String): Bitmap? {

        val targetW = imageView!!.width
        val targetH = imageView!!.height

        if (targetW == 0 || targetH == 0) {
            //view has no dimension set
            return null
        } else try {
            val inputStream = am.open("img/$assetName")

            val bitmap = BitmapFactory.decodeStream(
                inputStream
            )

            if (bitmap == null) {
                return null
            }
            return bitmap


        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }

    init {
        try {
            files = am.list("img")

        } catch (e: IOException) {
            e.printStackTrace()

        }
    }
}