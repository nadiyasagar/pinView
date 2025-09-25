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
import kotlin.apply
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.let
import kotlin.math.min
import kotlin.text.deleteAt
import kotlin.text.isNotEmpty
import kotlin.text.lastIndex

class CustomPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val pinLength = 4
    private var pin = StringBuilder()
    private val circleViews = mutableListOf<View>()
    private var onPinComplete: ((String) -> Unit)? = null

    private val numbers = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "clear", "0", "done"
    )

    // 🌈 Colors (defaults)
    var circleFilledColor: Int = Color.BLACK
    var circleEmptyColor: Int = Color.TRANSPARENT
    var circleStrokeColor: Int = Color.BLACK
    var buttonBackgroundColor: Int = Color.BLACK
    var buttonTextColor: Int = Color.WHITE
    var buttonIconColor: Int = Color.WHITE

    // ✅ Separate for action buttons
    var clearButtonBackgroundColor: Int = Color.RED
    var clearButtonIconColor: Int = Color.WHITE
    var doneButtonBackgroundColor: Int = Color.GREEN
    var doneButtonIconColor: Int = Color.WHITE

    // ⚡ New customizable sizes
    var circleSizeDp: Int = 14       // default 20dp
    var buttonTextSizeSp: Float = 14f // default 18sp

    init {
        // ✅ Read custom attributes if provided in XML
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.CustomPinView) {
                circleFilledColor = getColor(
                    R.styleable.CustomPinView_circleFilledColor,
                    circleFilledColor
                )
                circleEmptyColor = getColor(
                    R.styleable.CustomPinView_circleEmptyColor,
                    circleEmptyColor
                )
                circleStrokeColor = getColor(
                    R.styleable.CustomPinView_circleStrokeColor,
                    circleStrokeColor
                )
                buttonBackgroundColor = getColor(
                    R.styleable.CustomPinView_buttonBackgroundColor,
                    buttonBackgroundColor
                )
                buttonTextColor = getColor(
                    R.styleable.CustomPinView_buttonTextColor,
                    buttonTextColor
                )
                buttonIconColor = getColor(
                    R.styleable.CustomPinView_buttonIconColor,
                    buttonIconColor
                )
                circleSizeDp = getDimensionPixelSize(
                    R.styleable.CustomPinView_circleSize,
                    circleSizeDp.dpToPx()
                ).toInt()
                buttonTextSizeSp = getDimension(
                    R.styleable.CustomPinView_buttonTextSize,
                    buttonTextSizeSp
                )

                clearButtonBackgroundColor = getColor(
                    R.styleable.CustomPinView_clearButtonBackgroundColor, clearButtonBackgroundColor
                )
                clearButtonIconColor = getColor(
                    R.styleable.CustomPinView_clearButtonIconColor, clearButtonIconColor
                )
                doneButtonBackgroundColor = getColor(
                    R.styleable.CustomPinView_doneButtonBackgroundColor, doneButtonBackgroundColor
                )
                doneButtonIconColor = getColor(
                    R.styleable.CustomPinView_doneButtonIconColor, doneButtonIconColor
                )

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
            val circle = View(context).apply {
                val size = circleSizeDp.dpToPx()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = 12
                    marginEnd = 12
                }
                background = createCircleDrawable(false)
            }
            container.addView(circle)
            circleViews.add(circle)
        }
    }


    private fun setupKeypad() {
        val keypad = findViewById<GridLayout>(R.id.keypad)
        keypad.removeAllViews()

        numbers.forEach { label ->
            val button = MaterialButton(context).apply {
                text = if (label == "clear" || label == "done") "" else label
                textSize = buttonTextSizeSp // ⚡ Set text size dynamically
                icon = when (label) {
                    "clear" -> AppCompatResources.getDrawable(context, R.drawable.ic_close)
                    "done" -> AppCompatResources.getDrawable(context, R.drawable.ic_done)
                    else -> null
                }
                // ⚡ Apply colors
                when (label) {
                    "clear" -> {
                        setBackgroundColor(clearButtonBackgroundColor)
                        iconTint = ColorStateList.valueOf(clearButtonIconColor)
                    }
                    "done" -> {
                        setBackgroundColor(doneButtonBackgroundColor)
                        iconTint = ColorStateList.valueOf(doneButtonIconColor)
                    }
                    else -> {
                        setBackgroundColor(buttonBackgroundColor)
                        setTextColor(buttonTextColor)
                        iconTint = ColorStateList.valueOf(buttonIconColor)
                    }
                }
                icon?.let {
                    iconSize = buttonTextSizeSp.dpToPx() // use dpToPx for pixels
                }
                // ✅ Adjust icon placement depending on type
                iconGravity = if (label == "clear" || label == "done") {
                    MaterialButton.ICON_GRAVITY_TEXT_START // keeps it centered if text is empty
                } else {
                    MaterialButton.ICON_GRAVITY_TOP
                }

                // ✅ Add padding balance
                iconPadding = if (label == "clear" || label == "done") 0 else 8.dpToPx()

                gravity = Gravity.CENTER

                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                }

                setOnClickListener { handleInput(label) }
            }
            button.post {
                val size = min(button.width, button.height)
                button.layoutParams.width = size
                button.layoutParams.height = size
                button.requestLayout()
            }

            keypad.addView(button)
        }
    }

    private fun handleInput(label: String) {
        when (label) {
            "clear" -> {
                if (pin.isNotEmpty()) {
                    pin.deleteAt(pin.lastIndex)
                    updateCircles()
                }
            }
            "done" -> {
                if (pin.length == pinLength) {
                    onPinComplete?.invoke(pin.toString())
                }
            }
            else -> {
                if (pin.length < pinLength) {
                    pin.append(label)
                    updateCircles()

                    // ✅ Call onPinComplete automatically when PIN is complete
                    if (pin.length == pinLength) {
                        onPinComplete?.invoke(pin.toString())
                    }
                }
            }
        }
    }

    private fun updateCircles() {
        circleViews.forEachIndexed { index, view ->
            view.background = createCircleDrawable(index < pin.length)
        }
    }

    private fun createCircleDrawable(filled: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setStroke(3, circleStrokeColor)
            setColor(if (filled) circleFilledColor else circleEmptyColor)
        }
    }

    fun setOnPinCompleteListener(listener: (String) -> Unit) {
        onPinComplete = listener
    }

    fun Int.dpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()
    fun Float.dpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

}
