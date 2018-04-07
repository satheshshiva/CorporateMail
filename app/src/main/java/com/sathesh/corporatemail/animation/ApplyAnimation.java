package com.sathesh.corporatemail.animation;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.activity.MainActivity;
import com.sathesh.corporatemail.application.MyActivity;

public class ApplyAnimation {


	public static void setTitleInboxStatusTextSwitcher(MyActivity activity,
													   TextSwitcher titlebar_inbox_status_textswitcher) {

		Animation in = AnimationUtils.loadAnimation(activity,
				R.anim.push_up_in);
		Animation out = AnimationUtils.loadAnimation(activity,
				R.anim.push_up_out);
		titlebar_inbox_status_textswitcher.setInAnimation(in);
		titlebar_inbox_status_textswitcher.setOutAnimation(out);

	}


	//View Mail Activity
	public static Animation getDownloadingImagesLblInAnim(MyActivity activity){

		return  AnimationUtils.loadAnimation(activity, R.anim.slide_in_from_top);

	}
	public static Animation getDownloadingImagesLblOutAnim(MyActivity activity){
		return  AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_top);
	}

	public static Animation getLoginPageTextViewShakeAnim(MyActivity activity){
		return  AnimationUtils.loadAnimation(activity, R.anim.shake);
	}

	public static Animation getDrawerLayoutPage2InAnimation(MyActivity activity){
		return  AnimationUtils.loadAnimation(activity, R.anim.fade);
	}

	public static Animation getDrawerLayoutPage1InAnimation(MyActivity activity){
		return  AnimationUtils.loadAnimation(activity, R.anim.slide_right);
	}

	/*** Activity Transition Animations
	 * @param activity***/

	public static void setMainActivitySignedInAnim(MainActivity activity) {

		activity.overridePendingTransition(R.anim.fade, R.anim.hold);

	}

	public static void setMainActivityNotSignedSignedInAnim(MainActivity activity) {

		activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);

	}

	public static void setComposeActivityOpenAnim(MyActivity activity) {

		activity.overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.zoom_exit);

	}

	public static void setComposeActivityCloseAnim(MyActivity activity) {

		activity.overridePendingTransition(R.anim.zoom_enter, R.anim.slide_out_to_bottom);

	}

	public static void setViewMailAnim(MyActivity activity) {

		activity.overridePendingTransition(R.anim.slide_left,R.anim.slide_left);

	}

}
