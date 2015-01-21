package com.wipromail.sathesh.ui.components;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.wipromail.sathesh.constants.Constants;

/** Source: https://bitbucket.org/informatic0re/awesome-font-iconview/overview
 *
 *

*/


public class IconView extends TextView implements Constants{

    public static final String FONT = "fontawesome-webfont_4.2.ttf";

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
    }

}
