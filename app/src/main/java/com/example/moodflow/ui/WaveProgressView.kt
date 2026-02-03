package com.example.moodflow.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.animation.ValueAnimator
import kotlin.math.min

class WaveProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.parseColor("#80FFFFFF")
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#3B82F6")
    }

    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#AA3B82F6")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 48f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private var progressFraction: Float = 0f
    private var waveOffset: Float = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            waveOffset = it.animatedFraction
            invalidate()
        }
    }

    init {
        animator.start()
    }

    fun setProgressFraction(fraction: Float) {
        progressFraction = fraction.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val radius = size / 2f
        val cx = width / 2f
        val cy = height / 2f

        // Clip to circle
        val save = canvas.save()
        val clipPath = Path().apply { addCircle(cx, cy, radius, Path.Direction.CW) }
        canvas.clipPath(clipPath)

        // Background
        canvas.drawColor(Color.parseColor("#113B82F6"))

        // Water level y
        val level = height * (1f - progressFraction)
        val waveAmplitude = size * 0.04f
        val waveLength = size * 0.6f
        val path = Path()
        path.moveTo(-waveLength + waveOffset * waveLength, level)
        var x = -waveLength
        while (x <= width + waveLength) {
            val controlX = x + waveLength / 2f
            val controlY = level + if ((x / waveLength).toInt() % 2 == 0) waveAmplitude else -waveAmplitude
            val endX = x + waveLength
            val endY = level
            path.quadTo(controlX, controlY, endX, endY)
            x += waveLength
        }
        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(0f, height.toFloat())
        path.close()
        canvas.drawPath(path, wavePaint)

        canvas.restoreToCount(save)

        // Border
        canvas.drawCircle(cx, cy, radius - borderPaint.strokeWidth / 2f, borderPaint)

        // Draw percentage text
        val percentage = (progressFraction * 100).toInt()
        val text = "$percentage%"
        
        // Calculate text position (center of circle)
        val textY = cy + (textPaint.descent() + textPaint.ascent()) / 2
        
        // Draw text
        canvas.drawText(text, cx, textY, textPaint)
    }
}

