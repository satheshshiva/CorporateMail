package com.wipromail.sathesh.ui;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.animation.ApplyAnimation;
import com.wipromail.sathesh.constants.Constants;

/**
 * @author sathesh
 *
 */
public class WarningDisplayNotificationBar implements Constants{

	private LinearLayout titleBar_Warning_linearLayout;
	private Activity activity;
	private TextView titlebar_warning_msg;

	/** this class will display the status of downloading or resolving names
	 * @param activity
	 */
	public WarningDisplayNotificationBar(Activity activity){
		this.activity=activity;
		titleBar_Warning_linearLayout=(LinearLayout)activity.findViewById(R.id.titleBar_Warning_linearLayout);
		titlebar_warning_msg=(TextView)activity.findViewById(R.id.titlebar_warning_msg);

	}


	/** shows the layout with anim
	 * 
	 */
	public void showStatusBar(){
		Log.d(TAG, "Show status bar clled");
		//show resolving names notification layout
		Animation animation = ApplyAnimation.getDownloadingImagesLblInAnim(activity);
		titleBar_Warning_linearLayout.setAnimation(animation);
		titleBar_Warning_linearLayout.setVisibility(View.VISIBLE);
	}

	/** hides the layout with anim
	 * 
	 */
	public void hideStatusBar(){
		Log.d(TAG, "hide status bar clled");
		Animation animation = ApplyAnimation.getDownloadingImagesLblOutAnim(activity);
		titleBar_Warning_linearLayout.setAnimation(animation);
		titleBar_Warning_linearLayout.setVisibility(View.GONE);
	}

	/** updates the text
	 * @param text
	 */
	public void setText(String text){
		titlebar_warning_msg.setText(text);
	}
}
