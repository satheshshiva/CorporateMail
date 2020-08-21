package com.sathesh.corporatemail.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.sathesh.corporatemail.BuildConfig;
import com.sathesh.corporatemail.constants.Constants;
import com.sathesh.corporatemail.security.EncryptionDecryption;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Sathesh
 *
 */
/**
 * @author sathesh
 *
 */
public class Utilities implements Constants {

	public static String encrypt(String cleartext) throws Exception{
		return EncryptionDecryption.encrypt(cleartext);

	}

	public static String decrypt(String encrptedString) throws Exception{
		return EncryptionDecryption.decrypt( encrptedString);

	}

	public static String convertEditableToHTML(Editable text){

		Spannable spannedBody=text;
		return Html.toHtml(spannedBody);

	}

	public static String convertHTMLToText(String html){


		Spanned spannedHtml=Html.fromHtml(html);
		if(null!=spannedHtml){
			return Html.fromHtml(html).toString();
		}
		else{
			return "";
		}

	}

	/** Gives the src url to be used for an image file
	 * @param imagePath
	 * @return
	 */
	public static String getHTMLImageUrl(String contentType , String imagePath) {
		/*System.out.println( "MyUtilities imagepath" + imagePath);
		String base64= getBase64DataUrl(contentType, imagePath);
		Log.d(TAG, "MyUtilities base 64" + base64);

		return "data:" +contentType +  ";base64," +base64;*/

		return "file://"+ imagePath;
		//return "content://com.wipromail.sathesh.localfile"+imagePath;
	}

	/**
	 * @return
	 */
	public static String getBase64DataUrl(String contentType , String imagePath) {
		byte[] a=convertImageToByteArray(contentType , imagePath);

		return Base64.encodeToString(convertImageToByteArray(contentType , imagePath), Base64.DEFAULT);
	}

	/**
	 * @param contentType
	 * @param imagePath
	 * @return
	 */
	public static byte[] convertImageToByteArray(String contentType , String imagePath){
		Bitmap bm = BitmapFactory.decodeFile(imagePath);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		if(contentType !=null && !(contentType.equals(""))){

			if(contentType.toLowerCase().contains("jpeg") || contentType.toLowerCase().contains("jpg") ){
				boolean b=bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
			}
			else if(contentType.toLowerCase().contains("png") ){
				bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
			}
		}
		return baos.toByteArray(); 

	}
	/** deletes non empty directory
	 * @param dir
	 * @return
	 */
	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();


	}

	/** Check wether the email address is valid
	 * @param emailAddress
	 * @return
	 */
	public static boolean isValidEmail(String emailAddress) {
		return emailAddress.contains(" ") == false && emailAddress.matches(".+@.+\\.[a-z]+");
	} 

	/** input a string of array. outut a string array with trim() applied
	 * @return	string array
	 */
	public static String[] trimArrayString(String[] strarray){
		String[] returnArray=null;
		if(strarray!=null && strarray.length > 0){
			returnArray = new String[strarray.length];
			int i=0;
			while(i<strarray.length){
				if(strarray[i]!=null){
					returnArray[i] = strarray[i].trim();
				}
				else{
					returnArray[i] ="";
				}
				i++;
			}
		}

		return returnArray;
	}

	public static long getNumberOfDaysFromToday(Date date){
		long dateDiff=0;

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		Calendar cDate = Calendar.getInstance();
		cDate.setTime(date);
		cDate.set(Calendar.HOUR_OF_DAY, 0);
		cDate.set(Calendar.MINUTE, 0);
		cDate.set(Calendar.SECOND, 0);
		cDate.set(Calendar.MILLISECOND, 0);

		dateDiff = (today.getTimeInMillis() - cDate.getTimeInMillis()) / (1000 * 60 * 60 * 24) ;
		return dateDiff;

	}


	/**
	 * use to clear the shared preference from memory and file system. since shared preferences are used from memory u have clear and commit.
	 * http://stackoverflow.com/questions/6125296/delete-sharedpreferences-file/17403413#17403413
	 * @param Context
	 * @param filesToDel	string array of the files in the shared preferences folder to delete. Pass Null to delete all the files in the folder.
	 */
	public static void deleteSharedPreferences(Context Context, String... filesToDel){
		File dir = new File(Context.getFilesDir().getParent() + "/shared_prefs/");

		if (filesToDel==null || filesToDel.length<1 ){
			Log.d(LOG_TAG, "filesToDel " + filesToDel);
			filesToDel = dir.list();
		}
		Log.d(LOG_TAG, "Files for deletion " );
		for(int i=0;i<filesToDel.length; i++){
			Log.d(LOG_TAG, filesToDel[i]);
		}

		for (int i = 0; i < filesToDel.length; i++) {
			// clear each of the preferences
			Context.getSharedPreferences(filesToDel[i].replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
		}
		// Make sure it has enough time to save all the commited changes
		try { Thread.sleep(1000); } catch (InterruptedException e) {}
		for (int i = 0; i < filesToDel.length; i++) {
			// delete the files
			Log.d(LOG_TAG, "Deleting file " + dir + filesToDel[i]);
			new File(dir, filesToDel[i]).delete();
			Log.d(LOG_TAG, "Deleting file " + dir + filesToDel[i].replace(".xml", ".bak"));
			//delete the back up files if exists
			new File(dir, filesToDel[i].replace(".xml", ".bak")).delete();
		}
	}

    /** generic catch block which will log the exception
     * @param thisClass - pass this object
     * @param e
     */
    public static void generalCatchBlock( Exception e , Object thisClass) {
        generalCatchBlock(e, null, thisClass.getClass());
    }

    /** generic catch block which will log the exception
     * @param thisClass - pass this
     * @param e
     */
    public static void generalCatchBlock( Exception e, String additionalMsg, Object thisClass){
        Log.e(LOG_TAG, "Exception Occured ");
        if(additionalMsg!=null && !additionalMsg.equals("")){
            Log.e(LOG_TAG, additionalMsg);
        }
        Log.e(LOG_TAG, new StringBuffer()
                .append(e.getClass().getName()).append(":").append(e.getMessage())
                .append(" on ")
                .append(thisClass.getClass().getName()).append(":").append(e.getStackTrace()[0].getLineNumber())
                .toString());
        if(BuildConfig.DEBUG)
            e.printStackTrace();
    }

    /** generic catch block which will log the exception
     * @param e
     */
    public static void generalCatchBlock( Exception e, String additionalMsg){
        Log.e(LOG_TAG, "Exception Occured ");
        if(additionalMsg!=null && !additionalMsg.equals("")){
            Log.e(LOG_TAG, additionalMsg);
        }
        Log.e(LOG_TAG, new StringBuffer()
                .append(e.getClass().getName()).append(":").append(e.getMessage())
                .toString());
        if(BuildConfig.DEBUG)
            e.printStackTrace();
    }

	public static void openFile(Context context, File url) throws Exception{

		try {
			Uri uri = FileProvider.getUriForFile(
					context,
					context.getApplicationContext()
							.getPackageName() + ".provider", url);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
				// Word document
				intent.setDataAndType(uri, "application/msword");
			} else if (url.toString().contains(".pdf")) {
				// PDF file
				intent.setDataAndType(uri, "application/pdf");
			} else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
				// Powerpoint file
				intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
			} else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
				// Excel file
				intent.setDataAndType(uri, "application/vnd.ms-excel");
			} else if (url.toString().contains(".zip")) {
				// ZIP file
				intent.setDataAndType(uri, "application/zip");
			} else if (url.toString().contains(".rar")){
				// RAR file
				intent.setDataAndType(uri, "application/x-rar-compressed");
			} else if (url.toString().contains(".rtf")) {
				// RTF file
				intent.setDataAndType(uri, "application/rtf");
			} else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
				// WAV audio file
				intent.setDataAndType(uri, "audio/x-wav");
			} else if (url.toString().contains(".gif")) {
				// GIF file
				intent.setDataAndType(uri, "image/gif");
			} else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
				// JPG file
				intent.setDataAndType(uri, "image/jpeg");
			} else if (url.toString().contains(".txt")) {
				// Text file
				intent.setDataAndType(uri, "text/plain");
			} else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
					url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
				// Video files
				intent.setDataAndType(uri, "video/*");
			} else {
				intent.setDataAndType(uri, "*/*");
			}
			intent.setData(uri);
			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			throw new Exception("No suitable application to open this file");
		}
	}

}
