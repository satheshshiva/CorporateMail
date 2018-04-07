package com.sathesh.corporatemail.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.constants.Constants;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends MyActivity implements Constants {

	private SharedPreferences credStorage;
	private TextView SignedInAccUsername;
	private String SignedInAccUser=USERNAME_NULL,SignedInAccPassword=PASSWORD_NULL ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        setContentView(R.layout.activity_settings);

        //Initialize toolbar
        MailApplication.toolbarInitialize(this);

        try {
			refreshSignedIn();
		} catch (Exception e) {
			e.printStackTrace();
		}
     //   getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void refreshSignedIn() throws Exception {
    	Map<String, String> storedCredentials = new HashMap<String, String>();
    	storedCredentials = MailApplication.getStoredCredentials(this);
    	
    	SignedInAccUsername= (TextView)findViewById(R.id.settings_signed_in_user_textView);
    	
    	SignedInAccUser= storedCredentials.get("signedInAccUser");
    	SignedInAccPassword = storedCredentials.get("signedInAccPassword");
    	
    	if (null != SignedInAccUser && !(SignedInAccUser.equals(USERNAME_NULL)) && !(SignedInAccPassword.equals(PASSWORD_NULL))){
    		SignedInAccUsername.setText(SignedInAccUser);
    		findViewById(R.id.settings_signIn_btn).setVisibility(View.GONE);
    		findViewById(R.id.settings_signOut_btn).setVisibility(View.VISIBLE);
    	}
    	else{
    	SignedInAccUsername.setText(SETTINGS_NOT_SIGNED_IN_TEXT);
    	findViewById(R.id.settings_signIn_btn).setVisibility(View.VISIBLE);
		findViewById(R.id.settings_signOut_btn).setVisibility(View.GONE);
    	}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    public void signOutBtnOnclick(View view) throws Exception{
    	SharedPreferences.Editor editor = credStorage.edit();
	      editor.putString(CRED_PREFS_USERNAME, USERNAME_NULL);
	      editor.putString(CRED_PREFS_PASSWORD, PASSWORD_NULL);
	      
	      // Commit the edits!
	      editor.commit();
	      
	      refreshSignedIn();
	      
    }
    
    public void signInBtnOnclick(View view){
    	Intent intent = new Intent(this, LoginPageActivity.class);
        startActivity(intent);
	      
    }
    
  	 @Override
  	  public void onStart() {
  	    super.onStart();
  	  }

  	  @Override
  	  public void onStop() {
  	    super.onStop();
  	  }
}
