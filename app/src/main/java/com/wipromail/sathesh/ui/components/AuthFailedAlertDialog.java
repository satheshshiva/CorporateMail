package com.wipromail.sathesh.ui.components;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;

public class AuthFailedAlertDialog  implements Constants{

	public static void showAlertdialog(Activity activity, Context context){

		final Context _context=context;
		final Activity _activity=activity;
		
		//build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_authfailed_title)
		.setMessage(activity.getText(R.string.dialog_authfailed_lbl))
		.setCancelable(false)
		.setPositiveButton(R.string.dialog_authfailed_btn_lbl, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				 ChangePasswordDialog.showAlertdialog(_activity, _context);
				

			}
		})
		.setNegativeButton(R.string.alertdialog_negative_lbl, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		})
		.create();
		AlertDialog authFailed = builder.create();
		authFailed.show();
	}
}
