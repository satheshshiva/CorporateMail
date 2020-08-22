package com.sathesh.corporatemail.ui.listeners;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.sathesh.corporatemail.R;
import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.asynctask.ResolveNamesAsyncTask;
import com.sathesh.corporatemail.asynctask.interfaces.IResolveNames;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.ews.EWSConnection;
import com.sathesh.corporatemail.fragment.datapasser.SearchContactFragmentDataPasser;
import com.sathesh.corporatemail.ui.util.UIutilities;

import microsoft.exchange.webservices.data.core.ExchangeService;

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
            service = EWSConnection.getInstance(context);}
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
