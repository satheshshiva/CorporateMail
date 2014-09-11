/**************************************************************************
 * copyright file="Callback.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the Callback.java.
 **************************************************************************/
package com.wipromail.sathesh.service.data;

import java.util.concurrent.Future;

interface Callback<T> {
	T  processMe(Future task);

}
