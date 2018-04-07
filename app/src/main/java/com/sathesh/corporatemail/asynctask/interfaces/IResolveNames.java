/**
 * 
 */
package com.sathesh.corporatemail.asynctask.interfaces;

import com.sathesh.corporatemail.service.data.NameResolutionCollection;

/**
 * @author Sathesh
 *
 */
public interface IResolveNames {

	public void handleResolvingNames();
	public void handleResolveNamesOutput(NameResolutionCollection outputCollection, String extra1);

	public void handleResolveNamesOutputError(
			NameResolutionCollection outputCollection, String extra1, Exception pE);
}
