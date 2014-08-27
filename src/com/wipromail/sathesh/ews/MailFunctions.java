package com.wipromail.sathesh.ews;

import java.text.ParseException;
import java.util.Date;

import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.service.data.ExchangeService;
import com.wipromail.sathesh.service.data.FindItemsResults;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.MessageBody;
import com.wipromail.sathesh.service.data.ServiceLocalException;


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
	
	public MessageBody getBody(EmailMessage message) throws ServiceLocalException;

}
