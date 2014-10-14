package com.wipromail.sathesh.tools;

import android.content.Context;
import android.util.Log;

import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.sqlite.db.cache.CacheDbHelper;
import com.wipromail.sathesh.util.Utilities;

import java.io.File;

/**This class contains method for clearing cache dirs
 * @author sathesh
 *
 */
public class ClearCache implements Constants{

	public static void clearFullCacheAndDbDir(Context context){

        //Clear the cache directory which contains the inline images
		clearCacheDirectory(context);

        //Delete the cache database
        CacheDbHelper.deleteDatabase(context);
	}

    private static void clearCacheDirectory(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                Utilities.deleteDirectory(dir);
            }
        } catch (Exception e) {
            Log.e(TAG, "ClearCache -> Error while deleting the cache directory " + e.getMessage());
        }
    }

}

