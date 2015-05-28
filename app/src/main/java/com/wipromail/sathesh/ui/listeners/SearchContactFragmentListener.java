package com.wipromail.sathesh.ui.listeners;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MyActivity;
import com.wipromail.sathesh.asynctask.ResolveNamesAsyncTask;
import com.wipromail.sathesh.asynctask.interfaces.IResolveNames;
import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.ews.EWSConnection;
import com.wipromail.sathesh.fragment.datapasser.SearchContactFragmentDataPasser;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.ui.util.UIutilities;

/**
 * Created by Sathesh on 5/28/15.
 */
public class SearchContactFragmentListener implements Constants, OnClickListener {

    private MyActivity activity;
    private Context context;
    private ExchangeService service;
    private SearchContactFragmentDataPasser fragmentDataPasser;

    public SearchContactFragmentListener(MyActivity activity, Context context, SearchContactFragmentDataPasser fragmentDataPasser){
        this.activity = activity;
        this.context = context;
        this.fragmentDataPasser = fragmentDataPasser;

        try {
            service = EWSConnection.getServiceFromStoredCredentials(context);}
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*** ON CLICK METHODS ***/
    private void onClickDirectorySearch(View view){

        UIutilities.hideKeyBoard(activity, fragmentDataPasser.getContactSearch());

        if( null != fragmentDataPasser.getContactSearch()
                && null!=fragmentDataPasser.getContactSearch().getText()
                && null!=fragmentDataPasser.getContactSearch().getText().toString()
                && !(fragmentDataPasser.getContactSearch().getText().toString().equals(""))){

            //EWS Call
            new ResolveNamesAsyncTask((IResolveNames) fragmentDataPasser,
                    activity,
                    service,
                    fragmentDataPasser.getContactSearch().getText().toString(),false,"",""
            ).execute();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.searchDirectoryBtn:
                onClickDirectorySearch(view);
                break;
        }
    }
}
