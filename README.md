#A View with which you can change items' selection through sweeping.

##效果图
### 单选模式
![single select mode](https://raw.githubusercontent.com/l465659833/SweepSelect/master/art/single_select.gif)

### 多选模式
![multy select mode](https://raw.githubusercontent.com/l465659833/SweepSelect/master/art/multi_select.gif)


#How To Use 如何使用

add to your build.gradle files:

```
dependencies {
    compile 'com.pl:SweepSelect:0.2.2'
}
```

在layout中添加：
```
    <com.pl.sweepselect.SweepSelect
        android:id="@+id/select_week"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:itemString="@array/multyChooseArray"
        app:multyChooseMode="true"
        app:normalColor="#ffffff"
        app:normalSize="16sp"
        app:selectedColor="#f5c824"
        app:selectedSize="20sp" />
```

在values文件夹arrays文件中添加：

```
    <string-array name="multyChooseArray">
        <item>"周一"</item>
        <item>"周二"</item>
        <item>"周三"</item>
        <item>"周四"</item>
        <item>"周五"</item>
        <item>"周六"</item>
        <item>"周日"</item>
    </string-array>
```

## Attributes

| attr 属性          | description 描述 |
|:---				 |:---|
| itemString  	     | 各待选项的文本的数组 |
| selectedColor	 	 | 被选中文本颜色 |
| normalColor 		 | 未选中文本颜色 |
| selectedSize 			 |  被选中文本字体大小 |
| normalSize 	 |  未选中文本字体大小 |
| multyChooseMode | 是否为多选模式，true为多选模式，false为单选模式 |
| singleLine | 是否为单行，true为单行模式，false为多行模式 |
| numberEachLine | 多行模式下，每一行的带选项数量 |
| spaceEachLine |  每一行之间的间距（上一行文字底部到下一行文字顶部间距） |
| emptyPrefix | 第一行的空白前缀的数量 |

## Method
```java
    /**
     * 设置选择结果的回调接口
     * @param onSelectResultListener
     */
    public void setOnSelectResultListener(SweepSelect.onSelectResultListener onSelectResultListener);

    /**
     * 设置当前各个条目的选中状态
     * @param selections 对应于每个条目的选择状态，数组长度等于条目数量
     */
    public void setCurrentSelection(boolean[] selections);

    /**
     * 获取当前是否为多选模式
     * @return true为多选模式，false为单选模式
     */
    public boolean isMultyChooseMode();

    /**
     * 设置多选或单选模式
     * @param multyChooseMode true为多选模式，false为单选模式
     */
    public void setMultyChooseMode(boolean multyChooseMode);

    /**
     * 获取文字在未选中状态下的字体大小，单位为像素
     * @return
     */
    public int getNormalSize();

    /**
     * 设置文字在未选中状态下的字体大小，单位为像素
     * @param normalSize
     */
    public void setNormalSize(int normalSize);

    /**
     * 获取文字在未选中状态下的字体颜色，argb表示
     * @return
     */
    public int getNormalColor();

    /**
     * 设置文字在未选中状态下的字体颜色，argb表示
     * @param normalColor
     */
    public void setNormalColor(int normalColor);

    /**
     * 获取文字在选中状态下的字体大小，单位为像素
     * @return
     */
    public int getSelectedSize();

    /**
     * 设置文字在选中状态下的字体大小，单位为像素
     * @param selectedSize
     */
    public void setSelectedSize(int selectedSize);

    /**
     * 获取文字在选中状态下的字体颜色，argb表示
     * @return
     */
    public int getSelectedColor();

    /**
     * 设置文字在选中状态下的字体颜色，argb表示
     * @param selectedColor
     */
    public void setSelectedColor(int selectedColor);

    /**
     * 获取各待选项的文字表示
     * @return CharSequence[]
     */
    public CharSequence[] getItemStrings() ;

    /**
     * 设置各待选项的文字表示
     * @param  itemStrings 每一个元素为一个带选项，各元素长度应该在3个字符以内，否则不好看
     */
    public void setItemStrings(CharSequence[] itemStrings);

     /**
     * 是否单行显示
     */
    public boolean isSingleLine();
    /**
     * 设置是否单行显示
     * @param singleLine true为单行显示
     */
    public void setSingleLine(boolean singleLine) ;

    /**
     * 获取每行显示数量，默认是7
     */
    public int getNumberEachLine() ;

    /**
     * 设置每行显示数量，默认是7
     * @param numberEachLine
     */
    public void setNumberEachLine(int numberEachLine);

    /**
     * 获取每行间的距离，默认是文字高度的一半
     */
    public int getSpaceEachLine();

    /**
     * 设置每行间的距离，默认是文字高度的一半
     * @param spaceEachLine
     */
    public void setSpaceEachLine(int spaceEachLine);

    /**
     * 获取第一行的第一行的空白前缀的数量
     */
    public int getEmptyPrefix();

    /**
     * 设置第一行的第一行的空白前缀的数量
     * @param emptyPrefix
     */
    public void setEmptyPrefix(int emptyPrefix);

```