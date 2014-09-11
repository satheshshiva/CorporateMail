/**************************************************************************
 * copyright file="ComplexFunctionDelegate.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the ComplexFunctionDelegate.java.
 **************************************************************************/
package com.wipromail.sathesh.service.data;

 

interface ComplexFunctionDelegate<T1 extends EwsServiceXmlReader> {
	
	 Boolean func(T1 arg1) throws  Exception;
}
