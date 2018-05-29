package com.example.emmanuelozibo.mlkit

import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.lang.Exception

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private var actionToggleDrawerToggle: ActionBarDrawerToggle? = null
    private var firebaseVisionImage: FirebaseVisionImage? = null
    private var rawImage: ImageView? = null;private var processedImage: ImageView? = null;
    private var extracted_text: TextView? = null
    private var imageBitmap:Bitmap? = null
    private var paint: Paint? = null;val canvas = Canvas();var drawerLayout:DrawerLayout?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this)
        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.mainToolbar)
        rawImage = findViewById(R.id.rawImage);extracted_text = findViewById(R.id.text)
        processedImage = findViewById(R.id.processedImage)
        drawerLayout = findViewById<DrawerLayout>(R.id.nav_drawer)
        actionToggleDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        actionToggleDrawerToggle?.syncState()

        paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 1f
        color = Color.RED
        style = Paint.Style.STROKE}

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.file -> openFile()
            R.id.camera -> openCamera()
        }
        return true
    }

    private fun openFile(){

        val fileIntent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        startActivityForResult(fileIntent, 2)

    }

    private fun openCamera(){
        //todo: implement this
        dispatchCameraIntent()
    }

    private fun dispatchCameraIntent(){
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(packageManager) != null){
            startActivityForResult(pictureIntent, REQUESTCODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUESTCODE){
            val extra = data?.extras
            val imageBitmap = extra?.get("data") as Bitmap
            rawImage?.visibility = View.VISIBLE
            rawImage?.setImageBitmap(imageBitmap)
            firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap)
            extractText(firebaseVisionImage, imageBitmap)
        }else if (requestCode == 2){
            val uri = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            rawImage?.visibility = View.VISIBLE
            rawImage?.setImageBitmap(bitmap)
            firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
            extractText(firebaseVisionImage, bitmap)
        }

    }

    private fun extractText(firebaseVisionImage: FirebaseVisionImage?, bitmap: Bitmap){
        imageBitmap = bitmap
        val firebaseTextDetector = FirebaseVision.getInstance().visionTextDetector
        firebaseTextDetector.detectInImage(firebaseVisionImage!!)
                .addOnSuccessListener(object : OnSuccessListener<FirebaseVisionText>{
                    override fun onSuccess(p0: FirebaseVisionText?) {
                        val textFromEachBlock = StringBuffer()
                        for(block in p0?.blocks!!){
                            textFromEachBlock.append(block.text +"\n")
                            Log.i("text", textFromEachBlock.toString())
                            drawOverEachBlock(imageBitmap, block)
                        }
                    }

                })
                .addOnFailureListener(object : OnFailureListener{
                    override fun onFailure(p0: Exception) {
                        Toast.makeText(this@MainActivity, "${p0.message}", Toast.LENGTH_LONG).show()
                    }

                })

    }

    private fun drawOverEachBlock(imgBit: Bitmap?, block: FirebaseVisionText.Block?) {
        val offset = 50

        val newBit  = imgBit?.copy(imgBit.config, true)
        val rectF = RectF()
        rectF.set(block?.boundingBox)
        canvas.setBitmap(newBit)
        canvas.drawRect(rectF, paint)
        imageBitmap = newBit
        displayImage(newBit)
    }

    private fun displayImage(newBitmap: Bitmap?) {
        if (processedImage?.visibility == View.GONE){
            processedImage?.visibility = View.VISIBLE
            processedImage?.setImageBitmap(newBitmap)
        }else{
            processedImage?.setImageBitmap(newBitmap)
        }

    }


    companion object {
        val REQUESTCODE = 1
    }

}
