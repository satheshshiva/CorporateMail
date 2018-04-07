package com.sathesh.corporatemail.web;

import android.webkit.WebView;

public class StandardWebView {

	public final  String  ENCODING="utf-8";
	public final  String MIME_TYPE_HTML="text/html";
	
	public void loadData(WebView webview, String html){
		//android bug fix with loadData(). have to use loaddatawithbaseurl()
		//issue:http://code.google.com/p/android/issues/detail?id=1733,http://code.google.com/p/android/issues/detail?id=4401
		webview.loadDataWithBaseURL(null, html,  MIME_TYPE_HTML, ENCODING, null);
	}
}
