package com.sathesh.corporatemail.ui.util;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.sathesh.corporatemail.activity.TncActivity;
import com.sathesh.corporatemail.application.MyActivity;
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

    public static void setPrivacyPolicyTextView(MyActivity activity, TextView tncTextView) {

		tncTextView.setMovementMethod(LinkMovementMethod.getInstance());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			tncTextView.setTransitionName(TransitionSharedElementNames.privacyPolicy);
		}
		SpannableString ss = new SpannableString("View our privacy policy");
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View textView) {
				Intent intent = new Intent(activity, TncActivity.class);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					ActivityOptions options = ActivityOptions
							.makeSceneTransitionAnimation(activity, tncTextView, TransitionSharedElementNames.privacyPolicy);
					activity.startActivity(intent, options.toBundle());
					return;
				}
				activity.startActivity(intent);
			}
		};
		ss.setSpan(clickableSpan, 9, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tncTextView.setText(ss);
    }
}
