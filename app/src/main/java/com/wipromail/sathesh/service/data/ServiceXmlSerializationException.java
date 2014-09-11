/**************************************************************************
 * copyright file="ServiceXmlSerializationException.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the ServiceXmlSerializationException.java.
 **************************************************************************/
package com.wipromail.sathesh.service.data;

/**
 * Represents an error that occurs when the XML for a request cannot be
 * serialized.
 * 
 */
public class ServiceXmlSerializationException extends ServiceLocalException {

	/**
	 * ServiceXmlSerializationException Constructor.
	 */
	public ServiceXmlSerializationException() {
		super();
	}

	/**
	 * Instantiates a new service xml serialization exception.
	 * 
	 * @param message
	 *            the message
	 */
	public ServiceXmlSerializationException(String message) {
		super(message);

	}

	/**
	 * Instantiates a new service xml serialization exception.
	 * 
	 * @param message
	 *            the message
	 * @param innerException
	 *            the inner exception
	 */
	public ServiceXmlSerializationException(String message,
			Exception innerException) {
		super(message, innerException);
	}

}
