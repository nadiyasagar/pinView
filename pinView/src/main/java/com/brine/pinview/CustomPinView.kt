package com.brine.pinview

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.withStyledAttributes
import com.google.android.material.button.MaterialButton

class CustomPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // ---- Constants ----
    private val pinLength = 4
    private val numbers = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "clear", "0", "done"
    )
    private val density = Resources.getSystem().displayMetrics.density

    // ---- State ----
    private var pin = StringBuilder()
    private val circleViews = mutableListOf<View>()
    private var onPinComplete: ((String) -> Unit)? = null

    // ---- Configurable Colors ----
    var circleFilledColor: Int = Color.BLACK
    var circleEmptyColor: Int = Color.TRANSPARENT
    var circleStrokeColor: Int = Color.BLACK
    var buttonBackgroundColor: Int = Color.BLACK
    var buttonTextColor: Int = Color.WHITE
    var buttonIconColor: Int = Color.WHITE
    var clearButtonBackgroundColor: Int = Color.RED
    var clearButtonIconColor: Int = Color.WHITE
    var doneButtonBackgroundColor: Int = Color.GREEN
    var doneButtonIconColor: Int = Color.WHITE
    var rippleColorForAll: Int = Color.WHITE

    private var clearButtonIconRes: Int = R.drawable.ic_close
    private var doneButtonIconRes: Int = R.drawable.ic_done

    // ---- Configurable Sizes ----
    var circleSizeDp: Int = 14
    var buttonTextSizeSp: Float = 14f
    var circleKeypadSpacing: Int = 24.dpToPx()
    var buttonSpacing: Int = 8.dpToPx()
    var circleSpacing: Int = 8.dpToPx()

    private val rowCount = 4
    private val colCount = 3

    // ---- Cached Drawables ----
    private val filledCircleDrawable by lazy { createCircleDrawable(true) }
    private val emptyCircleDrawable by lazy { createCircleDrawable(false) }

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.CustomPinView) {
                rippleColorForAll =
                    getColor(R.styleable.CustomPinView_rippleColorForAll, rippleColorForAll)
                circleFilledColor =
                    getColor(R.styleable.CustomPinView_circleFilledColor, circleFilledColor)
                circleEmptyColor =
                    getColor(R.styleable.CustomPinView_circleEmptyColor, circleEmptyColor)
                circleStrokeColor =
                    getColor(R.styleable.CustomPinView_circleStrokeColor, circleStrokeColor)
                circleSpacing =
                    getDimensionPixelSize(R.styleable.CustomPinView_circleSpacing, circleSpacing)
                buttonBackgroundColor =
                    getColor(R.styleable.CustomPinView_buttonBackgroundColor, buttonBackgroundColor)
                buttonTextColor =
                    getColor(R.styleable.CustomPinView_buttonTextColor, buttonTextColor)
                buttonIconColor =
                    getColor(R.styleable.CustomPinView_buttonIconColor, buttonIconColor)
                circleSizeDp = getDimensionPixelSize(
                    R.styleable.CustomPinView_circleSize,
                    circleSizeDp.dpToPx()
                )
                buttonTextSizeSp =
                    getDimension(R.styleable.CustomPinView_buttonTextSize, buttonTextSizeSp)
                clearButtonBackgroundColor = getColor(
                    R.styleable.CustomPinView_clearButtonBackgroundColor,
                    clearButtonBackgroundColor
                )
                clearButtonIconColor =
                    getColor(R.styleable.CustomPinView_clearButtonIconColor, clearButtonIconColor)
                doneButtonBackgroundColor = getColor(
                    R.styleable.CustomPinView_doneButtonBackgroundColor,
                    doneButtonBackgroundColor
                )
                doneButtonIconColor =
                    getColor(R.styleable.CustomPinView_doneButtonIconColor, doneButtonIconColor)
                circleKeypadSpacing = getDimensionPixelSize(
                    R.styleable.CustomPinView_circleKeypadSpacing,
                    circleKeypadSpacing
                )
                buttonSpacing =
                    getDimensionPixelSize(R.styleable.CustomPinView_buttonSpacing, buttonSpacing)
                clearButtonIconRes = getResourceId(R.styleable.CustomPinView_clearButtonIcon, clearButtonIconRes)
                doneButtonIconRes = getResourceId(R.styleable.CustomPinView_doneButtonIcon, doneButtonIconRes)
            }
        }

        inflate(context, R.layout.view_pin_input, this)
        setupCircles()
        setupKeypad()
    }

    private fun setupCircles() {
        val container = findViewById<LinearLayout>(R.id.pinCircles)
        container.removeAllViews()
        circleViews.clear()

        repeat(pinLength) {
            val size = circleSizeDp.dpToPx()
            val circle = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = circleSpacing / 2
                    marginEnd = circleSpacing / 2
                }
                background = emptyCircleDrawable
            }
            container.addView(circle)
            circleViews.add(circle)
        }
    }

    private fun setupKeypad() {
        val keypad = findViewById<GridLayout>(R.id.keypad)
        (keypad.layoutParams as? MarginLayoutParams)?.topMargin = circleKeypadSpacing
        keypad.removeAllViews()

        numbers.forEachIndexed { index, label ->
            val button = createKeyButton(label)

            val row = index / colCount
            val col = index % colCount
            button.layoutParams = gridLayoutParams(row, col)

            keypad.addView(button)
        }
    }

    private fun createKeyButton(label: String): MaterialButton =
        MaterialButton(context).apply {
            text = if (label in listOf("clear", "done")) "" else label
            textSize = buttonTextSizeSp
            rippleColor = ColorStateList.valueOf(rippleColorForAll)

            when (label) {
                "clear" -> {
                    icon = AppCompatResources.getDrawable(context, clearButtonIconRes)
                    setBackgroundColor(clearButtonBackgroundColor)
                    iconTint = ColorStateList.valueOf(clearButtonIconColor)
                }

                "done" -> {
                    icon = AppCompatResources.getDrawable(context, doneButtonIconRes)
                    setBackgroundColor(doneButtonBackgroundColor)
                    iconTint = ColorStateList.valueOf(doneButtonIconColor)
                }

                else -> {
                    setBackgroundColor(buttonBackgroundColor)
                    setTextColor(buttonTextColor)
                    iconTint = ColorStateList.valueOf(buttonIconColor)
                }
            }

            icon?.let { iconSize = buttonTextSizeSp.dpToPx() }
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            iconPadding = 0
            gravity = Gravity.CENTER

            setOnClickListener { handleInput(label) }

            // Force square shape
            addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                val size = v.measuredWidth.coerceAtMost(v.measuredHeight)
                if (size > 0 && (v.layoutParams.width != size || v.layoutParams.height != size)) {
                    v.layoutParams.width = size
                    v.layoutParams.height = size
                    v.requestLayout()
                }
            }
        }

    private fun gridLayoutParams(row: Int, col: Int): GridLayout.LayoutParams =
        GridLayout.LayoutParams().apply {
            width = 0
            height = 0
            columnSpec = GridLayout.spec(col, 1f)
            rowSpec = GridLayout.spec(row, 1f)

            val gap = buttonSpacing / 2
            setMargins(
                if (col == 0) 0 else gap,
                if (row == 0) 0 else gap,
                if (col == colCount - 1) 0 else gap,
                if (row == rowCount - 1) 0 else gap
            )
        }

    private fun handleInput(label: String) {
        when (label) {
            "clear" -> if (pin.isNotEmpty()) {
                pin.deleteAt(pin.lastIndex)
                updateCircles()
            }

            "done" -> if (pin.length == pinLength) {
                onPinComplete?.invoke(pin.toString())
            }

            else -> if (pin.length < pinLength) {
                pin.append(label)
                updateCircles()
                if (pin.length == pinLength) onPinComplete?.invoke(pin.toString())
            }
        }
    }

    private fun updateCircles() {
        circleViews.forEachIndexed { index, view ->
            view.background = if (index < pin.length) filledCircleDrawable else emptyCircleDrawable
        }
    }

    private fun createCircleDrawable(filled: Boolean): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setStroke(3, circleStrokeColor)
            setColor(if (filled) circleFilledColor else circleEmptyColor)
        }

    fun setOnPinCompleteListener(listener: (String) -> Unit) {
        onPinComplete = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = 300.dpToPx()
        val minHeight = 400.dpToPx()
        val width = MeasureSpec.getSize(widthMeasureSpec).takeIf { it > 0 } ?: minWidth
        val height = MeasureSpec.getSize(heightMeasureSpec).takeIf { it > 0 } ?: minHeight

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    private fun Int.dpToPx(): Int = (this * density).toInt()
    private fun Float.dpToPx(): Int = (this * density).toInt()
}

