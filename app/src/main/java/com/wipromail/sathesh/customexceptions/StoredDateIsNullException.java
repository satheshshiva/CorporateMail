package com.wipromail.sathesh.customexceptions;

public class StoredDateIsNullException extends Exception
{
  private static final long serialVersionUID = 4553279518248519777L;

  public String toString()
  {
    return "The Last successful Sync is not available. May be the sync if first time.";
  }
}
