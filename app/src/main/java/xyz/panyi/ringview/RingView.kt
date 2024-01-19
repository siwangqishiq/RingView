package xyz.panyi.ringview

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import java.util.Random
import java.util.logging.LogManager
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Ring View
 * panyi
 *
 */
class RingView : View {
    constructor(context: Context?) : super(context){
        initView(context)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        initView(context)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        initView(context)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes){
        initView(context)
    }

    companion object{
        const val STATE_RING = 0
        const val STATE_ROUND_RECT = 1
        const val STATE_SHOW_LOGO = 2
        const val STATE_RECT_ROUND = 3

        const val STOKEN_WIDTH = 30.0f

        const val RING_COUNT = 16
        val UPDATE_SPEED = 0.04f
        const val CONTENT_SCALE = 0.75f
        const val N = 3

        const val SHAPE_ONE = 1
        const val SHAPE_TWO = 2


        private val random = Random()
        fun rnd(start:Float , end:Float) = start + random.nextFloat() * (end - start)

        fun rnd(start:Int , end:Int):Int {
            val bound = Math.max(end -start , 1)
            return start + random.nextInt(bound) * (end - start)
        }
    }

    data class RingData(
        var centerX:Float = 0.0f,
        var centerY:Float = 0.0f,
        var radius:Float = 0.1f,
        var A:Float = 0.0f,
        var paint:Paint = Paint(),
        var path: Path = Path(),
        var rotateAngle:Float = 0.0f,
        var rotateSpeed: Float = (Math.PI / 360.0f).toFloat(),
        var shapeType:Int = SHAPE_ONE
    )//end inner class


    private var mState:Int = STATE_RING
    private var mTime:Float = 0.0f
    private var mDeltaTime = 0.0f

    private val mCirclePaint : Paint = Paint()

    private var mRingColor:Int = Color.argb(1.0f , 1.0f , 1.0f , 1.0f)

    private var mCycleTimes:Int = 0

    private val mRingDataList = ArrayList<RingData>(4)

    private val mRoundRectPaint : Paint = Paint()

    private var mRoundRectRadius = 0.0f
    private var mRoundRectMinRadius = 0.0f
    private var mRoundRectMaxRadius = 0.0f
    private val mRoundRect : RectF = RectF()

    private fun initView(context: Context?){
        initMainCircle()
        initRoundRect()
    }

    private fun initMainCircle(){
        mCirclePaint.apply {
            color = mRingColor
            strokeWidth = STOKEN_WIDTH
            style = Paint.Style.STROKE
            maskFilter = BlurMaskFilter(mCirclePaint.strokeWidth , BlurMaskFilter.Blur.SOLID)
        }
    }

    private fun initRoundRect(){
        mRoundRectPaint.apply {
            color = mRingColor
            strokeWidth = STOKEN_WIDTH
            style = Paint.Style.STROKE
            maskFilter = BlurMaskFilter(mCirclePaint.strokeWidth , BlurMaskFilter.Blur.SOLID)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        resetRingsData()
        resetRoundRectShapeData()
    }

    private fun resetRoundRectShapeData(){
        val rectWidth =  measuredWidth * CONTENT_SCALE
        val rectHeight = measuredHeight * CONTENT_SCALE

        val left = measuredWidth  / 2.0f - rectWidth / 2.0f
        val top = measuredHeight / 2.0f - rectHeight / 2.0f
        mRoundRect.set(left , top , left + rectWidth  , top + rectHeight)

        mRoundRectRadius = rectWidth / 2.0f;
        mRoundRectMaxRadius = mRoundRectRadius
        mRoundRectMinRadius = mRoundRectRadius / 2.0f;
    }

    private fun buildRingData(viewWidth : Int, viewHeight: Int , startAngleRad : Float) : RingData{
        return RingData().apply {
            centerX = viewWidth / 2.0f
            centerY = viewHeight / 2.0f

            shapeType = if(rnd(0.0f , 1.0f) > 0.5) SHAPE_ONE else SHAPE_TWO
            Log.i("shapeType" , "shapeType = $shapeType")

            radius =  (viewWidth  / 2.0f) * CONTENT_SCALE
//            radius += rnd(-0.2f * radius ,0.05f* radius)
//            paint.color = mRingColor
//            paint.color = Color.argb(rnd(0.5f , 1.0f) , rnd(0.0f , 1.0f) , rnd(0.0f , 1.0f)  , rnd(0.0f , 1.0f))
            val intenseValue = rnd(0.6f , 1.0f)
            paint.color = Color.argb(1.0f ,
                intenseValue * (mRingColor.red / 255.0f) ,
                intenseValue * (mRingColor.green / 255.0f) ,
                intenseValue * (mRingColor.blue / 255.0f) );

            paint.style = Paint.Style.STROKE
//            paint.strokeWidth = rnd(mCirclePaint.strokeWidth / 8.0f , mCirclePaint.strokeWidth / 2.0f)
            paint.strokeWidth = mCirclePaint.strokeWidth / 4.0f
             paint.maskFilter = BlurMaskFilter(paint.strokeWidth , BlurMaskFilter.Blur.NORMAL)
            A = radius * rnd(0.05f, 0.2f)

//            rotateAngle = rnd(0.0f, (Math.PI).toFloat())
            rotateAngle = startAngleRad
//            rotateSpeed = rnd((-Math.PI / 180.0f).toFloat(), (Math.PI / 180.0f).toFloat())
            rotateSpeed = rnd(0.0f, (Math.PI / 90.0f).toFloat())
//            rotateSpeed =  (Math.PI / 90.0f).toFloat()
//            println("rotateAngel = $rotateAngle")
        }
    }

    private fun resetRingsData(){
        mTime = 0.0f

        val viewWidth = measuredWidth
        val viewHeight = measuredHeight

        mRingDataList.clear()
        for (i in 0 until RING_COUNT ){
            mRingDataList.add(buildRingData(viewWidth , viewHeight , i * (Math.PI.toFloat()/180)*4.0f ))
        }//end for i
    }

    override fun onDraw(canvas: Canvas?) {
        mTime += UPDATE_SPEED

        when(mState){
            STATE_RING ->{
                drawRingsView(canvas)
            }
            STATE_ROUND_RECT -> {
                drawCircleToRect(canvas)
            }
            STATE_SHOW_LOGO -> {
                drawLogo(canvas)
            }
            STATE_RECT_ROUND -> {
                drawRectToCircle(canvas)
            }
        }//end when
    }

    private fun drawCircleToRect(canvas:Canvas?){
        val A = (mRoundRectMaxRadius - mRoundRectMinRadius )
        val radius =A * cos(mTime / 2.0f) + mRoundRectMinRadius
        canvas?.drawRoundRect(mRoundRect , radius, radius, mRoundRectPaint)
        invalidate()

        if(mTime >= Math.PI){
            mState = STATE_SHOW_LOGO
            mTime = 0.0f
        }
    }

    private fun drawLogo(canvas:Canvas?){
        canvas?.drawRoundRect(mRoundRect , mRoundRectMinRadius, mRoundRectMinRadius, mRoundRectPaint)
        invalidate()

        if(mTime >= Math.PI){
            mState = STATE_RECT_ROUND
            mTime = 0.0f
        }
    }

    private fun drawRectToCircle(canvas:Canvas?){
        val A = (mRoundRectMaxRadius - mRoundRectMinRadius )
        val radius =A * sin(mTime / 2.0f) + mRoundRectMinRadius
        canvas?.drawRoundRect(mRoundRect , radius, radius, mRoundRectPaint)
        invalidate()

        if(mTime >= Math.PI){
            mState = STATE_RING
            mTime = 0.0f
        }
    }

    private fun drawRingsView(canvas:Canvas?){
        for(item in mRingDataList){
            renderRing(canvas , item)
        }//end for each
        renderNormalCircle(canvas)
        invalidate()

        if(mTime >= 2 * Math.PI){
            mCycleTimes++
            mTime = 0.0f
            onOneCycleEnd()
        }
    }

    /**
     *
     * 周期结束
     */
    protected fun onOneCycleEnd(){
//        resetRingsData()
//        if(mCycleTimes == 1){
//            mState = STATE_ROUND_RECT
//        }

        mState = STATE_ROUND_RECT
    }

    private fun renderNormalCircle(canvas:Canvas?){
        val viewWidth = measuredWidth
        val viewHeight = measuredHeight

        val viewSize = if(viewWidth >= viewHeight) viewWidth else viewHeight

        val centerX = viewWidth / 2.0f
        val centerY = viewHeight / 2.0f;
        val normalRadius = (viewSize * CONTENT_SCALE) / 2.0f
//        mCirclePaint.alpha = (255.0f * (0.5f) * sin(mTime.toDouble()) + 1.0f).toInt()
        canvas?.drawCircle(centerX , centerY , normalRadius , mCirclePaint)
    }

    /**
     *
     */
    private fun renderRing(canvas:Canvas? , itemData : RingData){
        val step = 64 // later opt 根据view实际大小动态调整
        canvas?.save()
        canvas?.translate(itemData.centerX , itemData.centerY)
        canvas?.rotate(Math.toDegrees(itemData.rotateAngle.toDouble()).toFloat())
        itemData.rotateAngle += itemData.rotateSpeed

        itemData.path.apply {
            val deltaAngle = (2 * Math.PI) / step
            val currentA =  itemData.A * sin(mTime)
            reset()
            for(i  in 0 until  step){
                val currentAngle = deltaAngle * i
                var x:Float
                var y:Float
                if(itemData.shapeType == SHAPE_ONE){
                    x = (itemData.radius + currentA * cos(currentAngle * N).toFloat()) * (cos(currentAngle).toFloat())
                    y = (itemData.radius + currentA * cos(currentAngle * N).toFloat()) * (sin(currentAngle).toFloat())
                }else{
                    x = (itemData.radius + currentA * sin(currentAngle * N).toFloat()) * (cos(currentAngle).toFloat())
                    y = (itemData.radius + currentA * cos(currentAngle * N).toFloat()) * (sin(currentAngle).toFloat())
                }

                if(i == 0){
                    moveTo(x , y)
                }else{
                    lineTo(x , y)
                }
            }//end for i
            close()
        }
        canvas?.drawPath(itemData.path , itemData.paint)
        canvas?.restore()
    }

}//end class

