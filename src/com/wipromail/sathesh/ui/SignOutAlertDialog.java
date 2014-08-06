package com.wipromail.sathesh.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.tools.SignOut;

public class SignOutAlertDialog  implements Constants{

	private static CheckBox resetSettings;
	public static void showAlertdialog(Activity activity, Context context){

		//build the dialog for sign out alert box using the xml layout
		LayoutInflater factory = LayoutInflater.from(context);
		final View textEntryView = factory.inflate(R.layout.dialog_signout_alert, null);

		final Context _context=context;
		final Activity _activity=activity;
		
		resetSettings = (CheckBox)textEntryView.findViewById(R.id.resetSettings);
		final SignOut _signOut = new SignOut();
		
		//build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_signout_alert_title)
		.setView(textEntryView)
		.setPositiveButton(R.string.alertdialog_positive_lbl, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				if(resetSettings!=null && resetSettings.isChecked()){
					//sign out and reset all settings
					try {
						_signOut.signOutAndResetAllSettings(_activity, _context);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e(TAG, "SignOut -> Error while Signing out " + e.getMessage());
						e.printStackTrace();
					}
				}
				else{
					//sign out and dont reset all settings
					try {
						_signOut.signOutAndRetainSettings(_activity, _context);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e(TAG, "SignOut -> Error while Signing out " + e.getMessage());
						e.printStackTrace();
					}
				}

			}
		})
		.setNegativeButton(R.string.alertdialog_negative_lbl, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		})
		.create();
		AlertDialog signOutAlert = builder.create();
		signOutAlert.show();
	}
}
