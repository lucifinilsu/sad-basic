package com.sad.basic.demo;

import java.io.Serializable;

/**
 * Created by Administrator on 2019/3/14 0014.
 */

public class EleImpl implements IEle,Serializable {
    @Override
    public String get() {
        return "sss";
    }
}
