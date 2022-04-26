package com.sad.basic.demo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Administrator on 2019/2/11 0011.
 */

public class SubActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        /*Bundle bundle=getIntent().getExtras();
        DataBean bean= (DataBean) bundle.getSerializable("bean");
        TextView tv = (TextView) findViewById(R.id.sample_text);
        for (DataItem item:bean.getDataItems()
             ) {
            tv.append(item.get()+item.getMap().get("ss"));
        }
        tv.append(bean.thead().getName());*/
    }
}
