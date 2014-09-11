/**************************************************************************
 * copyright file="InvalidOperationException.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the InvalidOperationException.java.
 **************************************************************************/
package com.wipromail.sathesh.service.data;

/**
 * The Class InvalidOperationException.
 */
public class InvalidOperationException extends Exception {

	/**
	 * Instantiates a new invalid operation exception.
	 */
	public InvalidOperationException() {

	}

	/**
	 * Instantiates a new invalid operation exception.
	 * 
	 * @param strMessage
	 *            the str message
	 */
	public InvalidOperationException(String strMessage) {
		super(strMessage);
	}
}
