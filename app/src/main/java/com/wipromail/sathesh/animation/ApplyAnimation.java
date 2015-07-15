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


	//View Mail Activity
	public static Animation getDownloadingImagesLblInAnim(Activity activity){

		return  AnimationUtils.loadAnimation(activity, R.anim.slide_in_from_top);

	}
	public static Animation getDownloadingImagesLblOutAnim(Activity activity){
		return  AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_top);
	}

	public static Animation getLoginPageTextViewShakeAnim(Activity activity){
		return  AnimationUtils.loadAnimation(activity, R.anim.shake);
	}

	public static Animation getDrawerLayoutPage2InAnimation(Activity activity){
		return  AnimationUtils.loadAnimation(activity, R.anim.slide_left);
	}

	public static Animation getDrawerLayoutPage1InAnimation(Activity activity){
		return  AnimationUtils.loadAnimation(activity, R.anim.slide_right);
	}

	/*** Activity Transition Animations ***/

	public static void setMainActivitySignedInAnim(Activity activity) {

		activity.overridePendingTransition(R.anim.fade, R.anim.hold);

	}

	public static void setMainActivityNotSignedSignedInAnim(Activity activity) {

		activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);

	}

	public static void setComposeActivityOpenAnim(Activity activity) {

		activity.overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.zoom_exit);

	}

	public static void setComposeActivityCloseAnim(Activity activity) {

		activity.overridePendingTransition(R.anim.zoom_enter, R.anim.slide_out_to_bottom);

	}

	public static void setViewMailAnim(Activity activity) {

		activity.overridePendingTransition(R.anim.slide_left,R.anim.slide_left);

	}

}
