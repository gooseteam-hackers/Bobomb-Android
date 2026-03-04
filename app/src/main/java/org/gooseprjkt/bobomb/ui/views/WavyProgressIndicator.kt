package org.gooseprjkt.bobomb.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import org.gooseprjkt.bobomb.R



class WavyProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val bounds = RectF()


    private var waveAmplitude = 5f
    private var waveWavelength = 24f
    private var waveSpeed = 3f

    private var waveOffset = 0f
    private var isIndeterminate = true
    private var progress = 0f

    private var animator: ValueAnimator? = null

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.WavyProgressIndicator,
            defStyleAttr,
            0
        )

        waveAmplitude = typedArray.getDimension(
            R.styleable.WavyProgressIndicator_waveAmplitude,
            5f
        )
        waveWavelength = typedArray.getDimension(
            R.styleable.WavyProgressIndicator_waveWavelength,
            24f
        )
        waveSpeed = typedArray.getDimension(
            R.styleable.WavyProgressIndicator_waveSpeed,
            3f
        )

        val indicatorColor = typedArray.getColor(
            R.styleable.WavyProgressIndicator_indicatorColor,
            0xFF2196F3.toInt()
        )
        val trackColor = typedArray.getColor(
            R.styleable.WavyProgressIndicator_trackColor,
            0x302196F3.toInt()
        )

        typedArray.recycle()

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.color = indicatorColor
        paint.strokeCap = Paint.Cap.ROUND

        trackPaint.style = Paint.Style.STROKE
        trackPaint.strokeWidth = 8f
        trackPaint.color = trackColor
        trackPaint.strokeCap = Paint.Cap.ROUND

        startAnimation()
    }

    private fun startAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, waveWavelength * 2).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                waveOffset = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun setIndeterminate(indeterminate: Boolean) {
        isIndeterminate = indeterminate
        if (indeterminate) {
            startAnimation()
        } else {
            animator?.cancel()
        }
    }

    fun setProgress(progress: Int, max: Int = 100) {
        this.progress = (progress.toFloat() / max) * 100
        if (!isIndeterminate) {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2


        bounds.set(0f, centerY - 4f, width, centerY + 4f)
        canvas.drawRoundRect(bounds, 4f, 4f, trackPaint)

        if (isIndeterminate) {

            path.reset()
            var x = 0f
            var isFirstPoint = true

            while (x <= width + waveWavelength * 2) {
                val y = centerY + waveAmplitude * kotlin.math.sin(
                    (2 * Math.PI * (x + waveOffset) / waveWavelength).toFloat()
                )
                if (isFirstPoint) {
                    path.moveTo(x, y)
                    isFirstPoint = false
                } else {
                    path.lineTo(x, y)
                }
                x += 2f
            }

            canvas.drawPath(path, paint)
        } else {

            val progressWidth = (width * progress / 100f)
            path.reset()
            var x = 0f
            var isFirstPoint = true

            while (x <= progressWidth) {
                val y = centerY + waveAmplitude * kotlin.math.sin(
                    (2 * Math.PI * (x + waveOffset) / waveWavelength).toFloat()
                )
                if (isFirstPoint) {
                    path.moveTo(x, y)
                    isFirstPoint = false
                } else {
                    path.lineTo(x, y)
                }
                x += 2f
            }

            canvas.drawPath(path, paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isIndeterminate) {
            startAnimation()
        }
    }
}
