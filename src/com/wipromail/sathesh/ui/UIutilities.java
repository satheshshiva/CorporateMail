package com.wipromail.sathesh.ui;

import com.wipromail.sathesh.constants.Constants;

import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class UIutilities implements Constants{

	public static void hideKeyBoard(Context context, EditText editText){
		try {
			InputMethodManager imm = (InputMethodManager)context.getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "UIutilities -> Error while closing keyboard ");
			e.printStackTrace();
		}
	}
}
