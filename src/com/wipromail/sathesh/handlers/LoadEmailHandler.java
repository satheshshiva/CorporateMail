/**
 * 
 */
package com.wipromail.sathesh.handlers;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.fragment.ViewMailFragment;
import com.wipromail.sathesh.fragment.ViewMailFragment.Status;

/**
 * @author sathesh
 *
 */
public class LoadEmailHandler extends Handler implements Constants{

	/*** HANDLER ****/
	private ViewMailFragment parent;

	public LoadEmailHandler(ViewMailFragment viewMailFragment){
		this.parent=viewMailFragment;
	}


	@Override
	public void handleMessage(Message msg) {
		Status status = (ViewMailFragment.Status)msg.getData().getSerializable("state");
		String message = (String)msg.getData().getSerializable("message");
		handleStatusMessage(status,message);
	}

	/** Handles the status. 
	 * Called from the runnable
	 * Will be also called from the fragment on  config change(Screen rotation)
	 * 
	 */
	public void handleStatusMessage(Status status, String message) {
		// TODO Auto-generated method stub
		switch (status){
		case LOADING:
			//Jus started loading. network call is not made yet.
			parent.setCurrentStatus(Status.LOADING);
			parent.getWebview().loadUrl(LOADING_HTML_URL);
			if(BuildConfig.DEBUG){
				Log.d(TAG, "LoadEmailHandler-> Loading");
			}
			break;
		case SHOW_BODY:
			parent.setCurrentStatus(Status.SHOW_BODY);
			parent.displayEverything();
			break;

		case SHOW_IMG_LOADING_PROGRESSBAR:
			parent.setCurrentStatus(Status.SHOW_IMG_LOADING_PROGRESSBAR);
			parent.getProgressStatusDispBar().showStatusBar();
			updateProgressBarLabel(parent.getRemainingInlineImages(), parent.getTotalInlineImages());
			break;
		case DOWNLOADED_AN_IMAGE:
			//triggered when each 1 of the image got downloaded
			parent.setCurrentStatus(Status.DOWNLOADED_AN_IMAGE);
		//	parent.getProgressStatusDispBar().showStatusBar();
			//refresh the body (so that the newly downloaded image will be displayed
			parent.showBody(message);
			updateProgressBarLabel(parent.getRemainingInlineImages(), parent.getTotalInlineImages());
			break;
		case LOADED:
			//the mail has been displayed fully
			parent.setCurrentStatus(Status.LOADED);
			break;
		case ERROR:
			parent.setCurrentStatus(Status.ERROR);
			parent.getStandardWebView().loadData(parent.getWebview(), VIEW_MAIL_ERROR_HTML);
			break;
		}
	}


	/** shows a status bar showing the lable "Downloading remainingImgs of totalImgs images
	 * @param remainingImgs - remaining no. of images to load
	 * @param totalImgs - total no. of inline images in email
	 * @param loadStatusView - loads and shows the bar. Call it only for the first time we
	 */
	public void updateProgressBarLabel(int remainingImgs, int totalImgs) {
		// TODO Auto-generated method stub

		if(remainingImgs>0){
			// if only one remaining image to load then show a customized label
			if(remainingImgs == 1){
				parent.getProgressStatusDispBar().setText(parent.getContext().getString(R.string.viewmail_downloading_img,
						((totalImgs+1) - remainingImgs), totalImgs));
			}
			//if there are more no of remaining images to load then show lable " x of n"
			else{
				parent.getProgressStatusDispBar().setText(parent.getContext().getString(R.string.viewmail_downloading_imgs,
						((totalImgs+1) - remainingImgs), totalImgs));
			}
			//	Notifications.showToast(activity, getString(R.string.viewmail_downloading_img,no), Toast.LENGTH_SHORT);
		}
		else{
			parent.getProgressStatusDispBar().hideStatusBar();
		}
	}
}

