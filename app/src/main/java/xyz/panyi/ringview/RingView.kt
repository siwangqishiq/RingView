package xyz.panyi.ringview

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ring View
 * panyi
 *
 */
class RingView : View {//end class
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
        const val RING_COUNT = 10
        const val CONTENT_SCALE = 0.75f
        const val N = 3
        private val random = Random()

        fun rnd(start:Float , end:Float) = start + random.nextFloat() * (end - start)
    }

    data class RingData(
        var centerX:Float = 0.0f,
        var centerY:Float = 0.0f,
        var radius:Float = 0.1f,
        var A:Float = 0.0f,
        var paint:Paint = Paint(),
        var path: Path = Path(),
        var rotateAngle:Float = 0.0f,
        var rotateSpeed: Float = (Math.PI / 360.0f).toFloat()
    )//end inner class


    private var mState:Int = 0
    private var mTime:Float = 0.0f

    private val mCirclePaint : Paint = Paint()

    private val mRingDataList = ArrayList<RingData>(4)

    private fun initView(context: Context?){
        mCirclePaint.color = Color.WHITE
        mCirclePaint.strokeWidth = 32.0f;
        mCirclePaint.style = Paint.Style.STROKE
        mCirclePaint.maskFilter = BlurMaskFilter(mCirclePaint.strokeWidth , BlurMaskFilter.Blur.SOLID)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        resetRingsData()
    }

    private fun buildRingData(viewWidth : Int , viewHeight: Int) : RingData{
        return RingData().apply {
            centerX = viewWidth / 2.0f
            centerY = viewHeight / 2.0f

            radius =  (viewWidth  / 2.0f) * CONTENT_SCALE
            radius += rnd(-0.05f * radius ,0.05f* radius)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                this.paint.color = Color.argb(0.7f , 0.0f , 0.0f , 1.0f)
//            }
            this.paint.color = Color.BLUE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = mCirclePaint.strokeWidth / 2.0f
            paint.maskFilter = BlurMaskFilter(paint.strokeWidth , BlurMaskFilter.Blur.NORMAL)
            A = radius * rnd(0.1f, 0.2f)

            rotateAngle = rnd(0.0f, (Math.PI).toFloat())
            rotateSpeed = rnd((-Math.PI / 360.0f).toFloat(), (Math.PI / 360.0f).toFloat())
//            println("rotateAngel = $rotateAngle")
        }
    }

    private fun resetRingsData(){
        mTime = 0.0f

        val viewWidth = measuredWidth
        val viewHeight = measuredHeight

        mRingDataList.clear()
        for (i in 0 until RING_COUNT ){
            mRingDataList.add(buildRingData(viewWidth , viewHeight))
        }//end for i
    }

    override fun onDraw(canvas: Canvas?) {
        drawRingsView(canvas)
    }

    private fun drawRingsView(canvas:Canvas?){
        mTime += 0.02f
        for(item in mRingDataList){
            renderRing(canvas , item)
        }//end for each
        renderNormalCircle(canvas)

        invalidate()
    }

    private fun renderNormalCircle(canvas:Canvas?){
        val viewWidth = measuredWidth
        val viewHeight = measuredHeight

        val viewSize = if(viewWidth >= viewHeight) viewWidth else viewHeight

        val centerX = viewWidth / 2.0f
        val centerY = viewHeight / 2.0f;
        val normalRadius = (viewSize * CONTENT_SCALE) / 2.0f

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
                val x = (itemData.radius + currentA * cos(currentAngle * N).toFloat()) * (cos(currentAngle).toFloat())
                val y = (itemData.radius + currentA * sin(currentAngle * N).toFloat()) * (sin(currentAngle).toFloat())
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

