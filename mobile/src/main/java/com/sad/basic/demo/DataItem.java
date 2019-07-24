package com.sad.basic.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2019/2/11 0011.
 */

public class DataItem implements Serializable{
    String s="0";
    HashMap<String,ArrayList<String>> map=new HashMap<>();

    public HashMap<String, ArrayList<String>> getMap() {
        return map;
    }

    public void setMap(HashMap<String, ArrayList<String>> map) {
        this.map = map;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String get(){
        return s;
    }
}
