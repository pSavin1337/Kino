package com.lospollos.cinemahallview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class CinemaHallView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var cinemaHallBackgroundColor = Color.GRAY
    private val emptyPlacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
    }
    private val selectedPlacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
    }
    private val takenPlacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
    }
    private val screenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }
    private val screenTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = SCREEN_TEXT_SIZE
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = PLACE_SIZE / 2
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val rowTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = ROW_TEXT_SIZE
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private var rightBound: Float = 0f
    private var bottomBound: Float = 0f
    private var leftBound: Float = 0f
    private var topBound: Float = 0f

    private var contentWidth: Float = 0f
    private var contentHeight: Float = 0f

    var cinemaHallMatrix: List<MutableList<Byte>> = ArrayList()
        set(value) {
            field = value
            invalidate()
        }

    var isViewEnabled = true

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CinemaHallView, 0, 0)
        val backgroundColor =
            typedArray.getColor(R.styleable.CinemaHallView_backgroundColor, Color.GRAY)
        cinemaHallBackgroundColor = backgroundColor
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        contentWidth = (GAP + PLACE_SIZE) * cinemaHallMatrix[FIRST_ROW_INDEX].size
        contentHeight = (GAP + PLACE_SIZE) * cinemaHallMatrix.size

        val desiredWidth =
            paddingLeft + paddingRight + GAP + contentWidth + rowTextPaint.measureText(
                ROW_MAX_TEXT
            ) * 2
        val desiredHeight = paddingLeft + paddingRight + GAP + contentHeight + SCREEN_HEIGHT + GAP

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec).toFloat()

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> desiredWidth.coerceAtMost(widthSize)
            else -> desiredWidth
        }
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightSize)
            else -> desiredHeight
        }
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    private fun isNothingRow(row: List<Byte>): Boolean {
        var res = true
        row.forEach {
            res = res && (it == NOTHING)
        }
        return res
    }

    private fun drawScreen(
        canvas: Canvas?,
        screenX: Float,
        screenY: Float,
        textPaint: Paint,
        screenPaint: Paint
    ) {
        var x = screenX
        var y = screenY
        canvas?.drawRoundRect(
            x,
            y,
            x + contentWidth + GAP + rowTextPaint.measureText(ROW_MAX_TEXT) * 2,
            y + SCREEN_HEIGHT,
            ROUND_X,
            ROUND_Y,
            screenPaint
        )
        x += GAP
        y += GAP
        x += GAP
        canvas?.drawText(
            SCREEN_TEXT,
            (contentWidth + GAP + rowTextPaint.measureText(ROW_MAX_TEXT) * 2) / 2,
            SCREEN_HEIGHT / 2 + SCREEN_TEXT_GAP,
            textPaint
        )
    }

    private fun drawRowNumber(
        canvas: Canvas?,
        x: Float,
        y: Float,
        textPaint: Paint,
        rowNumber: Int
    ) {
        canvas?.drawText(
            ROW_TEXT + rowNumber,
            x + rowTextPaint.measureText(ROW_MAX_TEXT) / 2,
            y + PLACE_SIZE / 2 + TEXT_GAP,
            textPaint
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawColor(cinemaHallBackgroundColor)

        var x = 0f
        var y = 0f
        drawScreen(canvas, x, y, screenTextPaint, screenPaint)

        leftBound = x
        x += GAP * 2
        y += GAP * 2 + SCREEN_HEIGHT
        topBound = y
        var rowNumber = 1
        cinemaHallMatrix.forEach {
            x = 0f
            if (isNothingRow(it)) {
                rowNumber--
            } else {
                drawRowNumber(canvas, x, y, rowTextPaint, rowNumber)
            }
            x += rowTextPaint.measureText(ROW_MAX_TEXT) + GAP
            var placeNumber = 1
            it.forEach { place ->
                when (place) {
                    NOTHING -> {
                        placeNumber--
                    }
                    EMPTY -> {
                        drawPlace(canvas, x, y, emptyPlacePaint)
                    }
                    SELECTED -> {
                        drawPlace(canvas, x, y, selectedPlacePaint)
                        canvas?.drawText(
                            placeNumber.toString(),
                            x + PLACE_SIZE / 2,
                            y + PLACE_SIZE / 2 + TEXT_GAP,
                            textPaint
                        )
                    }
                    TAKEN -> {
                        drawPlace(canvas, x, y, takenPlacePaint)
                    }
                }
                placeNumber++
                x += GAP + PLACE_SIZE
            }
            if (!isNothingRow(it)) {
                drawRowNumber(canvas, x, y, rowTextPaint, rowNumber)
            }
            rowNumber++
            y += GAP + PLACE_SIZE
        }
        rightBound = x - GAP
        bottomBound = y - GAP
    }

    private fun drawPlace(canvas: Canvas?, x: Float, y: Float, paint: Paint) {
        canvas?.drawRoundRect(
            x,
            y,
            x + PLACE_SIZE,
            y + PLACE_SIZE,
            ROUND_X,
            ROUND_Y,
            paint
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isViewEnabled) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {

                    var stepX = 0f
                    var placeNumber = 0
                    val x = event.x - rowTextPaint.measureText(ROW_MAX_TEXT)
                    do {
                        stepX += PLACE_SIZE + GAP
                        placeNumber++
                    } while (stepX < x)
                    placeNumber--
                    stepX -= (PLACE_SIZE + GAP)
                    val xAboutOnePlace = x - stepX

                    var stepY = 0f
                    var rowNumber = 0
                    val y = event.y - GAP - SCREEN_HEIGHT
                    do {
                        stepY += PLACE_SIZE + GAP
                        rowNumber++
                    } while (stepY < y)
                    rowNumber--
                    stepY -= (PLACE_SIZE + GAP)
                    val yAboutOnePlace = y - stepY

                    if (xAboutOnePlace > GAP &&
                        yAboutOnePlace > GAP &&
                        event.x < rightBound &&
                        event.y < bottomBound &&
                        event.x > leftBound &&
                        event.y > topBound
                    ) {
                        when (cinemaHallMatrix[rowNumber][placeNumber]) {
                            EMPTY -> cinemaHallMatrix[rowNumber][placeNumber] = SELECTED
                            SELECTED -> cinemaHallMatrix[rowNumber][placeNumber] = EMPTY
                        }
                    }
                }
            }
            invalidate()
        }
        return true
    }

    fun confirm() {
        var i = 0
        cinemaHallMatrix.forEach {
            var j = 0
            it.forEach { place ->
                if (place == SELECTED) {
                    cinemaHallMatrix[i][j] = TAKEN
                }
                j++
            }
            i++
        }
        invalidate()
    }

    fun refresh() {
        var i = 0
        cinemaHallMatrix.forEach {
            var j = 0
            it.forEach { place ->
                if (place != NOTHING) {
                    cinemaHallMatrix[i][j] = EMPTY
                }
                j++
            }
            i++
        }
        invalidate()
    }

    private companion object {
        const val FIRST_ROW_INDEX = 0
        const val NOTHING: Byte = 0
        const val EMPTY: Byte = 1
        const val SELECTED: Byte = 2
        const val TAKEN: Byte = 3
        const val GAP = 70f
        const val PLACE_SIZE = 100f
        const val TEXT_GAP = 20f
        const val SCREEN_TEXT_GAP = 15f
        const val ROUND_X = 20f
        const val ROUND_Y = 20f
        const val SCREEN_TEXT_SIZE = 35f
        const val SCREEN_HEIGHT = 100f
        const val SCREEN_TEXT = "screen"
        const val ROW_TEXT = "Row "
        const val ROW_TEXT_SIZE = 40f
        const val ROW_MAX_TEXT = "Row 9"
    }

}