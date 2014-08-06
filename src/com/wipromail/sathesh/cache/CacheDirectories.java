package com.wipromail.sathesh.cache;

import java.io.File;

import android.content.Context;
import android.os.Environment;

/** This class has the methods to get the cache directories
 * @author sathesh
 *
 */
public class CacheDirectories {
	
	/** gets the download ccahe directory
	 * @return
	 */
	public static File getDownloadCacheDirectory() {
		// TODO Auto-generated method stub
		return  Environment.getDownloadCacheDirectory();
	}

	/** gets the ApplicationCache directory
	 * @return
	 */
	public static File getApplicationCacheDirectory(Context context) {
		// TODO Auto-generated method stub
		return  context.getCacheDir();
	}
}
