package com.sathesh.corporatemail.ui.components;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.animation.ApplyAnimation;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.constants.Constants;

/** Global class for displaying Progress Display Notification bar
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
	public ProgressDisplayNotificationBar(Activity activity, View view){
		this.activity=activity;
		this.titleBar_Progress_linearLayout=(LinearLayout)view.findViewById(R.id.titleBar_Progress_linearLayout);
		this.titlebar_viewmail_status=(TextView)view.findViewById(R.id.titlebar_viewmail_status);
	}

	/** this class will display the status of downloading or resolving names
	 * @param activity
	 */
	public ProgressDisplayNotificationBar(Activity activity){
		this.activity=activity;
		this.titleBar_Progress_linearLayout=(LinearLayout)activity.findViewById(R.id.titleBar_Progress_linearLayout);
		this.titlebar_viewmail_status=(TextView)activity.findViewById(R.id.titlebar_viewmail_status);
	}
	
	/** shows the layout with anim
	 * 
	 */
	public void showStatusBar(){
		if(BuildConfig.DEBUG){
		Log.d(LOG_TAG, "Show status bar clled");
		}
		//show resolving names notification layout
		Animation animation = ApplyAnimation.getDownloadingImagesLblInAnim((MyActivity) activity);
		titleBar_Progress_linearLayout.setAnimation(animation);
		titleBar_Progress_linearLayout.setVisibility(View.VISIBLE);
	}

	/** hides the layout with anim
	 * 
	 */
	public void hideStatusBar(){
		if(BuildConfig.DEBUG){
		Log.d(LOG_TAG, "hide status bar clled");
		}
		Animation animation = ApplyAnimation.getDownloadingImagesLblOutAnim((MyActivity) activity);
		titleBar_Progress_linearLayout.setAnimation(animation);
		titleBar_Progress_linearLayout.setVisibility(View.GONE);
	}

	/** updates the text
	 * @param text
	 */
	public void setText(String text){
		titlebar_viewmail_status.setText(text);
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProgressDisplayNotificationBar [titleBar_Progress_linearLayout=");
		builder.append(titleBar_Progress_linearLayout);
		builder.append(", activity=");
		builder.append(activity);
		builder.append(", titlebar_viewmail_status=");
		builder.append(titlebar_viewmail_status);
		builder.append("]");
		return builder.toString();
	}
}
