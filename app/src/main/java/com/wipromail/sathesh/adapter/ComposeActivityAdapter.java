package com.wipromail.sathesh.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.wipromail.sathesh.activity.ComposeActivity;
import com.wipromail.sathesh.constants.Constants;

public class ComposeActivityAdapter implements Constants{

	/**
	 * @param context 
	 * @param type - specifies the type of compose
	 * @param toBundle	- ContactSerializable objects stored in the bundle. This will be To: contacts..
	 * @param ccBundle	- ContactSerializable objects stored in the bundle. This will be CC: contacts..
	 * @param bccBundle	- ContactSerializable objects stored in the bundle. This will be BCC: contacts..
	 * @param subject - String.. Subject
	 * @param titleBar - Titlebar string
	 * @param setFocusOnBody - Boolean.. on true will focus on the body on load instead of Subject
	 */
	public static void startPrefilledCompose(Context context,int type, Bundle toBundle, Bundle ccBundle, Bundle bccBundle, String subject, String titleBar, boolean setFocusOnBody){
		//the prefill data for the compose activity before starting the intent
		Intent intent = new Intent(context,ComposeActivity.class);
		intent.putExtra(ComposeActivity.PREFILL_DATA_EXTRA, true);
		intent.putExtra(ComposeActivity.PREFILL_DATA_TYPE_EXTRA, type);
		if(null != toBundle && !(toBundle.isEmpty())){
			intent.putExtra(ComposeActivity.PREFILL_DATA_TO_EXTRA, toBundle);
		}
		if(null != ccBundle && !(ccBundle.isEmpty())){
			
			intent.putExtra(ComposeActivity.PREFILL_DATA_CC_EXTRA, ccBundle);
		}
		if(null != bccBundle && !(bccBundle.isEmpty())){
			intent.putExtra(ComposeActivity.PREFILL_DATA_BCC_EXTRA, bccBundle);
		}

		intent.putExtra(ComposeActivity.PREFILL_DATA_SUBJECT_EXTRA, subject);
		intent.putExtra(ComposeActivity.PREFILL_DATA_TITLEBAR_EXTRA, titleBar);
		intent.putExtra(ComposeActivity.PREFILL_DATA_SETFOCUS_ON_BODY_EXTRA, setFocusOnBody);

		//open the Compose activity to send email to developer
		context.startActivity(intent);
	}
	
	
	
	/**
	 * @param context 
	 * @param type - specifies the type of compose
	 * @param toBundle	- ContactSerializable objects stored in the bundle. This will be To: contacts..
	 * @param ccBundle	- ContactSerializable objects stored in the bundle. This will be CC: contacts..
	 * @param bccBundle	- ContactSerializable objects stored in the bundle. This will be BCC: contacts..
	 * @param subject - String.. Subject
	 * @param titleBar - Titlebar string
	 * @param setFocusOnBody - Boolean.. on true will focus on the body on load instead of Subject
	 * @param quoteHtml - quoted text in reply
	 */
	public static void startReply(Context context, int type,String itemId,Bundle toBundle,  Bundle ccBundle, Bundle bccBundle, String subject, String titleBar, boolean setFocusOnBody, String quoteHtml){
		//the prefill data for the compose activity before starting the intent
		Intent intent = new Intent(context,ComposeActivity.class);
		intent.putExtra(ComposeActivity.PREFILL_DATA_EXTRA, true);
		intent.putExtra(ComposeActivity.PREFILL_DATA_TYPE_EXTRA, type);
		intent.putExtra(ComposeActivity.PREFILL_DATA_REPL_ITEMID, itemId);
		
		if(null != toBundle && !(toBundle.isEmpty())){
			intent.putExtra(ComposeActivity.PREFILL_DATA_TO_EXTRA, toBundle);
		}
		if(null != ccBundle && !(ccBundle.isEmpty())){
			intent.putExtra(ComposeActivity.PREFILL_DATA_CC_EXTRA, ccBundle);
		}
		if(null != bccBundle && !(bccBundle.isEmpty())){
			intent.putExtra(ComposeActivity.PREFILL_DATA_BCC_EXTRA, bccBundle);
		}

		intent.putExtra(ComposeActivity.PREFILL_DATA_SUBJECT_EXTRA, subject);
		intent.putExtra(ComposeActivity.PREFILL_DATA_TITLEBAR_EXTRA, titleBar);
		intent.putExtra(ComposeActivity.PREFILL_DATA_SETFOCUS_ON_BODY_EXTRA, setFocusOnBody);
		Log.d(TAG, "quoteHTMl in composeactivity adapter " + quoteHtml);
		intent.putExtra(ComposeActivity.PREFILL_DATA_QUOTE_HTML, quoteHtml);
		
		//open the Compose activity to send email to developer
		context.startActivity(intent);
	}
	
	/**
	 * @param context 
	 * @param type - specifies the type of compose
	 * @param toBundle	- ContactSerializable objects stored in the bundle. This will be To: contacts..
	 * @param ccBundle	- ContactSerializable objects stored in the bundle. This will be CC: contacts..
	 * @param bccBundle	- ContactSerializable objects stored in the bundle. This will be BCC: contacts..
	 * @param subject - String.. Subject
	 * @param titleBar - Titlebar string
	 * @param setFocusOnBody - Boolean.. on true will focus on the body on load instead of Subject
	 * @param quoteHtml - quoted text in reply
	 */
	public static void startForward(Context context, int type,String itemId,Bundle toBundle,  Bundle ccBundle, Bundle bccBundle, String subject, String titleBar, boolean setFocusOnBody, String quoteHtml){
		//the prefill data for the compose activity before starting the intent
		Intent intent = new Intent(context,ComposeActivity.class);
		intent.putExtra(ComposeActivity.PREFILL_DATA_EXTRA, true);
		intent.putExtra(ComposeActivity.PREFILL_DATA_TYPE_EXTRA, type);
		intent.putExtra(ComposeActivity.PREFILL_DATA_REPL_ITEMID, itemId);
		
		if(null != toBundle && !(toBundle.isEmpty())){
			intent.putExtra(ComposeActivity.PREFILL_DATA_TO_EXTRA, toBundle);
		}
		if(null != ccBundle && !(ccBundle.isEmpty())){
			intent.putExtra(ComposeActivity.PREFILL_DATA_CC_EXTRA, ccBundle);
		}
		if(null != bccBundle && !(bccBundle.isEmpty())){
			intent.putExtra(ComposeActivity.PREFILL_DATA_BCC_EXTRA, bccBundle);
		}

		intent.putExtra(ComposeActivity.PREFILL_DATA_SUBJECT_EXTRA, subject);
		intent.putExtra(ComposeActivity.PREFILL_DATA_TITLEBAR_EXTRA, titleBar);
		intent.putExtra(ComposeActivity.PREFILL_DATA_SETFOCUS_ON_BODY_EXTRA, setFocusOnBody);
		intent.putExtra(ComposeActivity.PREFILL_DATA_QUOTE_HTML, quoteHtml);
		
		//open the Compose activity to send email to developer
		context.startActivity(intent);
	}
}
