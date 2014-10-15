package com.wipromail.sathesh.cache;

import android.content.Context;
import android.os.Environment;

import com.wipromail.sathesh.constants.Constants;

import java.io.File;

/** This class has the methods to get the cache directories
 * @author sathesh
 *
 */
public class CacheDirectories implements Constants {
	
	/** gets the download ccahe directory
	 * @return
	 */
	public static File getDownloadCacheDirectory() {
		return  Environment.getDownloadCacheDirectory();
	}

	/** gets the ApplicationCache directory
	 * @return
	 */
	public static File getApplicationCacheDirectory(Context context) {
		return  context.getCacheDir();
	}

    /** Gets the inline imgs cache directory
     * @param context
     * @return
     * @throws Exception
     */
    public static String getMailCacheImageDirectory(Context context) throws Exception{
        return getApplicationCacheDirectory(context)+"/" + CACHE_DIRECTORY_INLINE_IMGS ;
    }
}
