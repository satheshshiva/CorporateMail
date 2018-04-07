package com.sathesh.corporatemail.ews;

import com.sathesh.corporatemail.service.data.Item;
import com.sathesh.corporatemail.service.data.ServiceLocalException;

import java.text.ParseException;
import java.util.Date;


public interface MailFunctions {

	public String getFrom(Item item) throws ServiceLocalException;
	
	public String getTo(Item item) throws ServiceLocalException;
	
	public String getCC(Item item) throws ServiceLocalException;
	
	public boolean getIsRead(Item item) throws ServiceLocalException;
	
	public String getSubject(Item item) throws ServiceLocalException;
	
	public boolean hasAttachments(Item item) throws ServiceLocalException;
	
	public int getSize(Item item) throws ServiceLocalException;
	
	public Date getDateTimeReceived(Item item) throws ServiceLocalException, ParseException;
	
	public String getItemId(Item item) throws ServiceLocalException, Exception;
	
	public String getBody(Item item) throws ServiceLocalException, Exception;

}
