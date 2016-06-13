package com.pl.sweepselect;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by penglu on 2016/5/22.
 */
public class SweepSelect extends View {
    //默认属性
    private static final int DEFAULT_BG_COLOR =0;
    private static final int DEFAULT_SELECTED_COLOR =-1;
    private static final int DEFAULT_NORMAL_COLOR =7500402;
    private static final int DEFAULT_TEXT_SIZE=30;
    private static final int DEFAULT_CORNER=4;

    //标示移动的方向，用于在连续左右滑动中改变是否选中
    private static final int DIRECTION_LEFT=1001;
    private static final int DIRECTION_RIGHT=1002;
    private static final int DIRECTION_NON=1003;

    //最小滑动距离，超过此距离才重新判断是否选中
    private static final float MIN_SCROLL_DISTANCE=20;

    //记录设置的属性变量
    private int backgroundColor=DEFAULT_BG_COLOR;
    private int corner=DEFAULT_CORNER;
    private CharSequence[] itemStrings;
    private int selectedColor=DEFAULT_SELECTED_COLOR;
    private int selectedSize=DEFAULT_TEXT_SIZE;
    private int normalColor=DEFAULT_NORMAL_COLOR;
    private int normalSize=DEFAULT_TEXT_SIZE;
    private boolean isMultyChooseMode=false;

    //绘制图像的中间变量
    private Paint backgroundPaint;
    private TextPaint textPaint;
    private Rect textRect;
    private RectF backgroundRect;
    private int currentDirection = DIRECTION_NON;
    private float lastX;

    private Item[] items;

    //选中结果回调函数
    private onSelectResultListener onSelectResultListener;

    public SweepSelect(Context context) {
        super(context);
        initView(context,null);
    }

    public SweepSelect(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context,attrs);
    }

    public SweepSelect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context,attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SweepSelect(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context,attrs);
    }

    private void initView(Context context,AttributeSet attrs){
        TypedArray typedArray=context.obtainStyledAttributes(attrs,R.styleable.SweepSelect);
        int length=typedArray.getIndexCount();
        for (int i=0;i<length;i++){
            int type = typedArray.getIndex(i);
            if (type == R.styleable.SweepSelect_backgroundColor) {
                backgroundColor=typedArray.getColor(i, DEFAULT_BG_COLOR);
            }else if (type==R.styleable.SweepSelect_itemString){
                itemStrings=typedArray.getTextArray(i);
            }else if (type==R.styleable.SweepSelect_selectedColor){
                selectedColor=typedArray.getColor(i, DEFAULT_SELECTED_COLOR);
            }else if (type==R.styleable.SweepSelect_normalColor){
                normalColor=typedArray.getColor(i, DEFAULT_NORMAL_COLOR);
            }else if (type==R.styleable.SweepSelect_selectedSize){
                selectedSize=typedArray.getDimensionPixelSize(i,DEFAULT_TEXT_SIZE);
            }else if (type==R.styleable.SweepSelect_normalSize){
                normalSize=typedArray.getDimensionPixelSize(i,DEFAULT_TEXT_SIZE);
            }else if (type==R.styleable.SweepSelect_corner){
                corner=typedArray.getDimensionPixelSize(i,DEFAULT_CORNER);
            }else if (type==R.styleable.SweepSelect_multyChooseMode){
                isMultyChooseMode=typedArray.getBoolean(i,false);
            }
        }
        typedArray.recycle();
        prepareDrawing();
    }

    private void prepareDrawing() {
        //根据当前设置的属性生成变量
        StringBuilder sb=new StringBuilder();
        if (itemStrings==null){
            itemStrings=new CharSequence[1];
            itemStrings[0]=new String("");
        }
        items=new Item[itemStrings.length];
        for (int i=0;i<itemStrings.length;i++){
            sb.append(itemStrings[i]);
            Item item=new Item();
            item.itemName=itemStrings[i].toString();
            item.isSeledted=false;
            item.hasChangedSinceActionDown=false;
            items[i]=item;
        }
        String text=sb.toString();
        backgroundPaint=new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setAntiAlias(true);
        textPaint=new TextPaint();
        textPaint.setTextSize(Math.max(selectedSize,normalSize));
        textPaint.setAntiAlias(true);

        textRect =new Rect();
        text = (String) TextUtils.ellipsize(text, textPaint, 1080, TextUtils.TruncateAt.END);
        textPaint.getTextBounds(text, 0, text.length(), textRect);

        textPaint.getTextBounds(text,0,text.length(), textRect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode==MeasureSpec.AT_MOST){
            int atMostHeight=MeasureSpec.getSize(heightMeasureSpec);
            int height= textRect.height()*3/2+corner;
            heightMeasureSpec=MeasureSpec.makeMeasureSpec(Math.min(height,atMostHeight),heightMode);
        }else if (heightMode==MeasureSpec.UNSPECIFIED){
            int height= textRect.height()*3/2+corner;
            heightMeasureSpec=MeasureSpec.makeMeasureSpec(height,heightMode);
        }else {

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //尺寸变化时，重新测量每个元素的位置
        backgroundRect=null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width=getWidth();
        int heigh=getHeight();
        if (backgroundRect==null) {
            backgroundRect = new RectF(0, 0, width, heigh);
            int count=items.length;
            int unitWidth=width/count;
            for (int i=0;i<count;i++){
                items[i].startPixel=unitWidth*i;
                items[i].endPixel=unitWidth*(i+1);
                items[i].height=heigh;
            }
        }
        drawBackground(canvas);
        drawText(canvas);
    }

    private void drawBackground(Canvas canvas){
        canvas.drawRoundRect(backgroundRect,corner,corner,backgroundPaint);
    }

    private void drawText(Canvas canvas){
        for (Item item:items){
            item.drawSelf(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x= event.getX();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                lastX=event.getX();
                currentDirection =DIRECTION_NON;
                checkSelect(event);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                //当滑动距离小于最低限度时，视为未滑动，防止出现抖动现象
                if (Math.abs(x-lastX)<MIN_SCROLL_DISTANCE){
                    return true;
                }
                if (x > lastX) {
                    currentDirection = DIRECTION_RIGHT;
                } else {
                    currentDirection = DIRECTION_LEFT;
                }
                checkSelect(event);
                lastX = event.getX();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                checkSelect(event);
                onSelectResult();
                //清理标记位
                lastX=-1;
                currentDirection =DIRECTION_NON;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void onSelectResult() {
        //统计选中情况，并调用回调函数
        boolean[] selections=new boolean[items.length];
        for (int i=0;i<items.length;i++){
            selections[i]=items[i].isSeledted;
            items[i].hasChangedSinceActionDown=false;
            items[i].lastDirection=DIRECTION_NON;
        }
        if (onSelectResultListener!=null){
            onSelectResultListener.select(selections);
        }
    }

    private void checkSelect(MotionEvent event){
        for (Item item:items){
            item.press(event.getX());
        }
    }

    private class Item {
        String itemName;
        float startPixel;
        float endPixel;
        float height;
        boolean isSeledted;
        boolean hasChangedSinceActionDown;
        int lastDirection;
        Rect textRect=new Rect();
        void drawSelf(Canvas canvas){
            if (isSeledted){
                textPaint.setColor(selectedColor);
                textPaint.setTextSize(selectedSize);
            }else {
                textPaint.setColor(normalColor);
                textPaint.setTextSize(normalSize);
            }
            textPaint.getTextBounds(itemName,0,itemName.length(),textRect);
            canvas.drawText(itemName,(startPixel+endPixel-textRect.width())/2,(height+textRect.height())/2,textPaint);
        }

        void press(float x){
            if (isMultyChooseMode) {
                //多选模式下，要根据左右滑动的方向变化来修改选择状态
                if (startPixel<=x&&endPixel>x){
                    if (!hasChangedSinceActionDown ||
                            (lastDirection != DIRECTION_NON && currentDirection != lastDirection)) {
                        isSeledted = !isSeledted;
                        hasChangedSinceActionDown = true;
                        lastDirection = currentDirection;
                    }
                    if (lastDirection == DIRECTION_NON && currentDirection != DIRECTION_NON) {
                        lastDirection = currentDirection;
                    }
                }
            }else {
                if (startPixel<=x&&endPixel>x){
                    isSeledted=true;
                }else {
                    isSeledted=false;
                }
            }
        }
    }


    public interface onSelectResultListener{
        /**
         * 选择结果的回调函数，通知回调方每个条目的选择情况
         * @param selections
         */
        void select(boolean[] selections);
    }

    /**
     * 设置选择结果的回调接口
     * @param onSelectResultListener
     */
    public void setOnSelectResultListener(SweepSelect.onSelectResultListener onSelectResultListener) {
        this.onSelectResultListener = onSelectResultListener;
    }

    /**
     * 设置当前各个条目的选中状态
     * @param selections 对应于每个条目的选择状态，数组长度等于条目数量
     */
    public void setCurrentSelection(boolean[] selections) {
        if (selections.length!=items.length){
            return;
        }
        for (int i=0;i<selections.length;i++){
            items[i].isSeledted=selections[i];
            if (!isMultyChooseMode&&selections[i]){
                break;
            }
        }
        onSelectResult();
        invalidate();
    }

    /**
     * 获取当前是否为多选模式
     * @return true为多选模式，false为单选模式
     */
    public boolean isMultyChooseMode() {
        return isMultyChooseMode;
    }

    /**
     * 设置多选或单选模式
     * @param multyChooseMode true为多选模式，false为单选模式
     */
    public void setMultyChooseMode(boolean multyChooseMode) {
        isMultyChooseMode = multyChooseMode;
    }

    /**
     * 获取文字在未选中状态下的字体大小，单位为像素
     * @return
     */
    public int getNormalSize() {
        return normalSize;
    }

    /**
     * 设置文字在未选中状态下的字体大小，单位为像素
     * @param normalSize
     */
    public void setNormalSize(int normalSize) {
        this.normalSize = normalSize;
        prepareDrawing();
        requestLayout();
    }

    /**
     * 获取文字在未选中状态下的字体颜色，argb表示
     * @return
     */
    public int getNormalColor() {
        return normalColor;
    }

    /**
     * 设置文字在未选中状态下的字体颜色，argb表示
     * @param normalColor
     */
    public void setNormalColor(int normalColor) {
        this.normalColor = normalColor;
        prepareDrawing();
        requestLayout();
    }

    /**
     * 获取文字在选中状态下的字体大小，单位为像素
     * @return
     */
    public int getSelectedSize() {
        return selectedSize;
    }

    /**
     * 设置文字在选中状态下的字体大小，单位为像素
     * @param selectedSize
     */
    public void setSelectedSize(int selectedSize) {
        this.selectedSize = selectedSize;
        prepareDrawing();
        requestLayout();
    }

    /**
     * 获取文字在选中状态下的字体颜色，argb表示
     * @return
     */
    public int getSelectedColor() {
        return selectedColor;
    }

    /**
     * 设置文字在选中状态下的字体颜色，argb表示
     * @param selectedColor
     */
    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
        prepareDrawing();
        requestLayout();
    }

    /**
     * 获取各待选项的文字表示
     * @return CharSequence[]
     */
    public CharSequence[] getItemStrings() {
        return itemStrings;
    }

    /**
     * 设置各待选项的文字表示
     * @param  itemStrings 每一个元素为一个带选项，各元素长度应该在3个字符以内，否则不好看
     */
    public void setItemStrings(CharSequence[] itemStrings) {
        this.itemStrings = itemStrings;
        prepareDrawing();
        requestLayout();
    }

    /**
     * 获取背景的圆角半径，单位为像素
     * @return
     */
    public int getCorner() {
        return corner;
    }

    /**
     * 设置背景的圆角半径，单位为像素
     * @param corner
     */
    public void setCorner(int corner) {
        this.corner = corner;
        prepareDrawing();
        requestLayout();
    }

    /**
     * 获取背景的颜色，argb表示
     * @return
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * 设置背景的颜色，argb表示
     * @param backgroundColor
     */
    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        prepareDrawing();
        requestLayout();
    }
}
