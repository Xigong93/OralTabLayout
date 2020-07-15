package pokercc.android.oraltablayout

import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager

private const val DEBUG = true

private fun log(message: String) {
    if (DEBUG) {
        Log.d("OralTabLayout", message)
    }
}

/**
 * 口语机经上面的切换tab,建议直接写死高度是100dp,否则会出现问题
 * Created by pokercc on 19-10-25.
 */
class OralTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    private val tabs = arrayOf("Part 1", "Part 2&3")
    private val selectedTextSize = 40f
    private val normalTextSize = 19f
    private var selectedIndex = 0
    private val selectedTabBgColor = Color.WHITE
    private val normalTabBgColor: Int = 0xffFBDE00.toInt()
    private val argbEvaluator = ArgbEvaluator()
    private val textColor = 0xff333333.toInt()
    private val textViews = ArrayList<TabTextView>()

    private val secondLargeMargin = 130f
    private val secondSmallMargin = 75f

    private val animDuration = 300L
    private val bottomLineHeight = 20.toDp()

    private val tabPaddingHorizontal = 25.toDp().toInt()
    private val tabPaddingTopBase = (tabPaddingHorizontal * 0.2).toInt()


    private val tabPaddingBottomBase = (tabPaddingHorizontal * 0.3).toInt()
    private val tabPaddingBottomS = tabPaddingBottomBase * 3
    private val tabPaddingBottomL = (tabPaddingBottomBase * 4.5f).toInt()

    private val pageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {


        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            if (selectedIndex != position) {
                selectedIndex = position
                onSelectedTabChangAnim()
            }
        }

    }

    init {
        tabs.mapIndexed { index, tab ->
            TabTextView(context).also { textView ->
                textView.tag = tab
                textView.text = tab
                textView.gravity = Gravity.CENTER
                textView.background = TabBgDrawable(index == 0, bottomLineHeight)
                textView.setTextColor(textColor)
                textView.setTypeface(null, Typeface.BOLD_ITALIC)
                textView.layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
                )


                textView.setOnClickListener {
                    log("第${index}个tab被点击了")
                    if (selectedIndex != index) {
                        selectedIndex = index
                        onSelectedTabChangAnim()
//                        onSelectedTabChange()
                        onTabSelectedListener?.onTabSelected(index)
                    }
                }
                textViews.add(textView)
            }
        }.forEach(this::addView)

        onSelectedTabChange()
    }

    private var animation: ValueAnimator? = null


    private var onTabSelectedListener: OnTabSelectedListener? = null
    private var bottomDrawable: BottomBarDrawable? = null
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bottomDrawable = BottomBarDrawable()
        bottomDrawable?.setBounds(0, 0, width, bottomLineHeight.toInt())

    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        bottomDrawable?.let {
            val save = canvas.save()
            canvas.translate(0f, (height - it.bounds.height()).toFloat())
            it.draw(canvas)
            canvas.restoreToCount(save)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST ||
            MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED
        ) {
            super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(
                    100.toDp().toInt(),
                    MeasureSpec.EXACTLY
                )

            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }


    }

    private fun onSelectedTabChangAnim() {
        animation?.cancel()

        animation = ValueAnimator.ofInt(0, 100)
            .also {
                it.interpolator = LinearInterpolator()
                it.duration = animDuration
                TabAnimListener().setAnim(it)
                it.start()
            }
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        viewPager.removeOnPageChangeListener(pageChangeListener)
        viewPager.addOnPageChangeListener(pageChangeListener)
        onTabSelectedListener = object : OnTabSelectedListener {
            override fun onTabSelected(position: Int) {
                viewPager.setCurrentItem(position, true)
            }

        }
    }

    private fun onSelectedTabChange() {

        textViews.forEachIndexed { index: Int, textView: TabTextView ->

            val selected = selectedIndex == index

            // 设置文字大小
            textView.setTextSize(
                TypedValue.COMPLEX_UNIT_DIP,
                if (selected) selectedTextSize else normalTextSize
            )
            // 设置背景
            textView.setBgColor(if (selected) selectedTabBgColor else normalTabBgColor)

            // 设置padding
            if (selected) {

                textView.setPadding(
                    tabPaddingHorizontal,
                    tabPaddingTopBase,
                    tabPaddingHorizontal,
                    tabPaddingBottomS
                )
            } else {
                textView.setPadding(
                    tabPaddingHorizontal,
                    tabPaddingTopBase,
                    tabPaddingHorizontal,
                    tabPaddingBottomL
                )
            }




            log("第${index}选中状态=$selected,textView:${textView.tag},padding:[${textView.paddingLeft},${textView.paddingTop},${textView.paddingRight},${textView.paddingBottom}]")


        }

        // 改变布局z轴
        // 这个影响了遍历顺序
        bringChildToFront(textViews[selectedIndex])


        // 改变外间距
        textViews.last()
            .setMarginStart(
                (if (selectedIndex == 0) secondLargeMargin else secondSmallMargin).toDp().toInt()
            )


    }

    inner class TabAnimListener : AnimatorListenerAdapter(), ValueAnimator.AnimatorUpdateListener {
        fun setAnim(animation: ValueAnimator) {
            animation.addUpdateListener(this)
            animation.addListener(this)
        }

        private var bringChildToFrontInvoked = false
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val animatedFraction = animation.animatedFraction

            // 改变布局z轴
            if (animatedFraction >= 0.5f && !bringChildToFrontInvoked) {
                bringChildToFront(textViews[selectedIndex])
                bringChildToFrontInvoked = true
            }

            // 改变文字大小
            val sTextSize =
                (selectedTextSize - normalTextSize) * animatedFraction + normalTextSize
            val nTextSize =
                selectedTextSize - (selectedTextSize - normalTextSize) * animatedFraction
            textViews[selectedIndex].setTextSize(TypedValue.COMPLEX_UNIT_DIP, sTextSize)
            textViews[1 - selectedIndex].setTextSize(TypedValue.COMPLEX_UNIT_DIP, nTextSize)


            // 改变背景颜色
            textViews[selectedIndex].setBgColor(
                argbEvaluator.evaluate(
                    animatedFraction,
                    normalTabBgColor,
                    selectedTabBgColor
                ) as Int
            )

            textViews[1 - selectedIndex].setBgColor(
                argbEvaluator.evaluate(
                    animatedFraction,
                    selectedTabBgColor,
                    normalTabBgColor
                ) as Int
            )

            // 改变内间距

            // 设置padding

            textViews[selectedIndex].setPadding(
                tabPaddingHorizontal,
                tabPaddingTopBase,
                tabPaddingHorizontal,
                (tabPaddingBottomL - (tabPaddingBottomL - tabPaddingBottomS) * animatedFraction).toInt()
            )
            textViews[1 - selectedIndex].setPadding(
                tabPaddingHorizontal,
                tabPaddingTopBase,
                tabPaddingHorizontal,
                (tabPaddingBottomS + (tabPaddingBottomL - tabPaddingBottomS) * animatedFraction).toInt()

            )

            // 改变外间距
            val secondMarginLeft = if (selectedIndex == 0) {
                secondSmallMargin + (secondLargeMargin - secondSmallMargin) * animatedFraction
            } else {
                secondLargeMargin - (secondLargeMargin - secondSmallMargin) * animatedFraction

            }.toDp().toInt()

            textViews.last().setMarginStart(secondMarginLeft)
        }


    }


    private fun Number.toDp(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), resources.displayMetrics)

    interface OnTabSelectedListener {
        fun onTabSelected(position: Int)
    }
}

@SuppressLint("ViewConstructor")
private class TabTextView(context: Context) : AppCompatTextView(context) {


    fun setBgColor(color: Int) = background
        ?.let { it as TabBgDrawable }
        ?.setColor(color)

    fun setMarginStart(marginStart: Int) {
        layoutParams = layoutParams
            ?.let { it as ViewGroup.MarginLayoutParams }
            ?.also {
                it.marginStart = marginStart
            }
    }


}


private class TabBgDrawable
/**
 * @param first 是不是第一个（第一个是不对称的)
 */
constructor(private val first: Boolean, @Px private val bottomLineHeight: Float) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.WHITE
        it.isDither = true
        it.isAntiAlias = true
    }

    private val path = Path()
    override fun draw(canvas: Canvas) {
        path.reset()
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        val firstPointX = width / 6


        // 第一个点，取长度的1/6
        if (first) {
            // 第一个不是对称的
            val radius = 25f
            path.moveTo(0f, height)

            path.lineTo(0f, radius)
            path.addArc(
                RectF(0f, 0f, 2 * radius, 2 * radius),
                180f, 90f
            )
            val previousX = width - firstPointX
            path.lineTo(previousX, 0f)
            val p2Y = height - bottomLineHeight
            path.cubicTo(
                previousX + firstPointX * 1f, 0f,
                previousX + firstPointX * 0.6f, p2Y,
                width, p2Y
            )
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()
        } else {
            // 第一个不是对称的
            val p2Y = height - bottomLineHeight

            path.moveTo(0f, p2Y)
            path.cubicTo(
                firstPointX * 0.4f, p2Y,
                firstPointX * 0f, 0f,
                firstPointX, 0f
            )
            val previousX = width - firstPointX
            path.lineTo(previousX, 0f)

            path.cubicTo(
                previousX + firstPointX * 1f, 0f,
                previousX + firstPointX * 0.6f, p2Y,
                width, p2Y

            )
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()

        }

        canvas.drawPath(path, paint)
    }

    fun setColor(color: Int) {
        if (paint.color != color) {
            paint.color = color
            invalidateSelf()
        }
    }

    override fun onStateChange(state: IntArray?): Boolean {
        return super.onStateChange(state)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        colorFilter?.let {
            paint.setColorFilter(colorFilter)
        }
    }
}

private class BottomBarDrawable : Drawable() {
    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.WHITE
        it.isAntiAlias = true

    }

    override fun draw(canvas: Canvas) {
        val height = bounds.height().toFloat()
        val width = bounds.width().toFloat()
        path.reset()
        path.moveTo(0f, height)
        path.addArc(
            RectF(0f, 0f, 2 * height, 2 * height),
            180f, 90f
        )
        path.lineTo(width, 0f)
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.close()
        canvas.drawPath(path, paint)

    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

}