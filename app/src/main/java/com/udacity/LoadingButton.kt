package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var fillFraction = 0f
    private val textBounds = Rect()

    private lateinit var bitmap: Bitmap
    private lateinit var buffer: Canvas

    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000
        interpolator = LinearInterpolator()
        addUpdateListener {
            fillFraction = it.animatedValue as Float
            invalidate()
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                buttonState = ButtonState.Loading
            }

            override fun onAnimationEnd(animation: Animator?) {
                buttonState = ButtonState.Completed
                fillFraction = 0f
            }

        })
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) {_, _, new ->
        isClickable = new.clickable
        currentText = resources.getString(new.textId)
        if (new.animate) valueAnimator.start()

        invalidate()
    }

    private var bgColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    private var fillColor = ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null)
    private var circleColor = ResourcesCompat.getColor(resources, R.color.colorAccent, null)

    private var currentText = resources.getString(R.string.button_name)
    private val defaultTextSize = resources.getDimension(R.dimen.default_text_size)

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = defaultTextSize
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            bgColor = getColor(R.styleable.LoadingButton_completeColor, bgColor)
            fillColor = getColor(R.styleable.LoadingButton_loadingColor, fillColor)
            circleColor = getColor(R.styleable.LoadingButton_circleColor, circleColor)

            textPaint.color = circleColor
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bufferedDraw(canvas)
    }

    private fun bufferedDraw(destination: Canvas) {

        // Draw the background
        buffer.drawColor(bgColor)

        // Draw the filling part based in the current animation fraction
        buffer.save()
        buffer.clipRect(0f, 0f, widthSize * fillFraction, heightSize * 1f)
        buffer.drawColor(fillColor)
        buffer.restore()

        // Draw the text
        val label: String = currentText
        val textWidth = textPaint.measureText(label)
        buffer.drawText(
            label,
            (widthSize - textWidth) / 2f,
            (heightSize - (textPaint.ascent() + textPaint.descent())) / 2f,
            textPaint
        )

        // Draw the arc part, filled based in current animation fraction
        buffer.save()
        textPaint.getTextBounds(label, 0, label.length, textBounds)

        val radius = textBounds.height().toFloat()
        buffer.translate((widthSize + textWidth + radius) / 2f, heightSize / 2f - radius / 2)
        buffer.drawArc(0f, 0f, radius, radius, 0f, 360f * fillFraction, true, textPaint)
        buffer.restore()

        destination.drawBitmap(bitmap, 0f, 0f, null)

    }

    override fun performClick(): Boolean {
        buttonState = ButtonState.Clicked

        return super.performClick()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (::bitmap.isInitialized) bitmap.recycle()

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        buffer = Canvas(bitmap)

    }
}