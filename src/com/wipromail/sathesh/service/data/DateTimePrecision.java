/**************************************************************************
 * copyright file="DateTimePrecision.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the DateTimePrecision.java.
 **************************************************************************/
package com.wipromail.sathesh.service.data;

/**
 * Defines the precision for returned DateTime values
 */
public enum DateTimePrecision {

	// Default value. No SOAP header emitted.
	Default,
	
	// Seconds
	
	Seconds,

	// Milliseconds

	Milliseconds
}
