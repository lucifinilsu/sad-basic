package com.sad.basic.demo;

import android.os.Looper;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2019/2/11 0011.
 */

public class DataBean<E extends DataBean<E>> implements ISer,Serializable {

    private ArrayList<DataItem> dataItems=new ArrayList<>();
    private IEle ele;

    public void setEle(IEle ele) {
        this.ele = ele;
    }

    public IEle getEle() {
        return ele;
    }
    /* private JSONObject jsonObject=new JSONObject();

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }*/

    public ArrayList<DataItem> getDataItems() {
        return dataItems;
    }

    public void setDataItems(ArrayList<DataItem> dataItems) {
        this.dataItems = dataItems;
    }

    @Override
    public Thread thead() {
        return Thread.currentThread();
    }
}
