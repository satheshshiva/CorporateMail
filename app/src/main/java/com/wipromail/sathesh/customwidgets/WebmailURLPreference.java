package com.wipromail.sathesh.customwidgets;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.activity.PreferencesActivity;
import com.wipromail.sathesh.adapter.GeneralPreferenceAdapter;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.constants.Constants;

public class WebmailURLPreference extends ListPreference implements Constants{

	private int mClickedDialogEntryIndex;

	public WebmailURLPreference(Context context) {
		super(context);
	}

	public WebmailURLPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    private GeneralPreferenceAdapter sharedPref = new GeneralPreferenceAdapter();

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {

		//   super.onPrepareDialogBuilder(builder);
        String value="";
		if (getEntries() == null || getEntryValues() == null) {
			throw new IllegalStateException(
					"ListPreference requires an entries array and an entryValues array.");
		}

        //get the stored preference value
        value=sharedPref.getServerURL(this.getContext());

        //get the index of the stored value
		mClickedDialogEntryIndex = findIndexOfValue(value);

        //build the single choice items dialog
		builder.setSingleChoiceItems(getEntries(), mClickedDialogEntryIndex, 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mClickedDialogEntryIndex = which;

				/*
				 * Clicking on an item simulates the positive button
				 * click, and dismisses the dialog.
				 */
				WebmailURLPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
				dialog.dismiss();
			}
		});

		/*
		 * The typical interaction for list-based dialogs is to have
		 * click-on-an-item dismiss the dialog instead of the user having to
		 * press 'Ok'.
		 */
		builder.setPositiveButton(null, null);
	}

	/* I have overridden this so that clicking the 3rd option in webmail server url drop down will open the custom dialog
	 * 
	 * (non-Javadoc)
	 * @see android.preference.ListPreference#onDialogClosed(boolean)
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// super.onDialogClosed(positiveResult);
        if(BuildConfig.DEBUG) {
            Log.i(TAG, "WebmailURLPreference -> Called Ondialog Closed");
        }
		if (positiveResult && mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
			String value = getEntryValues()[mClickedDialogEntryIndex].toString();
			if (callChangeListener(value)) {
				if(mClickedDialogEntryIndex!=(PreferencesActivity.CUSTOM_URL_POSITION)){
					setValue(value);
				}

				else{
					//the Custom URL option is clicked.. open the Custom Url Dialog
					MailApplication.showCustomServerURLDialog(getContext(), this, null);
				}
			}
		}
	}
	
	/* i have overridden this so that default value index 2 will be returned when there is no match for the value.
	 * i.e. when the user saves the custom url, while opening the list preference, there wont be any match for the custom url to highlight in the options. 
	 * So in that time instead of not highlighting anyone by returning -1 we return 2 to highliht custom url option.
	 * 
	 * (non-Javadoc)
	 * @see android.preference.ListPreference#onDialogClosed(boolean)
	 */
	@Override
	  public int findIndexOfValue(String value) {
	        if (value != null && getEntryValues() != null) {
	            for (int i = getEntryValues().length - 1; i >= 0; i--) {
	                if (getEntryValues()[i].equals(value)) {
	                    return i;
	                }
	            }
	        }
	        //high light custom url option if no match is found
	        return (PreferencesActivity.CUSTOM_URL_POSITION);
	    }
}


