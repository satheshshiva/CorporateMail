package com.sathesh.corporatemail.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sathesh.corporatemail.fragment.ViewMailFragment;
import com.sathesh.corporatemail.sqlite.db.cache.vo.CachedMailHeaderVO;

import java.util.ArrayList;

import static com.sathesh.corporatemail.constants.Constants.LOG_TAG;

public class ViewMailPagerAdapter  extends FragmentStateAdapter {
    private ArrayList<CachedMailHeaderVO> cachedHeaderVoList;

    public ViewMailPagerAdapter(FragmentActivity fa, ArrayList<CachedMailHeaderVO> cachedHeaderVoList) {
        super(fa);
        this.cachedHeaderVoList = cachedHeaderVoList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (cachedHeaderVoList!=null && cachedHeaderVoList.get(position) !=null) {
            return new ViewMailFragment(cachedHeaderVoList.get(position));
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
        return 1;
    }
}
