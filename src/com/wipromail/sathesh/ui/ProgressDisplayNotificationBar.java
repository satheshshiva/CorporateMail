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
public class ProgressDisplayNotificationBar implements Constants{

	private LinearLayout titleBar_Progress_linearLayout;
	private Activity activity;
	private TextView titlebar_viewmail_status;

	/** this class will display the status of downloading or resolving names
	 * @param activity
	 */
	public ProgressDisplayNotificationBar(Activity activity){
		this.activity=activity;
		titleBar_Progress_linearLayout=(LinearLayout)activity.findViewById(R.id.titleBar_Progress_linearLayout);
		titlebar_viewmail_status=(TextView)activity.findViewById(R.id.titlebar_viewmail_status);

	}


	/** shows the layout with anim
	 * 
	 */
	public void showStatusBar(){
		Log.d(TAG, "Show status bar clled");
		//show resolving names notification layout
		Animation animation = ApplyAnimation.getDownloadingImagesLblInAnim(activity);
		titleBar_Progress_linearLayout.setAnimation(animation);
		titleBar_Progress_linearLayout.setVisibility(View.VISIBLE);
	}

	/** hides the layout with anim
	 * 
	 */
	public void hideStatusBar(){
		Log.d(TAG, "hide status bar clled");
		Animation animation = ApplyAnimation.getDownloadingImagesLblOutAnim(activity);
		titleBar_Progress_linearLayout.setAnimation(animation);
		titleBar_Progress_linearLayout.setVisibility(View.GONE);
	}

	/** updates the text
	 * @param text
	 */
	public void setText(String text){
		titlebar_viewmail_status.setText(text);
	}
}
