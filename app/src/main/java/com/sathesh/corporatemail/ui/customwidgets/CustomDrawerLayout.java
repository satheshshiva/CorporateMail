package com.sathesh.corporatemail.ui.customwidgets;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;

/**
 * Created by Sathesh on 3/22/15.
 *
 * This class is the Custom View for the drawer layout. It is created to fix a Fragment+Drawerlayout bug in android
 * It was giving the exception " java.lang.IllegalArgumentException: DrawerLayout must be measured with MeasureSpec.EXACTLY."
 *
 * http://stackoverflow.com/questions/16599690/navigation-drawer-rendering-error-in-adt-layout-editor
 *
 */
public class CustomDrawerLayout extends DrawerLayout {

    public CustomDrawerLayout(Context context) {
        super(context);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}