/**
 * 
 */
package com.wipromail.sathesh.asynctask;


/**
 * @author sathesh
 *
 */
public class GetNewMails {
/*
	ExchangeService service;

	@Override
	protected Void doInBackground(Void... paramArrayOfParams) {
		// TODO Auto-generated method stub

		if (activity != null) {

			try {

				//get the total no of records in cache and get all the same number of records.
				totalCachedRecords = getTotalNumberOfRecordsInCache();

				publishProgress(status.UPDATING);
				currentStatus=status.UPDATING;

				publishProgress(status.UPDATE_CACHE_DONE);

				service = EWSConnection.getServiceFromStoredCredentials(activity.getApplicationContext());

				if(BuildConfig.DEBUG){
					Log.d(TAG, "MailListViewFragment -> Total records in cache"+totalCachedRecords);
				}

				//if the cache is present, then get the same number of rows from EWS as of the local no of rows
				int noOfMailsToFetch=(totalCachedRecords>MIN_NO_OF_MAILS?totalCachedRecords:MIN_NO_OF_MAILS);

				if(mailFolderId!=null && !(mailFolderId.equals("")))
					//Ews call
					findResults = NetworkCall.getFirstNItemsFromFolder(mailFolderId, service, noOfMailsToFetch);
				else
					//Ews call
					findResults = NetworkCall.getFirstNItemsFromFolder(WellKnownFolderName.valueOf(mailFolderName), service, noOfMailsToFetch);

				//empties the cache for this 
				if(findResults!=null){
					cacheNewData(findResults.getItems(), true);
				}

				publishProgress(status.UPDATE_LIST);
				currentStatus=status.UPDATE_LIST;
			}
			catch (final NoUserSignedInException e) {
				publishProgress(status.ERROR);
				currentStatus=status.ERROR;
				e.printStackTrace();
			}
			catch (UnknownHostException e) {
				publishProgress(status.ERROR);
				currentStatus=status.ERROR;
				e.printStackTrace();

			}
			catch(NoInternetConnectionException nic){
				publishProgress(status.ERROR);
				currentStatus=status.ERROR;
				nic.printStackTrace();
			}
			catch(HttpErrorException e){
				if(e.getMessage().toLowerCase().contains("Unauthorized".toLowerCase())){
					//unauthorised
					publishProgress(status.ERROR_AUTH_FAILED);
					currentStatus=status.ERROR_AUTH_FAILED;
				}
				else
				{
					publishProgress(status.ERROR);
					currentStatus=status.ERROR;
				}
				e.printStackTrace();
			}
			catch (Exception e) {
				publishProgress(status.ERROR);
				currentStatus=status.ERROR;
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... _int) {
		if(_int[0]==status.UPDATING){
			swipeRefreshLayout.setRefreshing(true);
			//if total cached records in the folder is more than 0 then show msg "Checking for new mails" otherwise "Update folder"
			if(totalCachedRecords>0){
				titlebar_inbox_status_textswitcher.setText(activity.getString(R.string.folder_updater_checking, getMailFolderDisplayName(mailType)).toString());
			}
			else{
				titlebar_inbox_status_textswitcher.setText(activity.getString(R.string.folder_updater_progress, getMailFolderDisplayName(mailType)).toString());
			}
			textSwitcherIcons(View.VISIBLE,View.GONE,View.GONE, View.GONE, View.GONE);
			maillist_update_progressbar.setProgress(40);

		}
		if(_int[0]==status.UPDATE_CACHE_DONE){
			maillist_update_progressbar.setProgress(65);

		}
		else if(_int[0]==status.UPDATE_LIST){
			maillist_update_progressbar.setProgress(90);
		}
		else  if(_int[0]==status.UPDATED){
			//successful update
			try {
				swipeRefreshLayout.setRefreshing(false);
				updateTextSwitcherWithMailCount();
				maillist_update_progressbar.setProgress(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Utilities.generalCatchBlock(e, this.getClass());
			}

		}
		else if(_int[0]==status.ERROR_AUTH_FAILED){
			// for auth failed show an alert box
			titlebar_inbox_status_textswitcher.setText(activity.getText(R.string.folder_auth_error));
			NotificationProcessing.showLoginErrorNotification(context);
			if(isAdded()){
				AuthFailedAlertDialog.showAlertdialog(activity, context);
			}
			else{
				Log.e(TAG, "Authentication failed. Not able to add the alert dialog due to isAdded() is false");
			}
			// stop the MNS service
			MailApplication.stopMNSService(context);
		}
		else  if(_int[0]==status.ERROR){
			textSwitcherIcons(View.GONE,View.GONE, View.VISIBLE, View.GONE, View.GONE);
			swipeRefreshLayout.setRefreshing(false);
			maillist_update_progressbar.setProgress(0);
			titlebar_inbox_status_textswitcher.setText(activity.getText(R.string.folder_updater_error));


		}
	}

	@Override
	protected void onPostExecute(Void a) {
		try {
			//refresh the display from the cache (which is now updated with new records)
			softRefreshList();
			if(currentStatus==status.UPDATE_LIST){
				publishProgress(status.UPDATED);
				currentStatus=status.UPDATED;
			}

		}
		catch(Exception e){
			if(BuildConfig.DEBUG)
				e.printStackTrace();
		}
	}

	//end of async task
}*/}
