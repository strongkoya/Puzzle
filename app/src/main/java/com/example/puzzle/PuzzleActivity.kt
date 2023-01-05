package com.example.puzzle

import android.R.attr.x
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*


class PuzzleActivity : AppCompatActivity() {
    var pieces: ArrayList<PuzzlePiece>? = null
    var mCurrentPhotoPath: String? = null
    var mCurrentPhotoUri: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)
        val layout = findViewById<RelativeLayout>(R.id.layout)

        val imageView = findViewById<ImageView>(R.id.ImageView)

        val intent = intent
        val assetName = intent.getStringExtra("assetName")
        mCurrentPhotoPath = intent.getStringExtra("mCurrentPhotoPath")
        mCurrentPhotoUri = intent.getStringExtra("mCurrentPhotoUri")
        Log.d("mCurrentPhotoUri ", "mCurrentPhotoUri.toString()")
        //   Log.d("mCurrentPhotoUri ",mCurrentPhotoUri.toString())
        // run image related code after the view was laid out
        // to have all dimensions calculated

        imageView.post {
            Log.d("Asset Name :", assetName.toString())
            if (assetName != null) {
                setPicFromAsset(assetName, imageView)
            } else if (mCurrentPhotoPath != null) {
                setPicFromPhotoPath(mCurrentPhotoPath!!, imageView)

            } else if (mCurrentPhotoUri != null) {
                imageView.setImageURI(Uri.parse(mCurrentPhotoUri))
            }
            pieces = splitImage()
            val touchListener = TouchListener(this@PuzzleActivity)
            //shuffle pieces order

            Log.d("Pieces length : ", pieces!!.size.toString())
            Collections.shuffle(pieces)
            for (piece in pieces!!) {
                piece.setOnTouchListener(touchListener)
                layout.addView(piece)

                //randomize position , ont the bottom of the screen

                val lParams = piece.layoutParams as RelativeLayout.LayoutParams
                //a verifer Random.nextInt ou nextInt
                val random = Random()
                lParams.leftMargin = random.nextInt(
                    layout.width - piece.pieceWidth
                )

                lParams.topMargin = layout.height - piece.pieceHeight
                // lParams.topMargin = layout.height - 150
                piece.layoutParams = lParams

            }


        }
    }


    private fun setPicFromAsset(assetName: String, imageView: ImageView?) {

        val targetW = imageView!!.width
        val targetH = imageView!!.height
        val am = assets

        try {

            val inputStream = am.open("img/$assetName")

            val bitmap = BitmapFactory.decodeStream(
                inputStream
            )

            if (bitmap == null) {
                Log.d("null nulll null :", "null null null")
            }

            imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this@PuzzleActivity, e.localizedMessage, Toast.LENGTH_LONG).show()
        }

    }

    private fun splitImage(): ArrayList<PuzzlePiece> {

        val piecesNumber = 12
        //val piecesNumber = 4
        val rows = 4
        val cols = 3
        /*val piecesNumber = 9
        val rows = 3
        val cols = 3*/
        val imageView = findViewById<ImageView>(R.id.ImageView)
        val pieces = ArrayList<PuzzlePiece>(piecesNumber)
        // val pieces = ArrayList<Bitmap>(piecesNumber)

        //get the scaled bitmap of the source image
        //val bitmap : Bitmap = imageView.drawable.toBitmap()
        val drawable = imageView.drawable as BitmapDrawable
        val bitmap = drawable.bitmap

        val scaledBitmapLeft = imageView.left;
        val scaledBitmapTop = imageView.top;
        val scaledBitmapWidth = imageView.width
        val scaledBitmapHeight = imageView.height


        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap, scaledBitmapWidth, scaledBitmapHeight, true
        )


        val pieceWidth = scaledBitmapWidth / cols
        val pieceHeight = scaledBitmapHeight / rows

        var yCoord = 0
        for (row in 0 until rows) {
            var xCoord = 0
            for (col in 0 until cols) {


                val pieceBitmap = Bitmap.createBitmap(
                    scaledBitmap, xCoord, yCoord,
                    scaledBitmapWidth / cols, scaledBitmapHeight / rows
                )

                val piece = PuzzlePiece(applicationContext)
                piece.setImageBitmap(pieceBitmap)


                piece.xCoord = xCoord + imageView.left
                piece.yCoord = yCoord + imageView.top

                piece.pieceWidth = pieceWidth
                piece.pieceHeight = pieceHeight



                piece.setImageBitmap(pieceBitmap)
                pieces.add(piece)
                xCoord += pieceWidth

            }
            yCoord += pieceHeight
        }
        return pieces

    }

    fun checkGameOver() {

        if (isGameOver) {
            AlertDialog.Builder(this@PuzzleActivity)
                .setTitle("Bravo !!!!")
                .setIcon(R.drawable.ic_celebration)
                .setMessage("Vous avez Gagné .. !!!\n Voulez-vous rejouer?")
                .setPositiveButton("Oui") { dialog, _ ->
                    finish()
                    dialog.dismiss()
                }
                .setNegativeButton("Non") { dialog, _ ->

                    dialog.dismiss()
                }
                .create()
                .show()
        }

    }

    private val isGameOver: Boolean
        private get() {
            for (piece in pieces!!) {
                if (piece.canMove) {
                    return false
                }
            }
            return true

        }

    private fun setPicFromPhotoPath(mCurrentPhotoPath: String, imageView: ImageView?) {

        //get the dimensions of the view
        val targetW = imageView!!.width
        val targetH = imageView!!.height

        //get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()

        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)

        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        //determine how much to scale down the image

        val scaleFactor = Math.min(
            photoW / targetW, photoH / targetH
        )

        //decode the image file into a bitmap sized to fill the view
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true
        val bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)

        var rotatedBitmap = bitmap

        // rotate bitmap if needed
        try {
            val ei = ExifInterface(mCurrentPhotoPath)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    rotatedBitmap = rotateImage(bitmap, 90f)
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    rotatedBitmap = rotateImage(bitmap, 180f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    rotatedBitmap = rotateImage(bitmap, 270f)
                }


            }

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this@PuzzleActivity, e.localizedMessage, Toast.LENGTH_LONG).show()
        }

        imageView.setImageBitmap(rotatedBitmap)

    }

    companion object {
        fun rotateImage(source: Bitmap, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)

            return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height, matrix, true
            )
        }
    }
}