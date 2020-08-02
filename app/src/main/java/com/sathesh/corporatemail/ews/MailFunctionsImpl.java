package com.sathesh.corporatemail.ews;

import java.text.ParseException;
import java.util.Date;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;

import static microsoft.exchange.webservices.data.property.complex.MessageBody.getStringFromMessageBody;

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
		return item.getDateTimeReceived();
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
