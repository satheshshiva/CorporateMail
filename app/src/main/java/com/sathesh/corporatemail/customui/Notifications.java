package com.sathesh.corporatemail.customui;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.sathesh.corporatemail.constants.Constants;

public class Notifications implements Constants {

	public static void showToast(Context context, CharSequence text, int duration){
		try {
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		} catch (Exception e) {
			Log.e(LOG_TAG, "Notifications -> Error occured while showing text: " + text);
			e.printStackTrace();
		}
	}

	public static void showToast(Context context, CharSequence text){
		showToast(context, text, Toast.LENGTH_SHORT);
	}

	public static void showSnackBarShort(View view, CharSequence text){
		Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
    	.show();
	}

	public static void showSnackBarLong(View view, CharSequence text){
		Snackbar.make(view, text, Snackbar.LENGTH_LONG)
				.show();
	}


	public static void showAlert(Context context, CharSequence text ){

		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(text)
			.setCancelable(true)
			.setPositiveButton(ERROR_ALERT_DISMISS_TEXT, (errorDialog, id) -> errorDialog.cancel());
			AlertDialog alert = builder.create();
			Log.i(LOG_TAG, "Notifications -> Showing alert: " + text);
			alert.show();
		} 
		catch(IllegalStateException ie){
			showToast(context, text, Toast.LENGTH_SHORT);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Notifications -> Error occured while showing alert box with message " + text);
			e.printStackTrace();
		}
	}

	public static void showInfo(Context context, CharSequence text ){

		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(text)
			.setCancelable(true)
			.setPositiveButton(INFO_ALERT_DISMISS_TEXT, (errorDialog, id) -> errorDialog.cancel());
			AlertDialog alert = builder.create();
			Log.i(LOG_TAG, "Notifications -> Showing info alert: " + text);
			alert.show();
		} 
		catch(IllegalStateException ie){
			showToast(context, text, Toast.LENGTH_SHORT);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Notifications -> Error occured while showing info box with message " + text);
			e.printStackTrace();
		}
	}

}
