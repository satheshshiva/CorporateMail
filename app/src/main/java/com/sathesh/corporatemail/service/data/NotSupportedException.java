package com.sathesh.corporatemail.service.data;

public class NotSupportedException extends Exception {
	/**
	 * Instantiates a new argument exception.
	 */
	public NotSupportedException() {
		super();
		
	}
	
	/**
	 * Instantiates a new NotSupported exception.
	 * 
	 * @param strMessage
	 *            the str message
	 */
	public NotSupportedException(String strMessage) {
		super(strMessage);
	}
}
