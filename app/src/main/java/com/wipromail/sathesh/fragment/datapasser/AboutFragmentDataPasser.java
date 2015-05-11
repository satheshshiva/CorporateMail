package com.wipromail.sathesh.fragment.datapasser;


import android.content.pm.PackageManager;
import android.view.View;

public interface AboutFragmentDataPasser {

    void onClickChkUpdate(View view);

    void onBugOrSuggestion(View view) throws PackageManager.NameNotFoundException;

    void fbOnclick(View view);

    void downloadAndUpdate();

}
