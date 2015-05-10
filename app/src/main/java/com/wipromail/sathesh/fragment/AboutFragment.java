package com.wipromail.sathesh.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.wipromail.sathesh.R;
import com.wipromail.sathesh.application.MailApplication;
import com.wipromail.sathesh.jsinterfaces.AboutActivityJSInterface;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InteractionListener} interface
 * to handle interaction events.
 * Use the {@link AboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutFragment extends Fragment {

    public ActionBarActivity activity ;
    private Context context ;

    private Button bugOrSuggestionBtn;
    private WebView wv;

    private InteractionListener mListener;

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (InteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement InteractionListener");
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
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        activity =   (ActionBarActivity)getActivity();
        context =  getActivity();

        wv = (WebView)view.findViewById(R.id.aboutActivityWebView);

        //the following code will prevent new webview from opening when loading url
        wv.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view1, String url) {

                view1.loadUrl(url);
                return true;
            }});

        WebSettings webSettings = wv.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);	//this is important
        webSettings.setSupportZoom(false);
        //for displaying js alert in console

        //	wv.setWebChromeClient(new CommonWebChromeClient());

        wv.addJavascriptInterface(new AboutActivityJSInterface(), AboutActivityJSInterface.ABOUT_ACTIVITY_JS_INTERFACE_NAME);

        bugOrSuggestionBtn = (Button)view.findViewById(R.id.about_activity_bugOrSuggestion_btn);
        //hide the Bug/suggestion buttton if no user signed in.
        try {
            if(!(MailApplication.checkUserIfSignedIn(context))){
                bugOrSuggestionBtn.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    public interface InteractionListener {
    }

}
