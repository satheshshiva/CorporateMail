package com.sathesh.corporatemail.ui.components;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

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

	private ConstraintLayout titleBar_Progress_linearLayout;
	private Activity activity;
	private TextView titlebar_viewmail_status;
	private ProgressBar progressBar;

	/** this class will display the status of downloading or resolving names
	 * @param activity
	 */
	public ProgressDisplayNotificationBar(Activity activity, View view){
		this.activity=activity;
		this.titleBar_Progress_linearLayout=(ConstraintLayout)view.findViewById(R.id.titleBar_Progress_layout);
		this.titlebar_viewmail_status=(TextView)view.findViewById(R.id.titlebar_viewmail_status);
		this.titleBar_Progress_linearLayout.setVisibility(View.INVISIBLE);
		this.progressBar = (ProgressBar) view.findViewById(R.id.titleBar_Progress_img);

	}

	/** this class will display the status of downloading or resolving names
	 * @param activity
	 */
	public ProgressDisplayNotificationBar(Activity activity){
		this.activity=activity;
		this.titleBar_Progress_linearLayout=(ConstraintLayout)activity.findViewById(R.id.titleBar_Progress_layout);
		this.titlebar_viewmail_status=(TextView)activity.findViewById(R.id.titlebar_viewmail_status);
		this.titleBar_Progress_linearLayout.setVisibility(View.INVISIBLE);
		this.progressBar = (ProgressBar) activity.findViewById(R.id.titleBar_Progress_img);
	}
	
	/** shows the layout with anim
	 * 
	 */
	public void showStatusBar(){
		this.progressBar.setVisibility(View.VISIBLE);
		//show resolving names notification layout
		Animation animation = ApplyAnimation.getDownloadingImagesLblInAnim((MyActivity) activity);
		this.titleBar_Progress_linearLayout.setAnimation(animation);
		this.titleBar_Progress_linearLayout.setVisibility(View.VISIBLE);
	}

	/** hides the layout with anim
	 * 
	 */
	public void hideStatusBar(){
		Animation animation = ApplyAnimation.getDownloadingImagesLblOutAnim((MyActivity) activity);
		this.titleBar_Progress_linearLayout.setAnimation(animation);
		this.titleBar_Progress_linearLayout.setVisibility(View.INVISIBLE);
	}

	public void hideProgressBar(){
		this.progressBar.setVisibility(View.INVISIBLE);
	}
	/** updates the text
	 * @param text
	 */
	public void setText(String text){
		this.titlebar_viewmail_status.setText(text);
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
