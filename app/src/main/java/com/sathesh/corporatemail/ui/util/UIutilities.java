package com.sathesh.corporatemail.ui.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sathesh.corporatemail.constants.Constants;

public class UIutilities implements Constants{

	/** Hides keyboard
	 *
	 * @param context
	 * @param editText
	 */
	public static void hideKeyBoard(Context context, EditText editText){
		try {
			InputMethodManager imm = (InputMethodManager)context.getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		} catch (Exception e) {
			Log.e(LOG_TAG, "UIutilities -> Error while closing keyboard ");
			e.printStackTrace();
		}
	}

	/** shows keyboard
	 *
	 * @param context
	 * @param view
	 */
	public static void showKeyBoard(Context context, View view){
		try {
			if (view.requestFocus()) {
				InputMethodManager imm = (InputMethodManager)
						context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "UIutilities -> Error while opening keyboard ");
			e.printStackTrace();
		}
	}

	public static int convertDpToPx(Context context, int dp){
		Resources r = context.getResources();
		return (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				dp,
				r.getDisplayMetrics());
	}
}
