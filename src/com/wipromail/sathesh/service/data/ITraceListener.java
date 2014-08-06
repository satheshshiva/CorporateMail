/**************************************************************************
 * copyright file="ITraceListener.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the ITraceListener.java.
 **************************************************************************/
package com.wipromail.sathesh.service.data;

/**
 * ITraceListener handles message tracing.
 */
public interface ITraceListener {

	/**
	 * Handles a trace message.
	 * 
	 * @param traceType
	 *            Type of trace message.
	 * @param traceMessage
	 *            The trace message.
	 */
	void trace(String traceType, String traceMessage);

}
