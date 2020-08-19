package com.sathesh.corporatemail.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sathesh.corporatemail.fragment.ViewMailFragment;
import com.sathesh.corporatemail.fragment.datapasser.ViewMailFragmentDataPasser;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.sathesh.corporatemail.constants.Constants.LOG_TAG;

public class ViewMailPagerAdapterAndListeners extends FragmentStateAdapter {
    private ArrayList<CachedMailHeaderVO> cachedHeaderVoList;
    private Map<Integer, ViewMailFragmentDataPasser> fragmentMap = new HashMap<>();

    /** constructor for the adapter
     *
     * @param fa
     * @param cachedHeaderVoList
     */
    public ViewMailPagerAdapterAndListeners(FragmentActivity fa, ArrayList<CachedMailHeaderVO> cachedHeaderVoList) {
        super(fa);
        this.cachedHeaderVoList = cachedHeaderVoList;
    }


    @NonNull
    @Override
    /**
     * Fragement generator
     */
    public Fragment createFragment(int position) {
        if (cachedHeaderVoList!=null && cachedHeaderVoList.get(position) !=null) {
            ViewMailFragment fragment = new ViewMailFragment(cachedHeaderVoList.get(position));
            fragmentMap.put(position, fragment);
            return fragment;
        }else{
            Log.e(LOG_TAG, "ViewMailPagerAdapter -> cachedHeaderVoList is null or empty. Creating a fragment with null value");
            return new ViewMailFragment(null);
        }
    }

    @Override
    public int getItemCount() {
        if (cachedHeaderVoList!=null && cachedHeaderVoList.size()>0){
            return cachedHeaderVoList.size();
        }else{
            Log.e(LOG_TAG, "ViewMailPagerAdapter -> cachedHeaderVoList is null or empty. Not able retrieve the size");
        }
        return 1; //returning 1 because at-least the view mail fragment will open
    }

    public Map<Integer, ViewMailFragmentDataPasser> getFragmentMap() {
        return fragmentMap;
    }
}
