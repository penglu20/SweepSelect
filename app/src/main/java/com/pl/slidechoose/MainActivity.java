package com.pl.slidechoose;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.pl.slideselect.SlideSelect;


public class MainActivity extends AppCompatActivity {
    private TextView result,result2;
    private SlideSelect select,select2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result= (TextView) findViewById(R.id.select_result);
        result2= (TextView) findViewById(R.id.select_result2);
        select= (SlideSelect) findViewById(R.id.select_week);
        select2= (SlideSelect) findViewById(R.id.select_week2);
        select.setOnSelectResultListener(new SlideSelect.onSelectResultListener() {
            @Override
            public void select(boolean[] selections) {
                CharSequence[] items=select.getItemStrings();
                StringBuilder resultText=new StringBuilder();
                for (int i=0;i<selections.length;i++){
                    if (selections[i]){
                        resultText.append(items[i]);
                        resultText.append(",");
                    }
                }
                if( resultText.length()>0){
                    resultText.deleteCharAt(resultText.length()-1);
                }
                result.setText(resultText);
            }
        });



        select2.setItemStrings(getResources().getStringArray(R.array.multyChooseArray));
        select2.setBackgroundColor(getResources().getColor(R.color.grey));
        select2.setNormalColor(getResources().getColor(R.color.white));
        select2.setNormalSize((int) (20*getResources().getDisplayMetrics().density));
        select2.setSelectedColor(getResources().getColor(R.color.yellow));
        select2.setSelectedSize((int) (25*getResources().getDisplayMetrics().density));
        select2.setMultyChooseMode(false);
        select2.setCorner((int) (10*getResources().getDisplayMetrics().density));
        select2.setOnSelectResultListener(new SlideSelect.onSelectResultListener() {
            @Override
            public void select(boolean[] selections) {
                CharSequence[] items=select.getItemStrings();
                StringBuilder resultText=new StringBuilder();
                for (int i=0;i<selections.length;i++){
                    if (selections[i]){
                        resultText.append(items[i]);
                        resultText.append(",");
                    }
                }
                if( resultText.length()>0){
                    resultText.deleteCharAt(resultText.length()-1);
                }
                result2.setText(resultText);
            }
        });

        select.setCurrentSelection(new boolean[]{false,false,false,true,true,true,false});
        select2.setCurrentSelection(new boolean[]{false,false,false,true,true,true,false});
    }
}
