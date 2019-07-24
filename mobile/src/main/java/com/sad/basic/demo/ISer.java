package com.sad.basic.demo;

import java.io.Serializable;

/**
 * Created by Administrator on 2019/2/11 0011.
 */

public interface ISer extends Serializable{
    default public Thread thead(){return null;};
}
