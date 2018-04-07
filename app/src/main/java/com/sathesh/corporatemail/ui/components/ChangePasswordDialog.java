package com.sathesh.corporatemail.ui.components;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.NotificationProcessing;
import com.sathesh.corporatemail.application.SharedPreferencesAdapter;
import com.sathesh.corporatemail.constants.Constants;

public class ChangePasswordDialog  implements Constants{

	private static EditText changePasswordEdit;

	public static void showAlertdialog(Activity activity, Context context){

		//build the dialog for change password using the xml layout
		LayoutInflater factory = LayoutInflater.from(context);
		final View textEntryView = factory.inflate(R.layout.dialog_change_password, null);

		final Context _context=context;

		//update the passowrd field with the old password
		changePasswordEdit = (EditText)textEntryView.findViewById(R.id.password_edit);
		try {
			changePasswordEdit.setText(SharedPreferencesAdapter.getSignedInPassword(context));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//build the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.dialog_changepwd_title)
		.setView(textEntryView)
		.setPositiveButton(R.string.alertdialog_save_lbl, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//validate the password and store the password
				String password="";

				if(changePasswordEdit!=null && changePasswordEdit.getText() !=null){
					
					MailApplication mailApplication = MailApplication.getInstance();
					/*will be true when the the password is wrong which is set by (NotificationProcessing.showLoginErrorNotification()). This will be set back to false when the user saves a 
					  new passoword in ChangePasswordDialog*/
					mailApplication.setWrongPwd(false);
					
				//clear all the notifications. may be the wrong password notification will be there.
					NotificationProcessing.cancelAllNotifications(_context);
					try {
						password= changePasswordEdit.getText().toString();
						SharedPreferencesAdapter.storeCredentialsPassword(_context, password);
						//starting MNS service so that it will start again by issuing a notify() if it was previously on wait() bcos of an auth issue
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e(TAG, "ChangePasswordDialog -> Error while storing password");
						e.printStackTrace();
					}
					
					MailApplication.startMNSService(_context);
				}

			}
		})
		.setNegativeButton(R.string.alertdialog_negative_lbl, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		})
		.create();
		AlertDialog changePassword = builder.create();
		changePassword.show();
	}
}
