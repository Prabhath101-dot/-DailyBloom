package com.example.moodflow.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class LiquidProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val path = Path()
    private var progressPercentage: Float = 0f
    private var liquidColor: Int = Color.parseColor("#8B5CF6") // Default purple

    fun setProgress(percentage: Float) {
        progressPercentage = percentage.coerceIn(0f, 100f)
        invalidate()
    }

    fun setLiquidColor(color: Int) {
        liquidColor = color
        paint.color = liquidColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        
        // Calculate the width of the liquid blob based on percentage
        val liquidWidth = (width * progressPercentage / 100f)
        
        if (liquidWidth <= 0) return
        
        // Create the curved blob path
        path.reset()
        
        // Start from bottom-left
        path.moveTo(0f, height)
        
        // Curve up along left edge
        path.quadTo(0f, height * 0.8f, 0f, height * 0.6f)
        
        // Curve to the right with a wave effect
        val waveAmplitude = height * 0.1f
        val waveFrequency = 2f
        
        // Create the main blob body with curved right edge
        val rightEdgeX = liquidWidth
        val rightEdgeY = height * 0.3f
        
        // Bottom curve to right edge
        path.quadTo(
            rightEdgeX * 0.3f, height * 0.9f,
            rightEdgeX * 0.7f, height * 0.8f
        )
        
        // Create the concave curve on the right edge
        path.quadTo(
            rightEdgeX, height * 0.7f,
            rightEdgeX, height * 0.5f
        )
        
        // Top curve of the right edge
        path.quadTo(
            rightEdgeX, height * 0.3f,
            rightEdgeX * 0.8f, height * 0.2f
        )
        
        // Curve back to top-left
        path.quadTo(
            rightEdgeX * 0.4f, height * 0.1f,
            0f, height * 0.2f
        )
        
        // Close the path back to start
        path.quadTo(0f, height * 0.4f, 0f, height)
        path.close()
        
        // Draw the liquid blob
        paint.color = liquidColor
        canvas.drawPath(path, paint)
    }
}
