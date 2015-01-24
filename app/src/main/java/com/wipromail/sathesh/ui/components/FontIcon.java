package com.wipromail.sathesh.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Spannable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.util.Utilities;

import java.util.Hashtable;

/** This has implementation of IconView and ButtonView in innerClass
 * It will show the font icons when given in text
 * reference: https://bitbucket.org/informatic0re/awesome-font-iconview/overview
 * FontAwesome: http://fortawesome.github.io/Font-Awesome/cheatsheet/
 * IcoMoon: https://icomoon.io/#preview-free

 */


public class FontIcon implements Constants{


    private static Typeface mFont;
    private static int fontVariant;

    public static final String FONT_AWESOME = "fontawesome-webfont_4.2.ttf";
    public static final String FONT_ICO_MOON = "IcoMoon-Free.ttf";

    //mapping for custom attributes present in attrs.xml
    private interface FontVariant{
        public int FONT_AWESOME=0;
        public int FONT_ICO_MOON =1;
    }

    private static Hashtable<String,Typeface> typeFacesCache  = new Hashtable<>();


    /** Called by the constructor to initialize the custom attributes from the attrs.xml
     *
     * @param context
     * @param attrs
     */
    private static void initializeAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray=null;
        try {
            typedArray = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.FontIcon,
                    0, 0);

            fontVariant = typedArray.getInteger(R.styleable.FontIcon_fontVariant, 0);

        } finally {
            typedArray.recycle();
        }
    }

    /**
     * Called by the constrcutor to return the type face to apply
     * @param context Context to get the assets from
     * @return Typeface from the given context with the given name
     */
    private synchronized static Typeface getMyTypeFace(Context context) {
        try {
            String path=null;

            //select the path to ttf based on layout attribute
            if (fontVariant == FontVariant.FONT_AWESOME) {
                //call the super method to set the type face
                path = FONT_AWESOME;
            } else if (fontVariant == FontVariant.FONT_ICO_MOON) {
                //call the super method to set the type face
                path= FONT_ICO_MOON;
            }

            //using a typeFaces hashtable as a cache for TypeFaces. It prevents creation of multiple TypeFace objects for the
            // same icon if called multiple times

            if(!typeFacesCache.contains(path)) {
                mFont = Typeface.createFromAsset(context.getAssets(), path);
                typeFacesCache.put(path, mFont);
            }
            else{
                mFont = typeFacesCache.get(path);
            }

        } catch (Exception e) {
            Utilities.generalCatchBlock(e, "FontIcon -> getMyTypeFace");
        }
        return mFont;
    }

    public static class IconView extends TextView{
        public IconView(Context context, AttributeSet attrs) {
            super(context, attrs);
           initializeAttributes(context, attrs);
            setTypeface(getMyTypeFace(context));

        }
    }

    public static class ButtonView extends Button {
        CharSequence str="";
        Typeface typeface;
        Spannable span;

        public ButtonView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initializeAttributes(context, attrs);
            setTypeface(getMyTypeFace(context));

        }

        @Override
        public void setText(CharSequence text, BufferType type) {
            super.setText(text, type);

        }

    }
}
