package com.sathesh.corporatemail.jsinterfaces;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.sathesh.corporatemail.constants.Constants;

public class CommonWebChromeClient extends WebChromeClient implements Constants{

	
	 @Override
     public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
         Log.d(TAG, message);
         result.confirm();
         return true;
     }
	 
	 @Override
     public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
         Log.e(TAG, "JS Line no:"+ consoleMessage.lineNumber() +" - " + consoleMessage.message() );
         return true;
        
     }
}
