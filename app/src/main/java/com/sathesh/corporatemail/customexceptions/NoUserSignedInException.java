package com.sathesh.corporatemail.customexceptions;

public class NoUserSignedInException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4553279518248519777L;

	@Override
	public String toString() {
		return "No User has signed in";
	}


}
