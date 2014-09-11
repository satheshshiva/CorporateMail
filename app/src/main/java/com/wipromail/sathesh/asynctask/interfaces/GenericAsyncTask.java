package com.wipromail.sathesh.asynctask.interfaces;

public interface GenericAsyncTask {
public void activity_OnPreExecute();
public void activity_onProgressUpdate(String... progress);
public void activity_OnPostExecute();

}
