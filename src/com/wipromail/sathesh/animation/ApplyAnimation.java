package com.wipromail.sathesh.animation;

import android.app.Activity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;

import com.wipromail.sathesh.R;

public class ApplyAnimation {


	public static void setTitleInboxStatusTextSwitcher(Activity activity,
			TextSwitcher titlebar_inbox_status_textswitcher) {

		Animation in = AnimationUtils.loadAnimation(activity,
				R.anim.push_up_in);
		Animation out = AnimationUtils.loadAnimation(activity,
				R.anim.push_up_out);
		titlebar_inbox_status_textswitcher.setInAnimation(in);
		titlebar_inbox_status_textswitcher.setOutAnimation(out);

	}

	public static void setMainActivitySignedInAnim(Activity activity) {

		activity.overridePendingTransition(R.anim.fade, R.anim.hold);

	}

	public static void setMainActivityNotSignedSignedInAnim(Activity activity) {

		activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);

	}

	public static void setViewMailAnim(Activity activity) {

		activity.overridePendingTransition(R.anim.slide_left,R.anim.slide_left);

	}

	//View Mail Activity
	public static Animation getDownloadingImagesLblInAnim(Activity activity){

		return  AnimationUtils.loadAnimation(activity, R.anim.push_up_in);
		
	}
	public static Animation getDownloadingImagesLblOutAnim(Activity activity){
		return  AnimationUtils.loadAnimation(activity, R.anim.push_up_out);
	}
}
