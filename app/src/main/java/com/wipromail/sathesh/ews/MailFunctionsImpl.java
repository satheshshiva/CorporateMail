package com.wipromail.sathesh.ews;

import com.wipromail.sathesh.service.data.EmailMessage;
import com.wipromail.sathesh.service.data.Item;
import com.wipromail.sathesh.service.data.ServiceLocalException;
import com.wipromail.sathesh.util.Utilities;

import java.text.ParseException;
import java.util.Date;

import static com.wipromail.sathesh.service.data.MessageBody.getStringFromMessageBody;


public class MailFunctionsImpl implements MailFunctions{

	private static MailFunctions mailFunctions= new MailFunctionsImpl();
	
	@Override
	public String getItemId(Item item) throws ServiceLocalException {
		return item.getId().getUniqueId() ;
		
	}
	
	@Override
	public String getFrom(Item item) throws ServiceLocalException,ClassCastException{
		EmailMessage mailItem = (EmailMessage) item;
		if(null != item && null != mailItem.getSender())
			return mailItem.getSender().getName();
		else
			return "";

	}

	@Override
	public String getTo(Item item) throws ServiceLocalException {
		return item.getDisplayTo();
	}

	@Override
	public String getCC(Item item) throws ServiceLocalException {
		return item.getDisplayCc();
	}

	@Override
	public boolean getIsRead(Item item) throws ServiceLocalException {
		EmailMessage mailItem = (EmailMessage) item;
		if(null != item && null != mailItem.getSender())
			return mailItem.getIsRead();
		else
			return true;

	}

	@Override
	public String getSubject(Item item) throws ServiceLocalException {
		return item.getSubject();
	}

	@Override
	public boolean hasAttachments(Item item) throws ServiceLocalException {
		return item.getHasAttachments();
	}

	@Override
	public int getSize(Item item) throws ServiceLocalException {
		return item.getSize();
	}

	@Override
	public Date getDateTimeReceived(Item item) throws ServiceLocalException, ParseException {
		//return the local time
		return Utilities.convertUTCtoLocal(item.getDateTimeReceived());
	}

	@Override
	public String getBody(Item item) throws ServiceLocalException, Exception{
		return getStringFromMessageBody(item.getBody());
	}

	public static MailFunctions getInbox() {
		return mailFunctions;
	}

	public void setInbox(MailFunctions mailFunctions) {
		this.mailFunctions = mailFunctions;
	}

}
