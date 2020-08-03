package com.sathesh.corporatemail;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;

import com.sathesh.corporatemail.application.MyActivity;
import com.sathesh.corporatemail.fragment.SettingsFragment;

public class SettingsActivity extends MyActivity implements SettingsFragment.ActivityDataPasser{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}