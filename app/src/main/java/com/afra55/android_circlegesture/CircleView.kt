package com.afra55.android_circlegesture

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

interface CircleListener {
    fun progress(float: Float)
}

class CircleView : View {

    companion object {
        val CIRCLE_PROGRESS_MAX = 360F
        val CIRCLE_PROGRESS_MIN = 0F
    }

    private var textPaint: TextPaint? = null
    private var dashCirclePaint: Paint? = null
    private var circlePaint: Paint? = null
    private var circleWidth: Float = 1F
    private var pointWidth: Float = 20F
    private var circle_dash_color: Int = Color.RED
    private var circle_color: Int = Color.RED

    var listener: CircleListener? = null


    // 如果要修改 btnScale 不能超过这个值
    var maxBtnScale = 1.3F
    var btnScale = 1F

    // 0 - 360
    private var circleDrawProgress = 180F

    fun getProgress(): Float {
        return circleDrawProgress
    }


    private var cx: Float = 0F
    private var cy: Float = 0F
    private var btnCx: Float = 0F
    private var btnCy: Float = 0F
    private var radius: Float = 0F

    private var showPointCircle = true

    fun showPointCircle(showPoint: Boolean) {
        showPointCircle = showPoint
        postInvalidate()
    }

    var playDrawable: Drawable? = null

    var isDebug = true

    private lateinit var mDetector: GestureDetectorCompat

    private val gestureDetector = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            if (e != null) {
                if (isTouchPointBtn(e)) {

                    logGesture("onDown")
                }
                return true
            }
            return super.onDown(e)
        }

        private fun isTouchPointBtn(e: MotionEvent?): Boolean {
            if (e == null || !showPointCircle) {
                return false
            }
            val clickArea = dp2Px(pointWidth)
            if (clickArea > abs(btnCx - e.x)
                && clickArea > abs(btnCy - e.y)
            ) {
                return true
            }
            return false
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {


            if (showPointCircle && e1 != null && e2 != null && cx > 0 && cy > 0) {


                val dx = e2.x - cx
                val dy = e2.y - cy
                val absCX = abs(dx)
                val absCY = abs(dy)

                var toDegrees = circleDrawProgress
                if (dx > 0F) {
                    // 右边
                    if (dy == 0F) {
                        toDegrees = 90F
                    } else if (dy < 0) {
                        // 右上角
                        val angle = atan(absCX / absCY)
                        toDegrees = Math.toDegrees(angle.toDouble()).toFloat()
                    } else if (dy > 0) {
                        // 右下角
                        val angle = atan(absCX / absCY)
                        toDegrees = 180 - Math.toDegrees(angle.toDouble()).toFloat()
                    }
                } else if (dx < 0F) {
                    // 左边
                    if (dy == 0F) {
                        toDegrees = 270F
                    } else if (dy < 0) {
                        // 左上角
                        val angle = atan(absCX / absCY)
                        toDegrees = 360 - Math.toDegrees(angle.toDouble()).toFloat()
                    } else if (dy > 0) {
                        // 左下角
                        val angle = atan(absCX / absCY)
                        toDegrees = 180 + Math.toDegrees(angle.toDouble()).toFloat()
                    }
                }

                if (toDegrees != circleDrawProgress) {
                    circleDrawProgress = toDegrees
                    btnScale = 1.3F
                    postInvalidate()
                    return true
                }

            }

            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onShowPress(e: MotionEvent?) {
            logGesture("onShowPress: ${e?.x}_${e?.y}")
            super.onShowPress(e)
        }
    }

    private fun logGesture(message: String) {
        if (isDebug) {
            Log.i("TouchGesture", message)
        }
    }


    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CircleView, defStyle, 0
        )
        if (a.hasValue(R.styleable.CircleView_circleViewPlayIc)) {
            playDrawable = a.getDrawable(
                R.styleable.CircleView_circleViewPlayIc
            )
            playDrawable?.callback = this
        }
        circle_dash_color = a.getColor(
            R.styleable.CircleView_circleViewCircleDashColor,
            circle_dash_color
        )
        circle_color = a.getColor(
            R.styleable.CircleView_circleViewCircleColor,
            circle_color
        )
        a.recycle()


        // Set up a default TextPaint object
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }

        dashCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dashCirclePaint!!.color = circle_dash_color
        dashCirclePaint!!.style = Paint.Style.STROKE
        dashCirclePaint!!.strokeWidth = dp2Px(circleWidth)
        val dashWidth = dp2Px(2F)
        dashCirclePaint!!.pathEffect = DashPathEffect(floatArrayOf(dashWidth, dashWidth), 0F)

        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint!!.color = circle_color
        circlePaint!!.style = Paint.Style.STROKE
        circlePaint!!.strokeWidth = dp2Px(circleWidth)

        mDetector = GestureDetectorCompat(context, gestureDetector)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // allocations per draw cycle.
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom


        radius = (contentWidth - dp2Px(circleWidth) - dp2Px(pointWidth) * maxBtnScale) / 2

        cx = paddingLeft + (contentWidth.toFloat()) / 2
        cy = paddingTop + (contentHeight.toFloat()) / 2
        dashCirclePaint?.let {
            canvas.drawCircle(
                cx,
                cy,
                radius,
                it
            )
        }
        if (showPointCircle) {
            circlePaint?.let {
                canvas.drawArc(
                    cx - radius,
                    cy - radius,
                    cx + radius,
                    cy + radius,
                    -90F,
                    circleDrawProgress,
                    false,
                    it
                )
            }
            btnCx = cx
            btnCy = cy - radius
            // 用于计算的角度
            val realAngle = Math.toRadians(circleDrawProgress.toDouble())
            val mathSin = abs(sin(realAngle))
            val mathCos = abs(cos(realAngle))
            when {
                circleDrawProgress == 0F -> {

                }
                circleDrawProgress > 0 && circleDrawProgress < 90F -> {
                    btnCx = (cx + radius * mathSin).toFloat()
                    btnCy = (cy - radius * mathCos).toFloat()

                }
                circleDrawProgress == 90F -> {

                    btnCx = cx + radius
                    btnCy = cy
                }
                circleDrawProgress > 90F && circleDrawProgress < 180F -> {
                    btnCx = (cx + radius * mathSin).toFloat()
                    btnCy = (cy + radius * mathCos).toFloat()
                }
                circleDrawProgress == 180F -> {

                    btnCx = cx
                    btnCy = cy + radius
                }
                circleDrawProgress > 180F && circleDrawProgress < 270F -> {
                    btnCx = (cx - radius * mathSin).toFloat()
                    btnCy = (cy + radius * mathCos).toFloat()
                }
                circleDrawProgress == 270F -> {
                    btnCx = cx - radius
                    btnCy = cy
                }
                circleDrawProgress > 270F && circleDrawProgress < 360F -> {
                    btnCx = (cx - radius * mathSin).toFloat()
                    btnCy = (cy - radius * mathCos).toFloat()
                }
                circleDrawProgress == 360F -> {

                }
            }



            if (btnScale > maxBtnScale) {
                btnScale = maxBtnScale
            } else if (btnScale < 0) {
                btnScale = 1F
            }
            val btnSize = dp2Px(pointWidth) * btnScale
            playDrawable?.let {
                it.setBounds(
                    (btnCx - btnSize / 2).toInt(), (btnCy - btnSize / 2).toInt(),
                    (btnCx + btnSize / 2).toInt(), (btnCy + btnSize / 2).toInt()
                )
                it.draw(canvas)
            }
        }

        listener?.progress(circleDrawProgress)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (mDetector.onTouchEvent(event)) {
            return true
        } else if (event?.action == MotionEvent.ACTION_UP) {
            btnScale = 1F
            postInvalidate()
        }
        return super.onTouchEvent(event)
    }

    fun changeProgress(add: Float = 0.1F): Boolean {
        circleDrawProgress += add
        if (circleDrawProgress > CIRCLE_PROGRESS_MAX) {
            circleDrawProgress = CIRCLE_PROGRESS_MAX
            return false
        } else if (circleDrawProgress < CIRCLE_PROGRESS_MIN) {
            circleDrawProgress = CIRCLE_PROGRESS_MIN
            return false
        }
        return true
    }
}

fun Context.dp2Px(dip: Float): Float {
    return this.resources.dp2Px(dip)
}

fun Resources.dp2Px(dip: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, this.displayMetrics)
}

fun View.dp2Px(dip: Float): Float {
    return this.resources.dp2Px(dip)
}