package com.wipromail.sathesh.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wipromail.sathesh.BuildConfig;
import com.wipromail.sathesh.R;
import com.wipromail.sathesh.activity.ContactDetailsActivity;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.asynctask.ResolveNamesAsyncTask;
import com.wipromail.sathesh.asynctask.interfaces.IResolveNames;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.customserializable.ContactSerializable;
import com.wipromail.sathesh.customui.Notifications;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.fragment.datapasser.SearchContactFragmentDataPasser;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.NameResolution;
import com.wipromail.sathesh.service.data.NameResolutionCollection;
import com.wipromail.sathesh.service.data.ServiceLocalException;
import com.wipromail.sathesh.ui.util.UIutilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchContactFragment extends Fragment implements Constants,SearchContactFragmentDataPasser,IResolveNames {

    public MyActivity activity ;
    private Context context ;
    private EditText contactSearch;
    private ActivityDataPasser activityDataPasser;
    private static SearchContactFragment fragment;
    private ExchangeService service;
    private ListView listView;
    private ActionBar myActionBar;
    private ActivityDataPasser mListener;

    private final Map<Integer, ContactSerializable> dispContactsMap = new HashMap<Integer, ContactSerializable>();
    private List<String> dispNameList = new ArrayList<String>();
    private final Map<Integer, NameResolution> dispMap = new HashMap<Integer, NameResolution>();
    private View view;

    /** Factory for this fragment
     *
     * @return
     */
    public static SearchContactFragment newInstance() {
        fragment = new SearchContactFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static SearchContactFragment getCurrentInstance(){
        return fragment;
    }

    public SearchContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ActivityDataPasser) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ActivityDataPasser");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate layout for this fragment
        view = inflater.inflate(R.layout.fragment_search_contacts, container, false);

        activity =   (MyActivity)getActivity();
        activityDataPasser =   (ActivityDataPasser)getActivity();
        context =  getActivity();

        //Initialize toolbar
        MailApplication.toolbarInitialize(activity, view);

        //action bar initialize
        myActionBar = activity.getSupportActionBar();
        //update mail type in the action bar title
        myActionBar.setTitle(activity.getString(R.string.drawer_menu_search_contact));
        myActionBar.setDisplayHomeAsUpEnabled(true);
        myActionBar.setHomeButtonEnabled(true);

        activity.getSupportActionBar().setHomeButtonEnabled(true);
        contactSearch= (EditText)view.findViewById(R.id.contactSearch);

        try {
            service = EWSConnection.getServiceFromStoredCredentials(context);}
        catch (Exception e) {
            e.printStackTrace();
        }

        listView = (ListView)view.findViewById(R.id.suggestionsListView);
        activity.setSupportProgressBarIndeterminateVisibility(view, false);

        return view;
    }

    /** ON RESUME **/
    @Override
    public void onResume() {
        super.onResume();

        try {
            // focus the edit text
            UIutilities.showKeyBoard(context, contactSearch);

            activityDataPasser.getmDrawerToggle().syncState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*** ON CLICK METHODS ***/
    public void onClickDirectorySearch(View view){
        UIutilities.hideKeyBoard(activity, contactSearch);

        if( null!=contactSearch.getText() && null!=contactSearch.getText().toString() && !(contactSearch.getText().toString().equals(""))){

            //EWS Call
            new ResolveNamesAsyncTask(this,activity,service,contactSearch.getText().toString(),false,"","").execute();
        }
    }

    // asynchronous calls
    @Override
    public void handleResolvingNames() {
        activity.setSupportProgressBarIndeterminateVisibility(view, true);
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
                ListAdapter adapter = new ArrayAdapter<String>(context,R.layout.simple_list_item_1,dispNameList);
                listView.setAdapter(adapter);

                // list onclick listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position,
                                            long arg) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, String.valueOf(position));
                        }
                        Intent contactDetailsIntent = new Intent(context, ContactDetailsActivity.class);
                        contactDetailsIntent.putExtra(ContactDetailsActivity.CONTACT_SERIALIZABLE_EXTRA, dispContactsMap.get(position));
                        contactDetailsIntent.putExtra(ContactDetailsActivity.SHOW_SENDMAIL_BTN_EXTRA, true);
                        startActivity(contactDetailsIntent);

                    }
                });
            }
            else{
                ListAdapter adapter = new ArrayAdapter<String>(context,R.layout.simple_list_item_1,dispNameList);
                listView.setAdapter(adapter);
                Notifications.showToast(context, getText(R.string.addrecipient_nomatch), Toast.LENGTH_SHORT);
            }
        } catch (ServiceLocalException e) {
            e.printStackTrace();
        }
        activity.setSupportProgressBarIndeterminateVisibility(view, false);
    }

    @Override
    public void handleResolveNamesOutputError(
            NameResolutionCollection outputCollection, String extra1,
            Exception pE) {
        Notifications.showToast(context, getText(R.string.addrecipient_error), Toast.LENGTH_SHORT);
        activity.setSupportProgressBarIndeterminateVisibility(view, false);
    }

    /** Interface - Fragment Interaction Listener
     *
     */
    public interface ActivityDataPasser {
        android.support.v7.app.ActionBarDrawerToggle getmDrawerToggle();
    }

}
