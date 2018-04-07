package com.sathesh.corporatemail.tools;

import android.content.Context;
import android.util.Log;

import com.sathesh.corporatemail.cache.adapter.CachedMailBodyAdapter;
import com.sathesh.corporatemail.cache.adapter.CachedMailHeaderAdapter;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.sqlite.db.cache.CacheDbHelper;
import com.sathesh.corporatemail.util.Utilities;

import java.io.File;

/**This class contains method for clearing cache dirs
 * @author sathesh
 *
 */
public class CacheClear implements Constants{

    private CachedMailHeaderAdapter mailHeadersCacheAdapter;
    private CachedMailBodyAdapter mailBodyCacheAdapter;

    /** Global - Clears the full Cache directory which inclides the inline images and the Cache sqlite database
     * Called by "Clear  Cache" button in settings and Sign Out
     *
     * @param context
     */
	public static void clearFullCacheAndDbDir(Context context){

        //Clear the cache directory which contains the inline images
		clearCacheDirectory(context);

        //Delete the cache database
        CacheDbHelper.deleteDatabase(context);
	}

    /** Global - The clear caching that will happen when exiting the MailListView
     * It will clear the cache directory(inline imgs), mail list header table only 100 records and mail list body only 100
     * @param context
     * @param mailType
     * @param mailFolderId
     */
    public void mailListViewClearCache(Context context, int mailType, String mailFolderId) throws Exception{
        //Clear the cache directory which contains the inline images
        clearCacheDirectory(context);

        mailHeadersCacheAdapter = new CachedMailHeaderAdapter(context);
        mailBodyCacheAdapter = new CachedMailBodyAdapter(context);

        //deleting cached mail headers
        mailHeadersCacheAdapter.deleteN(mailType, mailFolderId, CACHE_MAX_MAIL_HEADERS_TO_KEEP);
        mailBodyCacheAdapter.deleteN(mailType, mailFolderId, CACHE_MAX_MAIL_BODY_TO_KEEP);

    }


    /** Private method for clearing the cache directory which contains the inline images
     *
     * @param context
     */
    private static void clearCacheDirectory(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                Utilities.deleteDirectory(dir);
            }
        } catch (Exception e) {
            Log.e(TAG, "CacheClear -> Error while deleting the cache directory " + e.getMessage());
        }
    }



}

