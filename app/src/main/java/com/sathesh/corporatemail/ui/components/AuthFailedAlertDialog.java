package com.sathesh.corporatemail.ui.components;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.constants.Constants;

public class AuthFailedAlertDialog  implements Constants{

	public static void showAlertdialog(Activity activity, Context context){

		//build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_authfailed_title)
		.setMessage(activity.getText(R.string.dialog_authfailed_lbl))
		.setCancelable(false)
		.setPositiveButton(R.string.dialog_authfailed_btn_lbl, (dialog, whichButton) -> ChangePasswordDialog.showAlertdialog(activity, context))
		.setNegativeButton(R.string.alertdialog_negative_lbl, (dialog, whichButton) -> dialog.cancel())
		.create();
		AlertDialog authFailed = builder.create();
		authFailed.show();
	}
}
