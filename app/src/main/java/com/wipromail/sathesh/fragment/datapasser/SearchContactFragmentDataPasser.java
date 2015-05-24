package com.wipromail.sathesh.fragment.datapasser;


import android.view.View;
import android.widget.EditText;

public interface SearchContactFragmentDataPasser {
    void onClickDirectorySearch(View view);

    EditText getContactSearch();

}
