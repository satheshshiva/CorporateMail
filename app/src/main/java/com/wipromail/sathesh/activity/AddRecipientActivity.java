package com.wipromail.sathesh.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.MyActivity;
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
import com.wipromail.sathesh.ui.util.UIutilities;
import com.wipromail.sathesh.util.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRecipientActivity extends MyActivity implements Constants,IResolveNames {

	private EditText contactSearch;
	private ExchangeService service;
	private ListView listView;
	private Activity activity;
	
	public final static String   ADD_TYPE_EXTRA="ADD_TYPE_EXTRA";
	public final static String   ADD_TYPE_COLLECTION="ADD_TYPE_COLLECTION";
	public final static int   ADD_TYPE_TO=0;
	public final static int   ADD_TYPE_CC=1;
	public final static int   ADD_TYPE_BCC=2;
	private final Map<Integer, NameResolution> dispMap = new HashMap<Integer, NameResolution>();


	private final Map<Integer, ContactSerializable> dispContactsMap = new HashMap<Integer, ContactSerializable>();

	private final ArrayList<ContactSerializable> selectedList = new ArrayList<ContactSerializable>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_add_recipient);

        activity=this;

        //Initialize toolbar
        MailApplication.toolbarInitialize(this);

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
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		setSupportProgressBarIndeterminateVisibility(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public void onClickDirectorySearch(View view){
		UIutilities.hideKeyBoard(activity, contactSearch);
		if( null!=contactSearch.getText() && null!=contactSearch.getText().toString() && !(contactSearch.getText().toString().equals(""))){

			//EWS Call

			new ResolveNamesAsyncTask(this,this,service,contactSearch.getText().toString(),false,"","").execute();

		}
	}

	private ArrayList<ContactSerializable> getCheckedItems() {
		int cntChoice = listView.getCount();
		selectedList.clear();
		SparseBooleanArray sparseBooleanArray = listView.getCheckedItemPositions();

		for(int i = 0; i < cntChoice; i++){

			if(sparseBooleanArray.get(i)) {

				selectedList.add(dispContactsMap.get(i));

			}
		}

		return selectedList;
	}

	//To onclick 
	public void addToOnClick(View view){


		ArrayList<ContactSerializable> selectedList = getCheckedItems();
		if(selectedList.size()<=0 && contactSearch.getText() !=null && (!(contactSearch.getText().toString().equals("")))&& (Utilities.isValidEmail(contactSearch.getText().toString()))){

			//If there is nothing selected in the list then take the contact search as Email Id, create a ContactSerializable for that and mark the flag to try to resolve it
			ContactSerializable sContact = new ContactSerializable();
			sContact.setDisplayName(contactSearch.getText().toString());
			sContact.setEmail(contactSearch.getText().toString());
			sContact.setTryResolveNamesInDirectory(true);
			selectedList.add(sContact);

		}
		if(selectedList.size()>0){
			Intent data = new Intent();
			data.putExtra(ADD_TYPE_EXTRA, ADD_TYPE_TO);
			data.putExtra(ADD_TYPE_COLLECTION, selectedList);
			// Activity finished ok, return the data
			setResult(RESULT_OK, data);
			finish();
		}
		else{
			//contact search box empty
			Notifications.showToast(this, getText(R.string.addrecipient_select_person), Toast.LENGTH_SHORT);
		}
	}


	//CC on click
	public void addCCOnClick(View view){


		ArrayList<ContactSerializable> selectedList = getCheckedItems();
		if(selectedList.size()<=0 && contactSearch.getText() !=null && (!(contactSearch.getText().toString().equals(""))) && (Utilities.isValidEmail(contactSearch.getText().toString()))){

			//If there is nothing selected in the list then take the contact search as entry, create a ContactSerializable for that
			ContactSerializable sContact = new ContactSerializable();
			sContact.setDisplayName(contactSearch.getText().toString());
			sContact.setEmail(contactSearch.getText().toString());
			sContact.setTryResolveNamesInDirectory(true);
			selectedList.add(sContact);

		}
		if(selectedList.size()>0){
			Intent data = new Intent();
			data.putExtra(ADD_TYPE_EXTRA, ADD_TYPE_CC);
			data.putExtra(ADD_TYPE_COLLECTION, selectedList);
			// Activity finished ok, return the data
			setResult(RESULT_OK, data);

			finish();
		}
		else{
			//contact search box empty
			Notifications.showToast(this, getText(R.string.addrecipient_select_person), Toast.LENGTH_SHORT);
		}
	}

	//BCC onclick
	public void addBCCOnClick(View view){

		ArrayList<ContactSerializable> selectedList = getCheckedItems();
		if(selectedList.size()<=0 && contactSearch.getText() !=null && (!(contactSearch.getText().toString().equals("")))&& (Utilities.isValidEmail(contactSearch.getText().toString()))){

			//If there is nothing selected in the list then take the contact search as entry, create a ContactSerializable for that
			ContactSerializable sContact = new ContactSerializable();
			sContact.setDisplayName(contactSearch.getText().toString());
			sContact.setEmail(contactSearch.getText().toString());
			sContact.setTryResolveNamesInDirectory(true);
			selectedList.add(sContact);

		}
		if(selectedList.size()>0){
			Intent data = new Intent();
			data.putExtra(ADD_TYPE_EXTRA, ADD_TYPE_BCC);
			data.putExtra(ADD_TYPE_COLLECTION, selectedList);
			// Activity finished ok, return the data
			setResult(RESULT_OK, data);

			finish();
		}
		else{
			//contact search box empty
			Notifications.showToast(this, getText(R.string.addrecipient_select_person), Toast.LENGTH_SHORT);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		return true;
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
	public void handleResolveNamesOutput(
			NameResolutionCollection outputCollection, String extra1)  {

		ContactSerializable sContact;

		Log.d(TAG, "handle called "+ outputCollection);
		try {
			if(outputCollection!=null && outputCollection.getCount()>0){

				List<String> dispNameList = new ArrayList<String>();

				int resolveNameIndex=-1;
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
				ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_multiple_choice,dispNameList);
				listView.setAdapter(adapter);
			}
			else{
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

		Notifications.showToast(this, getText(R.string.searchContact_error), Toast.LENGTH_SHORT);
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void handleResolvingNames() {
		setSupportProgressBarIndeterminateVisibility(true);
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
