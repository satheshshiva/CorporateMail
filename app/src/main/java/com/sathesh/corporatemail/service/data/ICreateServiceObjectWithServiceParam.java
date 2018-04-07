/**************************************************************************
 * copyright file="ICreateServiceObjectWithServiceParam.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the ICreateServiceObjectWithServiceParam.java.
 **************************************************************************/
package com.sathesh.corporatemail.service.data;

/**
 * The Interface ICreateServiceObjectWithServiceParam.
 */
interface ICreateServiceObjectWithServiceParam {

	/**
	 * Creates the service object with service param.
	 * 
	 * @param srv
	 *            the srv
	 * @return the object
	 * @throws Exception
	 *             the exception
	 */
	Object createServiceObjectWithServiceParam(ExchangeService srv)
			throws Exception;
}
