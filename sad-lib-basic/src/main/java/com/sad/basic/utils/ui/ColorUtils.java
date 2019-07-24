package com.sad.basic.utils.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

/**
 * Created by Administrator on 2018/11/19 0019.
 */

public class ColorUtils {
    //drawable 着色
    public static void setImageViewColor(ImageView view, int colorResId) {
        //mutate()
        Drawable modeDrawable = view.getDrawable().mutate();
        Drawable temp = DrawableCompat.wrap(modeDrawable);
        ColorStateList colorStateList =     ColorStateList.valueOf(view.getResources().getColor(colorResId));
        DrawableCompat.setTintList(temp, colorStateList);
        view.setImageDrawable(temp);
    }
    //drawable 着色
    public static Drawable setDrawableColor(Context context, Drawable drawable, int colorResId) {
        Drawable temp = DrawableCompat.wrap(drawable);
        ColorStateList colorStateList = ColorStateList.valueOf(context.getResources().getColor(colorResId));
        DrawableCompat.setTintList(temp, colorStateList);
        return temp;
    }
    //drawable 着色
    public static Drawable setDrawableColor(Context context, @DrawableRes int drawableRes, int colorResId) {
        Drawable drawable=context.getResources().getDrawable(drawableRes);
        Drawable temp = DrawableCompat.wrap(drawable);
        ColorStateList colorStateList = ColorStateList.valueOf(context.getResources().getColor(colorResId));
        DrawableCompat.setTintList(temp, colorStateList);
        return temp;
    }
}
