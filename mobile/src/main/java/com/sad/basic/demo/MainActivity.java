package com.sad.basic.demo;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    //android01 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
    static {
        System.loadLibrary("native-lib");
    }

    static Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle bundle=msg.getData();
            DataBean dataBean= (DataBean) msg.getData().getSerializable("bean");
            //Log.e("---SAD_BASIC---","----------------Handler传值成功,json="+dataBean.getJsonObject());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testActivityNav();
            }
        });

    }

    public void testHandler(){
        Messenger messenger=new Messenger(handler);
        Message message=Message.obtain();
        message.setData(testBundle1());
        try {
            messenger.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testActivityNav(){
        Bundle bundle=testBundle1();
        Intent intent=new Intent();
        intent.setClass(MainActivity.this,SubActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public Bundle testBundle2(){
        Bundle bundle=new Bundle();
        ISer ser=new ISer() {
            @Override
            public Thread thead() {
                return null;
            }
        };
        bundle.putSerializable("ser",ser);
        return bundle;
    }

    public Bundle testBundle1(){
        Bundle bundle=new Bundle();
        DataBean bean=new DataBean();
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("a","1231546416");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //bean.setJsonObject(jsonObject);
        ArrayList<DataItem> list=new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DataItem item=new DataItem();
            item.setS(i+"");
            ArrayList<String> ss=new ArrayList<>();ss.add("ss");
            ArrayList<String> ss2=new ArrayList<>();ss2.add("bb");
            HashMap<String,ArrayList<String>> map=new HashMap<>();
            map.put("ss",ss);
            map.put("ss2",ss2);
            item.setMap(map);
            list.add(item);

        }
        bean.setDataItems(list);
        bean.setEle(new EleImpl());
        bundle.putSerializable("bean",bean);
        return bundle;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
