package com.wipromail.sathesh.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.asynctask.ResolveNamesAsyncTask;
import com.wipromail.sathesh.asynctask.interfaces.IResolveNames;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.NameResolution;
import com.wipromail.sathesh.service.data.NameResolutionCollection;
import com.wipromail.sathesh.service.data.ServiceLocalException;
import com.wipromail.sathesh.ui.UIutilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchContactsActivity extends SherlockActivity  implements Constants,IResolveNames{

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
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_search_contacts);

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

		Log.d(TAG, "handle called "+ outputCollection);
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
				Log.d(TAG, "dispNameList "+dispNameList);
				Log.d(TAG, "dispMap" +dispMap);
				Log.d(TAG, "dispContactsMap" +dispContactsMap);
				ListAdapter adapter = new ArrayAdapter<String>(this,R.layout.simple_list_item_1,dispNameList);
				listView.setAdapter(adapter);
				
				// list onclick listener
				listView.setOnItemClickListener(new OnItemClickListener() {
					
					@Override
					public void onItemClick(AdapterView<?> adapter, View view, int position,
							long arg) {
						Log.d(TAG, String.valueOf(position));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void handleResolveNamesOutputError(
			NameResolutionCollection outputCollection, String extra1,
			Exception pE) {
		// TODO Auto-generated method stub

		Notifications.showToast(this, getText(R.string.addrecipient_error), Toast.LENGTH_SHORT);
		setSupportProgressBarIndeterminateVisibility(false);
	}

}
