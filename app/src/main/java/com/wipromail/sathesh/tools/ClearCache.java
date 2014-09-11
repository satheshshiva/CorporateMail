package com.wipromail.sathesh.tools;

import java.io.File;

import android.content.Context;
import android.util.Log;

import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.util.Utilities;

/**This class contains method for clearing cache dirs
 * @author sathesh
 *
 */
public class ClearCache implements Constants{

	public static boolean clearFullCacheAndDbDir(Context context){
		try {
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory()) {
				Utilities.deleteDirectory(dir);
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "ClearCache -> Error while deleting the cache directory " + e.getMessage());
			return false;
		}
	}

}

