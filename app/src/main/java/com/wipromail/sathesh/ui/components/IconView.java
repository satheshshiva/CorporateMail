package com.wipromail.sathesh.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/** Source: https://bitbucket.org/informatic0re/awesome-font-iconview/overview
 *
 *

*/


public class IconView extends TextView {

    public static final String FONT = "fontawesome-webfont.ttf";

    private static Typeface mFont;
    private String mIcon;
    private boolean mMeasureRatio;

    /**
     * Returns the Typeface from the given context with the given name typeface
     * @param context Context to get the assets from
     * @param typeface name of the ttf file
     * @return Typeface from the given context with the given name
     */
    public static Typeface getTypeface(Context context, String typeface) {
        if (mFont == null) {
            mFont = Typeface.createFromAsset(context.getAssets(), typeface);
        }
        return mFont;
    }

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //applyAttributes(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setTypeface(IconView.getTypeface(context, FONT));
        setText(mIcon);
    }

    public void setIcon(int iconResId) {
        setText(iconResId);
    }

    public void setIcon(String iconString) {
        setText(iconString);
    }

  /*  private void applyAttributes(Context context, AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IconView, 0, 0);
        try{
            mIcon = a.getString(R.styleable.IconView_iconn);
            mMeasureRatio = a.getBoolean(R.styleable.IconView_measureRatio, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mMeasureRatio){
            int widthSize = getMeasuredWidth();
            int heightSize = getMeasuredHeight();
            int size = Math.max(widthSize, heightSize);
            setMeasuredDimension(size, size);
        }
    }*/
}
