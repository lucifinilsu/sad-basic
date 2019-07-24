package com.sad.basic.utils.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Created by Administrator on 2018/11/28 0028.
 */

public class DimensionUtils {
    /**
     * 获取TextView内容宽度
     * @param tv
     * @param isGetEveryCh
     * @return
     */
    public static float getTextViewFontWidth(TextView tv, boolean isGetEveryCh){
        String text= tv.getText().toString();
        float width = 0;
        Paint paint = new Paint();
        paint.setTextSize(tv.getTextSize());
        float text_width = paint.measureText(text);//得到总体长度
        if (isGetEveryCh) {
            width = text_width/text.length();//每一个字符的长度其中paint有很多属性可以设置，会影响长度
            return width;
        }
        return text_width;//返回整个长度

    }

    public static boolean TextViewIsOverlayScreen(Context context, TextView tv, float space){
        float tw=getTextViewFontWidth(tv, false);
        float sw=ScreenUtils.getScreenWidth(context)-space;
        return tw>sw;
    }


    public static float getScreenDensity(Context context){
        return context.getResources().getDisplayMetrics().density;
    }
    public static int dip2px(Context context, float dpValue)
    {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    public static int sp2px(Context context, float spValue){
        float scale = context.getResources().getDisplayMetrics().density;
        System.out.println(">>>>>>>>>>>>>>>>>>屏幕密度："+scale);
        return (int) (spValue * scale + 0.5f);
    }
    public static float sp2px_fix(Context context, float spValue){
        float scale = context.getResources().getDisplayMetrics().density;
        System.out.println(">>>>>>>>>>>>>>>>>>屏幕密度："+scale);
        return getRawSize(context, TypedValue.COMPLEX_UNIT_SP, spValue)/scale;
    }
    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,context.getResources().getDisplayMetrics());
    }
    public static int getDimenPix(Context context, int res){
        return context.getResources().getDimensionPixelSize(res);
    }
    /**
     * 获取当前分辨率下指定单位对应的像素大小（根据设备信息）
     * px,dip,sp转换 px
     *
     * Paint.setTextSize()单位为px
     *
     * 代码摘自：TextView.setTextSize()
     *
     * @param unit  TypedValue.COMPLEX_UNIT_xxx
     * @param size
     */
    public static float getRawSize(Context c, int unit, float size) {
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();

        return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
    }
    public static int px2dip(Context context, float pxValue)
    {
        if (context!=null){
            float mDensity = context.getResources().getDisplayMetrics().density;
            if(Math.abs(mDensity-0)<0.0001){
                mDensity = context.getResources().getDisplayMetrics().density;
            }
            return (int)(pxValue/mDensity + 0.5f);

        }
        return 0;
    }
}
