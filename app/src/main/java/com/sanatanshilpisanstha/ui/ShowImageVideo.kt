package com.sanatanshilpisanstha.ui

import android.graphics.Matrix
import android.os.Bundle
import android.os.PersistableBundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.sanatanshilpisanstha.databinding.ActivityImageVideoShowBinding
import com.sanatanshilpisanstha.utility.Extra

class ShowImageVideo: AppCompatActivity() {
    val binding by lazy {
        ActivityImageVideoShowBinding.inflate(layoutInflater)
    }
    private lateinit var matrix: Matrix
    private var scale = 1f
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()
        val intent = intent
        val image = intent.getStringExtra("image")

        matrix = Matrix()
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        gestureDetector = GestureDetector(this, GestureListener())

        if (image!=null){
            binding.imageShow.load(image)
            binding.imageShow.scaleType=ImageView.ScaleType.FIT_CENTER
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val previousScale = scale
            scale *= detector.scaleFactor
            scale = scale.coerceIn(0.1f, 5.0f) // limit zoom scale

            if (previousScale < scale) {
                // Zoom In
                adjustZoom()
            } else {
                // Zoom Out
                adjustZoom()
            }

            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val newScale = if (scale > 1) 1.0f else 2.0f

            if (newScale > scale) {
                // Zoom In
                adjustZoom()
            } else {
                // Zoom Out
                adjustZoom()
            }

            scale = newScale
            return true
        }
    }

    private fun adjustZoom() {
        binding.imageShow.scaleX = scale
        binding.imageShow.scaleY = scale

        val newHeight = binding.imageShow.height * scale
        val layoutParams = binding.imageShow.layoutParams
        layoutParams.height = newHeight.toInt()
        binding.imageShow.layoutParams = layoutParams
    }

}