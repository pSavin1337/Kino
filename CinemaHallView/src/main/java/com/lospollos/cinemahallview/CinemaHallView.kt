package com.lospollos.cinemahallview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
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
    private var textPaint: Paint
    private val rowTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = ROW_TEXT_SIZE
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private var rightBound: Float = 0f
    private var bottomBound: Float = 0f
    private var leftBound: Float = 0f
    private var topBound: Float = 0f
    private var placeSize: Float = 0f
    private var gap: Float = 0f

    private var contentWidth: Float = 0f
    private var contentHeight: Float = 0f

    private var placeTextRectangle = Rect()

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
        val defaultTextPlaceSize = 50f
        val textPlaceSize =
            typedArray.getFloat(R.styleable.CinemaHallView_placeSize, defaultTextPlaceSize)
        val defaultGap = 70f
        val gapBetweenPlaces =
            typedArray.getFloat(R.styleable.CinemaHallView_gapBetweenPlaces, defaultGap)
        gap = gapBetweenPlaces
        cinemaHallBackgroundColor = backgroundColor
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = textPlaceSize
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val maxValueOneDigit = 9
        val placeMaxText = if (cinemaHallMatrix.size > maxValueOneDigit)
            PLACE_MAX_TEXT_UPPER_TEN
        else
            PLACE_MAX_TEXT_LOWER_TEN
        val placeTextStart = 0
        textPaint.getTextBounds(
            placeMaxText,
            placeTextStart,
            placeMaxText.length,
            placeTextRectangle
        )
        placeSize = PLACE_TEXT_GAP * 2 + placeTextRectangle.width()

        contentWidth = (gap + placeSize) * cinemaHallMatrix[FIRST_ROW_INDEX].size
        contentHeight = (gap + placeSize) * cinemaHallMatrix.size

        val desiredWidth =
            paddingLeft + paddingRight + gap + contentWidth + rowTextPaint.measureText(
                ROW_MAX_TEXT
            ) * 2
        val desiredHeight = paddingLeft + paddingRight + gap + contentHeight + SCREEN_HEIGHT + gap

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
            x + contentWidth + gap + rowTextPaint.measureText(ROW_MAX_TEXT) * 2,
            y + SCREEN_HEIGHT,
            ROUND_X,
            ROUND_Y,
            screenPaint
        )
        x += gap * 2
        y += gap
        canvas?.drawText(
            SCREEN_TEXT,
            (contentWidth + gap + rowTextPaint.measureText(ROW_MAX_TEXT) * 2) / 2,
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
            y + placeSize / 2 + TEXT_GAP,
            textPaint
        )
    }

    private fun drawPlace(canvas: Canvas?, x: Float, y: Float, paint: Paint) {
        canvas?.drawRoundRect(
            x,
            y,
            x + placeSize,
            y + placeSize,
            ROUND_X,
            ROUND_Y,
            paint
        )
    }

    private fun drawPlaceText(
        canvas: Canvas?,
        x: Float,
        y: Float,
        placeNumber: Int,
        textPaint: Paint
    ) {
        canvas?.drawText(
            placeNumber.toString(),
            x + placeSize / 2,
            y + placeTextRectangle.height() + ((placeSize - placeTextRectangle.height()) / 2),
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
        x += gap * 2
        y += gap * 2 + SCREEN_HEIGHT
        topBound = y
        var rowNumber = 1
        cinemaHallMatrix.forEach {
            x = 0f
            if (isNothingRow(it)) {
                rowNumber--
            } else {
                drawRowNumber(canvas, x, y, rowTextPaint, rowNumber)
            }
            x += rowTextPaint.measureText(ROW_MAX_TEXT) + gap
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
                        drawPlaceText(canvas, x, y, placeNumber, textPaint)
                    }
                    TAKEN -> {
                        drawPlace(canvas, x, y, takenPlacePaint)
                    }
                }
                placeNumber++
                x += gap + placeSize
            }
            if (!isNothingRow(it)) {
                drawRowNumber(canvas, x, y, rowTextPaint, rowNumber)
            }
            rowNumber++
            y += gap + placeSize
        }
        rightBound = x - gap
        bottomBound = y - gap
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
                        stepX += placeSize + gap
                        placeNumber++
                    } while (stepX < x)
                    placeNumber--
                    stepX -= (placeSize + gap)
                    val xAboutOnePlace = x - stepX

                    var stepY = 0f
                    var rowNumber = 0
                    val y = event.y - gap - SCREEN_HEIGHT
                    do {
                        stepY += placeSize + gap
                        rowNumber++
                    } while (stepY < y)
                    rowNumber--
                    stepY -= (placeSize + gap)
                    val yAboutOnePlace = y - stepY

                    if (xAboutOnePlace > gap &&
                        yAboutOnePlace > gap &&
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
        const val PLACE_MAX_TEXT_LOWER_TEN = "9"
        const val PLACE_MAX_TEXT_UPPER_TEN = "99"
        const val PLACE_TEXT_GAP = 40f
    }

}