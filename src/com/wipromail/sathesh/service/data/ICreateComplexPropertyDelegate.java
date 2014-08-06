/**************************************************************************
 * copyright file="ICreateComplexPropertyDelegate.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the ICreateComplexPropertyDelegate.java.
 **************************************************************************/
package com.wipromail.sathesh.service.data;

/**
 * * Used to create instances of ComplexProperty.
 * 
 * @param <TComplexProperty>
 *            Type that extends ComplexProperty
 */
interface ICreateComplexPropertyDelegate
		<TComplexProperty extends ComplexProperty> {

	/**
	 * * used to create instances of ComplexProperty.
	 * 
	 * @return Complex property instance
	 */
	TComplexProperty createComplexProperty();
}
