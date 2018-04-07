package com.sathesh.corporatemail.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MailApplication;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.asynctask.ResolveNamesAsyncTask;
import com.sathesh.corporatemail.asynctask.interfaces.IResolveNames;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.customserializable.ContactSerializable;
import com.sathesh.corporatemail.customui.Notifications;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.service.data.ExchangeService;
import com.sathesh.corporatemail.service.data.NameResolution;
import com.sathesh.corporatemail.service.data.NameResolutionCollection;
import com.sathesh.corporatemail.service.data.ServiceLocalException;
import com.sathesh.corporatemail.ui.util.UIutilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class SearchContactsActivity extends MyActivity implements Constants,IResolveNames{

	private EditText contactSearch;
	private ExchangeService service;
	private ListView listView;
	private final Map<Integer, NameResolution> dispMap = new HashMap<Integer, NameResolution>();
	private Activity activity;

	private final Map<Integer, ContactSerializable> dispContactsMap = new HashMap<Integer, ContactSerializable>();
	private List<String> dispNameList = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.activity = this;

		setContentView(R.layout.fragment_search_contacts);

        //Initialize toolbar
        MailApplication.toolbarInitialize(this);

		getSupportActionBar().setHomeButtonEnabled(true);
		contactSearch= (EditText)findViewById(R.id.contactSearch);

		try {
			service = EWSConnection.getServiceFromStoredCredentials(this);}
		catch (Exception e) {
			e.printStackTrace();
		}

		listView = (ListView)findViewById(R.id.suggestionsListView);
		setSupportProgressBarIndeterminateVisibility(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	public void onClickDirectorySearch(View view){
		UIutilities.hideKeyBoard(activity, contactSearch);
		
		if( null!=contactSearch.getText() && null!=contactSearch.getText().toString() && !(contactSearch.getText().toString().equals(""))){

			//EWS Call

			new ResolveNamesAsyncTask(this,this,service,contactSearch.getText().toString(),false,"","").execute();

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item!=null && item.getItemId()==android.R.id.home){
			finish();
		}

		return super.onOptionsItemSelected(item);
	}
	@Override
	public void handleResolvingNames() {
		setSupportProgressBarIndeterminateVisibility(true);
	}

	@Override
	public void handleResolveNamesOutput(
			NameResolutionCollection outputCollection, String extra1) {

		ContactSerializable sContact;
		dispNameList.clear();		// this holds the name list to be displayed in listview
		dispMap.clear();			// map containing the name resolution
		dispContactsMap.clear();		// map containing ContactSerializable which can be passed to next intent

        if(BuildConfig.DEBUG) {
            Log.d(TAG, "handle called " + outputCollection);
        }
		try {
			if(outputCollection!=null && outputCollection.getCount()>0){
				int resolveNameIndex=-1;
				Log.i(TAG, "index 1 " +String.valueOf(resolveNameIndex));
				for(NameResolution nameResolution : outputCollection)
				{
					if(nameResolution!=null && null!=nameResolution.getContact() && null!=nameResolution.getContact().getDisplayName()){
						resolveNameIndex++;
						dispNameList.add(nameResolution.getContact().getDisplayName());
						dispMap.put(resolveNameIndex, nameResolution);
						sContact = ContactSerializable.getContactSerializableFromContact(nameResolution.getContact(), nameResolution.getMailbox().getAddress());

						dispContactsMap.put(resolveNameIndex, sContact);
					}
				}
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "dispNameList " + dispNameList);
                    Log.d(TAG, "dispMap" + dispMap);
                    Log.d(TAG, "dispContactsMap" + dispContactsMap);
                }
				ListAdapter adapter = new ArrayAdapter<String>(this,R.layout.simple_list_item_1,dispNameList);
				listView.setAdapter(adapter);
				
				// list onclick listener
				listView.setOnItemClickListener(new OnItemClickListener() {
					
					@Override
					public void onItemClick(AdapterView<?> adapter, View view, int position,
							long arg) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, String.valueOf(position));
                        }
						Intent contactDetailsIntent = new Intent(getBaseContext(), ContactDetailsActivity.class);
						contactDetailsIntent.putExtra(ContactDetailsActivity.CONTACT_SERIALIZABLE_EXTRA, dispContactsMap.get(position));
						contactDetailsIntent.putExtra(ContactDetailsActivity.SHOW_SENDMAIL_BTN_EXTRA, true);
						startActivity(contactDetailsIntent);
					}
				});
			}
			else{
				ListAdapter adapter = new ArrayAdapter<String>(this,R.layout.simple_list_item_1,dispNameList);
				listView.setAdapter(adapter);
				Notifications.showToast(this, getText(R.string.addrecipient_nomatch), Toast.LENGTH_SHORT);
			}
		} catch (ServiceLocalException e) {
			e.printStackTrace();
		}
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void handleResolveNamesOutputError(
			NameResolutionCollection outputCollection, String extra1,
			Exception pE) {
		Notifications.showToast(this, getText(R.string.addrecipient_error), Toast.LENGTH_SHORT);
		setSupportProgressBarIndeterminateVisibility(false);
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
