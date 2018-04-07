package com.sathesh.corporatemail.application;

import com.sathesh.corporatemail.constants.Constants;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class Utils implements Constants{

	private PowerManager pm;
	
	/** check internet availability
	 * @param context eg:activity.getContext()
	 * @return boolean
	 */
	public static boolean checkInternetConnection(Context context){

		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}

	}

}
