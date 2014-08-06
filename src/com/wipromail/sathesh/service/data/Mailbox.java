/**************************************************************************
 * copyright file="Mailbox.java" company="Microsoft"
 *     Copyright (c) Microsoft Corporation.  All rights reserved.
 * 
 * Defines the Mailbox.java.
 **************************************************************************/
package com.wipromail.sathesh.service.data;

import javax.xml.stream.XMLStreamException;

/***
 * Represents a mailbox reference.
 * 
 */
public class Mailbox extends ComplexProperty implements ISearchStringProvider {

	// Routing type
	/** The routing type. */
	private String routingType;

	// Email address
	/** The address. */
	private String address;

	/***
	 * Initializes a new instance of the Mailbox class.
	 */
	public Mailbox() {
		super();
	}

	/**
	 * * Initializes a new instance of the Mailbox class.
	 * 
	 * @param smtpAddress
	 *            the smtp address
	 */
	public Mailbox(String smtpAddress) {
		this();
		this.setAddress(smtpAddress);
	}

	/**
	 * * Initializes a new instance of the Mailbox class.
	 * 
	 * @param address
	 *            the address
	 * @param routingType
	 *            the routing type
	 */
	public Mailbox(String address, String routingType) {
		this(address);
		this.setRoutingType(routingType);
	}

	/**
	 * Gets the address.
	 * 
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Sets the address.
	 * 
	 * @param address
	 *            the new address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/***
	 * True if this instance is valid, false otherthise.
	 * 
	 * @return true if this instance is valid; otherwise false
	 */
	public boolean isValid() {
		return !(this.getAddress() == null || this.getAddress().isEmpty());
	}

	/**
	 * Gets  the routing type of the address used to refer to the user
	 * mailbox.
	 * 
	 * @return the routing type
	 */
	public String getRoutingType() {
		return routingType;
	}

	/**
	 * Sets the routing type.
	 * 
	 * @param routingType
	 *            the new routing type
	 */
	public void setRoutingType(String routingType) {
		this.routingType = routingType;
	}

	/**
	 * * Defines an implicit conversion between a string representing an SMTP
	 * address and Mailbox.
	 * 
	 * @param smtpAddress
	 *            the smtp address
	 * @return A Mailbox initialized with the specified SMTP address.
	 */
	public static Mailbox getMailboxFromString(String smtpAddress) {
		return new Mailbox(smtpAddress);
	}

	/**
	 * * Tries to read element from XML.
	 * 
	 * @param reader
	 *            the reader
	 * @return True if element was read.
	 * @throws Exception
	 *             the exception
	 */
	protected boolean tryReadElementFromXml(EwsServiceXmlReader reader)
			throws Exception {
		if (reader.getLocalName()
				.equalsIgnoreCase(XmlElementNames.EmailAddress)) {
			this.setAddress(reader.readElementValue());
			return true;
		} else if (reader.getLocalName().equalsIgnoreCase(
				XmlElementNames.RoutingType)) {
			this.setRoutingType(reader.readElementValue());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * * Writes elements to XML.
	 * 
	 * @param writer
	 *            the writer
	 * @throws XMLStreamException
	 *             the xML stream exception
	 * @throws ServiceXmlSerializationException
	 *             the service xml serialization exception
	 */
	protected void writeElementsToXml(EwsServiceXmlWriter writer)
			throws XMLStreamException, ServiceXmlSerializationException {
		writer.writeElementValue(XmlNamespace.Types,
				XmlElementNames.EmailAddress, this.address);
		writer.writeElementValue(XmlNamespace.Types,
				XmlElementNames.RoutingType, this.routingType);
	}

	/***
	 * Get a string representation for using this instance in a search filter.
	 * 
	 * @return String representation of instance.
	 */
	public String getSearchString() {
		return this.address;
	}

	/**
	 * Validates this instance.
	 * @throws Exception 
	 * @throws ServiceValidationException 
	 */
	@Override
    protected  void internalValidate() 
	throws ServiceValidationException, Exception {
        super.internalValidate();

        EwsUtilities.validateNonBlankStringParamAllowNull(
        		this.getAddress(), "address");
        EwsUtilities.validateNonBlankStringParamAllowNull(
        		this.getRoutingType(), "routingType");
    }


	/**
	 * * Determines whether the specified Object is equal to the current Object.
	 * 
	 * @param obj
	 *            the obj
	 * @return true if the specified Object is equal to the current Object
	 *         otherwise, false.
	 */
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		} else {
			if (!(obj instanceof Mailbox)) {
				return false;
			} else {
				Mailbox other = (Mailbox) obj;
				if (((this.address == null) && (other.address == null))
						|| ((this.address != null) && this.address
								.equalsIgnoreCase(other.address))) {
					return ((this.routingType == null) && 
							(other.routingType == null))
							|| ((this.routingType != null) && this.routingType
									.equalsIgnoreCase(other.routingType));
				} else {
					return false;
				}
			}
		}
	}

	/***
	 * Serves as a hash function for a particular type.
	 * 
	 * @return A hash code for the current object
	 */
	@Override
	public int hashCode() {
		if (!(null == this.getAddress() || this.getAddress().isEmpty())) {
			int hashCode = this.address.hashCode();

			if (!(null == this.getRoutingType() || this.getRoutingType()
					.isEmpty())) {
				hashCode ^= this.routingType.hashCode();
			}
			return hashCode;
		} else {
			return super.hashCode();
		}
	}

	/***
	 * Returns a String that represents the current Object.
	 * 
	 * @return A String that represents the current Object.
	 */
	@Override
	public String toString() {
		if (!this.isValid()) {
			return "";
		} else if (!(this.routingType == null || this.routingType.isEmpty())) {
			return this.routingType + ":" + this.address;
		} else {
			return this.address;
		}
	}
}
