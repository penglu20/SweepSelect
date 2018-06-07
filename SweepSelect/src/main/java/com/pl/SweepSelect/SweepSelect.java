package com.pl.sweepselect;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/** Created by penglu on 2016/5/22. */
public class SweepSelect extends View {
  public static final int MultySweep = 0; // isMultyChooseMode =true;
  public static final int SingleSweep = 1; // isMultyChooseMode =false; isSingleTapMode=false;
  public static final int SingleTap = 2; // isMultyChooseMode =false; isSingleTapMode=true;

  // 默认属性
  private static final int DEFAULT_SELECTED_COLOR = -1;
  private static final int DEFAULT_NORMAL_COLOR = 7500402;
  private static final int DEFAULT_TEXT_SIZE = 30;
  private static final int DEFAULT_NUMBER_EACH_LINE = 7;
  private static final int DEFAULT_EMPTY_PREFIX = 0;

  // 标示移动的方向，用于在连续左右滑动中改变是否选中
  private static final int DIRECTION_LEFT = 1001;
  private static final int DIRECTION_RIGHT = 1002;
  private static final int DIRECTION_NON = 1003;

  private static final int DIRECTION_UP = 1004;
  private static final int DIRECTION_DOWN = 1005;

  // 最小滑动距离，超过此距离才重新判断是否选中
  private static final float MIN_SCROLL_DISTANCE = 20;

  // 记录设置的属性变量
  private CharSequence[] itemStrings;
  private int selectedColor = DEFAULT_SELECTED_COLOR;
  private int selectedSize = DEFAULT_TEXT_SIZE;
  private int normalColor = DEFAULT_NORMAL_COLOR;
  private int normalSize = DEFAULT_TEXT_SIZE;
  private boolean isMultyChooseMode = false;
  private boolean isSingleTapMode = false;
  private boolean singleLine = true;
  private int numberEachLine = DEFAULT_NUMBER_EACH_LINE;
  private int spaceEachLine = -1;
  private int _spaceEachLine = -1;
  private int emptyPrefix = DEFAULT_EMPTY_PREFIX;

  // 绘制图像的中间变量
  private TextPaint textPaint;
  private Rect textRect;
  private int totalHeight;
  private boolean sizeHasChanged = true;
  private int currentXDirection = DIRECTION_NON;
  private int currentYDirection = DIRECTION_NON;
  private float lastX;
  private float lastY;

  private Item[] items;

  private int lines;

  // 选中结果回调函数
  private onSelectResultListener onSelectResultListener;
  private boolean hasTriggerLongClick;
  private boolean hasSweeped;

  public SweepSelect(Context context) {
    super(context);
    initView(context, null);
  }

  public SweepSelect(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  public SweepSelect(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public SweepSelect(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initView(context, attrs);
  }

  private void initView(Context context, AttributeSet attrs) {
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SweepSelect);
    int length = typedArray.getIndexCount();
    for (int i = 0; i < length; i++) {
      int type = typedArray.getIndex(i);
      if (type == R.styleable.SweepSelect_itemString) {
        itemStrings = typedArray.getTextArray(type);
      } else if (type == R.styleable.SweepSelect_selectedColor) {
        selectedColor = typedArray.getColor(type, DEFAULT_SELECTED_COLOR);
      } else if (type == R.styleable.SweepSelect_normalColor) {
        normalColor = typedArray.getColor(type, DEFAULT_NORMAL_COLOR);
      } else if (type == R.styleable.SweepSelect_selectedSize) {
        selectedSize = typedArray.getDimensionPixelSize(type, DEFAULT_TEXT_SIZE);
      } else if (type == R.styleable.SweepSelect_normalSize) {
        normalSize = typedArray.getDimensionPixelSize(type, DEFAULT_TEXT_SIZE);
      } else if (type == R.styleable.SweepSelect_chooseMode) {
        int mode = typedArray.getInt(type, MultySweep);
        if (mode == MultySweep) {
          isMultyChooseMode = true;
          isSingleTapMode = false;
        } else if (mode == SingleSweep) {
          isMultyChooseMode = false;
          isSingleTapMode = false;
        } else if (mode == SingleTap) {
          isMultyChooseMode = false;
          isSingleTapMode = true;
        }
      } else if (type == R.styleable.SweepSelect_singleLine) {
        singleLine = typedArray.getBoolean(type, false);
      } else if (type == R.styleable.SweepSelect_numberEachLine) {
        numberEachLine = typedArray.getInt(type, DEFAULT_NUMBER_EACH_LINE);
      } else if (type == R.styleable.SweepSelect_spaceEachLine) {
        spaceEachLine = typedArray.getDimensionPixelSize(type, -1);
      } else if (type == R.styleable.SweepSelect_emptyPrefix) {
        emptyPrefix = typedArray.getInt(type, DEFAULT_EMPTY_PREFIX);
      }
    }
    typedArray.recycle();
    prepareDrawing();
  }

  private void prepareDrawing() {
    // 根据当前设置的属性生成变量
    sizeHasChanged = true;
    StringBuilder sb = new StringBuilder();
    if (itemStrings == null) {
      itemStrings = new CharSequence[1];
      itemStrings[0] = new String("");
    }

    textPaint = new TextPaint();
    textPaint.setTextSize(Math.max(selectedSize, normalSize));
    textPaint.setAntiAlias(true);

    textRect = new Rect();

    if (singleLine) {
      lines = 1;
    } else {
      lines = (int) Math.ceil(((double) itemStrings.length + emptyPrefix) / numberEachLine);
    }

    //        items=new Item[itemStrings.length];
    items = new Item[lines * numberEachLine];
    for (int l = 0; l < lines; l++) {
      int start = 0;
      if (l == 0) {
        start = emptyPrefix;
        for (int i = 0; i < emptyPrefix; i++) {
          sb.append("");
          Item item = new Item();
          item.itemName = "";
          item.isSeledted = false;
          item.hasGetPressForcus = false;
          items[i] = item;
        }
      }
      for (int i = start; i < numberEachLine; i++) {
        if (l * numberEachLine - emptyPrefix + i < itemStrings.length) {
          sb.append(itemStrings[l * numberEachLine - emptyPrefix + i]);
          Item item = new Item();
          item.itemName = itemStrings[l * numberEachLine - emptyPrefix + i].toString();
          item.isSeledted = false;
          item.hasGetPressForcus = false;
          items[l * numberEachLine + i] = item;
        } else {
          sb.append("");
          Item item = new Item();
          item.itemName = "";
          item.isSeledted = false;
          item.hasGetPressForcus = false;
          items[l * numberEachLine + i] = item;
        }
      }
    }
    String text = sb.toString();
    text = (String) TextUtils.ellipsize(text, textPaint, 1080, TextUtils.TruncateAt.END);
    textPaint.getTextBounds(text, 0, text.length(), textRect);
    if (spaceEachLine == -1) {
      _spaceEachLine = textRect.height() / 2;
    } else {
      _spaceEachLine = spaceEachLine;
    }
    totalHeight = textRect.height() * (lines) + (lines + 1) * (_spaceEachLine);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int heightPadding = getPaddingTop() + getPaddingBottom();
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    if (heightMode == MeasureSpec.AT_MOST) {
      int atMostHeight = MeasureSpec.getSize(heightMeasureSpec);
      int height = totalHeight + heightPadding;
      heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(height, atMostHeight), heightMode);
    } else if (heightMode == MeasureSpec.UNSPECIFIED) {
      int height = totalHeight + heightPadding;
      heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode);
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    // 尺寸变化时，重新测量每个元素的位置
    sizeHasChanged = true;
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (getBackground() == null) {
      setBackgroundResource(R.drawable.dafault_background);
    }

    if (sizeHasChanged) {
      measureItem();
      sizeHasChanged = false;
    }
    drawText(canvas);
  }

  private void measureItem() {
    int width = getWidth();
    int heigh = getHeight();
    int paddingLeft = getPaddingLeft();
    int paddingRight = getPaddingRight();
    int paddingTop = getPaddingTop();
    int paddingBottom = getPaddingBottom();
    int count = numberEachLine;
    int unitWidth = (width - paddingLeft - paddingRight) / count;

    for (int l = 0; l < lines; l++) {
      for (int i = 0; i < numberEachLine; i++) {
        int pos = l * numberEachLine + i;
        items[pos].startXPixel = paddingLeft + unitWidth * i;
        items[pos].endXPixel = paddingLeft + unitWidth * (i + 1);
        items[pos].startYPixel = paddingTop + textRect.height() * l + (l + 1f / 2) * _spaceEachLine;
        items[pos].endYPixel = items[pos].startYPixel + textRect.height() + _spaceEachLine;
      }
    }
  }

  private void drawText(Canvas canvas) {
    for (Item item : items) {
      item.drawSelf(canvas);
    }
  }

  private Runnable longClickRunnable =
      new Runnable() {
        @Override
        public void run() {
          hasTriggerLongClick = true;
          Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
          vibrator.vibrate(50);
          if (onSelectResultListener != null) {
            int index = -1;
            boolean selection = false;
            for (int i = emptyPrefix; i < items.length; i++) {
              Item item = items[i];
              if (!TextUtils.isEmpty(item.itemName) && item.hasPressed(lastX, lastY)) {
                index = i - emptyPrefix;
                item.isSeledted = !item.isSeledted;
                selection = item.isSeledted;
              }
              item.press(lastX, lastY);
            }
            onSelectResultListener.onLongClicked(index, selection);
          }
        }
      };

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        hasTriggerLongClick = false;
        hasSweeped = false;
        if (!isSingleTapMode) {
          getParent().requestDisallowInterceptTouchEvent(true);
        }
        // 防止父View抢夺触摸焦点，导致触摸事件失效
        lastX = event.getX();
        lastY = event.getY();
        currentXDirection = DIRECTION_NON;
        currentYDirection = DIRECTION_NON;
        postDelayed(longClickRunnable, 500);
        if (!isSingleTapMode) {
          checkSelect(event);
        }
        invalidate();
        return true;
      case MotionEvent.ACTION_MOVE:
        // 当滑动距离小于最低限度时，视为未滑动，防止出现抖动现象
        if (Math.abs(x - lastX) < MIN_SCROLL_DISTANCE
            && Math.abs(y - lastY) < MIN_SCROLL_DISTANCE) {
          return true;
        }
        hasSweeped = true;
        removeCallbacks(longClickRunnable);
        if (x > lastX) {
          currentXDirection = DIRECTION_RIGHT;
        } else {
          currentXDirection = DIRECTION_LEFT;
        }
        if (y > lastY) {
          currentXDirection = DIRECTION_DOWN;
        } else {
          currentXDirection = DIRECTION_UP;
        }
        if (isSingleTapMode && hasSweeped) {
          invalidate();
          return false;
        }
        checkSelect(event);
        lastX = event.getX();
        lastY = event.getY();
        invalidate();
        return true;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        removeCallbacks(longClickRunnable);
        if (isSingleTapMode && hasSweeped) {
          invalidate();
          return false;
        }
        checkSelect(event);
        onSelectResult();
        // 清理标记位
        lastX = -1;
        lastY = -1;
        currentXDirection = DIRECTION_NON;
        currentYDirection = DIRECTION_NON;
        invalidate();
        return true;
    }
    return super.onTouchEvent(event);
  }

  private void onSelectResult() {
    // 统计选中情况，并调用回调函数
    if (hasTriggerLongClick) {
      return;
    }
    boolean[] selections = new boolean[itemStrings.length];
    for (int i = emptyPrefix; i < items.length; i++) {
      if (TextUtils.isEmpty(items[i].itemName)) {
        continue;
      }
      selections[i - emptyPrefix] = items[i].isSeledted;
      items[i].hasGetPressForcus = false;
      items[i].lastXDirection = DIRECTION_NON;
      items[i].lastYDirection = DIRECTION_NON;
    }
    if (onSelectResultListener != null) {
      onSelectResultListener.select(selections);
    }
  }

  private int lastSingleChooseId;
  private boolean hasSelected;

  private void checkSelect(MotionEvent event) {
    if (!isMultyChooseMode) {
      hasSelected = false;
      for (int i = 0; i < items.length; i++) {
        Item item = items[i];
        if (!TextUtils.isEmpty(item.itemName) && item.press(event.getX(), event.getY())) {
          lastSingleChooseId = i;
          hasSelected = true;
        }
      }
      if (!hasSelected) {
        items[lastSingleChooseId].isSeledted = true;
      }
      // 单选模式下，要对空白的地方进行排除
    } else {
      for (int i = 0; i < items.length; i++) {
        Item item = items[i];
        item.press(event.getX(), event.getY());
      }
    }
  }

  private class Item {
    String itemName;
    float startXPixel;
    float startYPixel;
    float endXPixel;
    float endYPixel;
    boolean isSeledted;
    boolean hasGetPressForcus;
    int lastXDirection;
    int lastYDirection;
    Rect textRect = new Rect();

    void drawSelf(Canvas canvas) {
      if (isSeledted) {
        textPaint.setColor(selectedColor);
        textPaint.setTextSize(selectedSize);
      } else {
        textPaint.setColor(normalColor);
        textPaint.setTextSize(normalSize);
      }
      textPaint.getTextBounds(itemName, 0, itemName.length(), textRect);
      canvas.drawText(
          itemName,
          startXPixel + (endXPixel - startXPixel - textRect.width()) / 2,
          startYPixel + (endYPixel - startYPixel + textRect.height()) / 2,
          textPaint);
    }

    boolean hasPressed(float x, float y) {
      if (startXPixel <= x && endXPixel > x && startYPixel <= y && endYPixel > y) {
        return true;
      } else {
        return false;
      }
    }

    boolean press(float x, float y) {
      if (isMultyChooseMode) {
        // 多选模式下，要根据左右滑动的方向变化来修改选择状态
        if (startXPixel <= x && endXPixel > x && startYPixel <= y && endYPixel > y) {
          //                    if (!hasGetPressForcus ||
          //                            (lastXDirection != DIRECTION_NON && currentXDirection !=
          // lastXDirection)||
          //                            (lastYDirection != DIRECTION_NON && currentYDirection !=
          // lastYDirection)) {
          if (!hasGetPressForcus) {
            isSeledted = !isSeledted;
            hasGetPressForcus = true;
            lastXDirection = currentXDirection;
            lastYDirection = currentYDirection;
          }
          //                    if ((lastXDirection == DIRECTION_NON && currentXDirection !=
          // DIRECTION_NON)||
          //                            (lastYDirection == DIRECTION_NON && currentYDirection !=
          // DIRECTION_NON)) {
          //                        lastXDirection = currentXDirection;
          //                        lastYDirection = currentYDirection;
          //                    }
        } else {
          hasGetPressForcus = false;
        }
      } else {
        if (startXPixel <= x && endXPixel > x && startYPixel <= y && endYPixel > y) {
          isSeledted = true;
        } else {
          isSeledted = false;
        }
      }
      return isSeledted;
    }
  }

  public interface onSelectResultListener {
    /**
     * 选择结果的回调函数，通知回调方每个条目的选择情况
     *
     * @param selections
     */
    void select(boolean[] selections);

    void onLongClicked(int index, boolean selection);
  }

  /**
   * 设置选择结果的回调接口
   *
   * @param onSelectResultListener
   */
  public void setOnSelectResultListener(SweepSelect.onSelectResultListener onSelectResultListener) {
    this.onSelectResultListener = onSelectResultListener;
  }

  /**
   * 设置当前各个条目的选中状态
   *
   * @param selections 对应于每个条目的选择状态，数组长度等于条目数量
   */
  public void setCurrentSelection(boolean[] selections) {
    if (selections.length != items.length) {
      return;
    }
    for (int i = 0; i < selections.length; i++) {
      items[i].isSeledted = false;
    }
    for (int i = 0; i < selections.length; i++) {
      items[i].isSeledted = selections[i];
      if (!isMultyChooseMode && selections[i]) {
        lastSingleChooseId = i;
        break;
      }
    }
    onSelectResult();
    invalidate();
  }

  /**
   * 获取当前是否为多选模式
   *
   * @return true为多选模式，false为单选模式
   */
  public boolean isMultyChooseMode() {
    return isMultyChooseMode;
  }

  /**
   * 设置多选或单选模式
   *
   * @param multyChooseMode true为多选模式，false为单选模式
   */
  public void setMultyChooseMode(boolean multyChooseMode) {
    isMultyChooseMode = multyChooseMode;
  }

  /**
   * 获取文字在未选中状态下的字体大小，单位为像素
   *
   * @return
   */
  public int getNormalSize() {
    return normalSize;
  }

  /**
   * 设置文字在未选中状态下的字体大小，单位为像素
   *
   * @param normalSize
   */
  public void setNormalSize(int normalSize) {
    this.normalSize = normalSize;
    prepareDrawing();
    requestLayout();
  }

  /**
   * 获取文字在未选中状态下的字体颜色，argb表示
   *
   * @return
   */
  public int getNormalColor() {
    return normalColor;
  }

  /**
   * 设置文字在未选中状态下的字体颜色，argb表示
   *
   * @param normalColor
   */
  public void setNormalColor(int normalColor) {
    this.normalColor = normalColor;
    prepareDrawing();
    requestLayout();
  }

  /**
   * 获取文字在选中状态下的字体大小，单位为像素
   *
   * @return
   */
  public int getSelectedSize() {
    return selectedSize;
  }

  /**
   * 设置文字在选中状态下的字体大小，单位为像素
   *
   * @param selectedSize
   */
  public void setSelectedSize(int selectedSize) {
    this.selectedSize = selectedSize;
    prepareDrawing();
    requestLayout();
  }

  /**
   * 获取文字在选中状态下的字体颜色，argb表示
   *
   * @return
   */
  public int getSelectedColor() {
    return selectedColor;
  }

  /**
   * 设置文字在选中状态下的字体颜色，argb表示
   *
   * @param selectedColor
   */
  public void setSelectedColor(int selectedColor) {
    this.selectedColor = selectedColor;
    prepareDrawing();
    requestLayout();
  }

  /**
   * 获取各待选项的文字表示
   *
   * @return CharSequence[]
   */
  public CharSequence[] getItemStrings() {
    return itemStrings;
  }

  /**
   * 设置各待选项的文字表示
   *
   * @param itemStrings 每一个元素为一个带选项，各元素长度应该在3个字符以内，否则不好看
   */
  public void setItemStrings(CharSequence[] itemStrings) {
    this.itemStrings = itemStrings;
    prepareDrawing();
    requestLayout();
  }

  /** 是否单行显示 */
  public boolean isSingleLine() {
    return singleLine;
  }
  /**
   * 设置是否单行显示
   *
   * @param singleLine true为单行显示
   */
  public void setSingleLine(boolean singleLine) {
    this.singleLine = singleLine;
    prepareDrawing();
    requestLayout();
  }

  /** 获取每行显示数量，默认是7 */
  public int getNumberEachLine() {
    return numberEachLine;
  }

  /**
   * 设置每行显示数量，默认是7
   *
   * @param numberEachLine
   */
  public void setNumberEachLine(int numberEachLine) {
    this.numberEachLine = numberEachLine;
    prepareDrawing();
    requestLayout();
  }

  /** 获取每行间的距离，默认是文字高度的一半 */
  public int getSpaceEachLine() {
    return spaceEachLine;
  }

  /**
   * 设置每行间的距离，默认是文字高度的一半
   *
   * @param spaceEachLine
   */
  public void setSpaceEachLine(int spaceEachLine) {
    this.spaceEachLine = spaceEachLine;
    prepareDrawing();
    requestLayout();
  }

  /** 获取第一行的第一行的空白前缀的数量 */
  public int getEmptyPrefix() {
    return emptyPrefix;
  }

  /**
   * 设置第一行的第一行的空白前缀的数量
   *
   * @param emptyPrefix
   */
  public void setEmptyPrefix(int emptyPrefix) {
    this.emptyPrefix = emptyPrefix;
    prepareDrawing();
    requestLayout();
  }
}
