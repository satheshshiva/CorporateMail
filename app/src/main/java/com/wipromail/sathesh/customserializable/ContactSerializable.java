package com.wipromail.sathesh.customserializable;

import android.util.Log;

import com.wipromail.sathesh.constants.Constants;
import com.wipromail.sathesh.service.data.Contact;
import com.wipromail.sathesh.service.data.PhoneNumberKey;
import com.wipromail.sathesh.service.data.PhysicalAddressKey;
import com.wipromail.sathesh.service.data.ServiceLocalException;

import java.io.Serializable;

public class ContactSerializable implements Serializable, Constants{


	private static final long serialVersionUID = 1L;
	private String displayName="";
	private String email="";
	private String department="";
	private String companyName="";
	private String designation="";
	private String workphone="";
	private String mobilephone="";
	private String fax="";
	private String officeLocation_street="";
	private String officeLocation_city="";
	private String officeLocation_state="";
	private String officeLocation_countryOrRegion="";
	private String officeLocation_postalCode="";
    private boolean resolveOnLoad=false;        //if set to true then the contact will be resolved on load on opening the contact
	private boolean tryResolveNamesInDirectory=false;		//if set to true then the contact will be resolved on ComposeActivity

	
	public ContactSerializable(){
	}
	
	public ContactSerializable(String email, String displayName, boolean tryResolveNamesInDirectory){
		this.email = email;
		this.displayName = displayName;
		this.tryResolveNamesInDirectory = tryResolveNamesInDirectory;
	}

	public static ContactSerializable  getContactSerializableFromContact(Contact contact, String email) throws ServiceLocalException {
		ContactSerializable sContact = new ContactSerializable();
		if(contact!=null){
		sContact.setDisplayName(contact.getDisplayName());
		sContact.setEmail(email);
		sContact.setDepartment(contact.getDepartment());
		sContact.setCompanyName(contact.getCompanyName());
		sContact.setDesignation(contact.getJobTitle());
		if(contact.getPhoneNumbers()!=null ){
			//Setting Business phone (work phone)
			try {
				if(contact.getPhoneNumbers().getPhoneNumber(PhoneNumberKey.BusinessPhone) !=null){

					sContact.setWorkphone(contact.getPhoneNumbers().getPhoneNumber(PhoneNumberKey.BusinessPhone));
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
			//Setting mobile phone
			try {
				if(contact.getPhoneNumbers().getPhoneNumber(PhoneNumberKey.MobilePhone) !=null){

					sContact.setMobilephone(contact.getPhoneNumbers().getPhoneNumber(PhoneNumberKey.MobilePhone));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			//Setting fax
			try {
				if(contact.getPhoneNumbers().getPhoneNumber(PhoneNumberKey.BusinessFax) !=null){

					sContact.setFax(contact.getPhoneNumbers().getPhoneNumber(PhoneNumberKey.BusinessFax));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
		//Setting office location
		if(contact.getPhysicalAddresses()!=null ){
			if(contact.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business) !=null){
				
				try {
					sContact.setOfficeLocation_street(contact.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getStreet());
					sContact.setOfficeLocation_city(contact.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getCity());
					sContact.setOfficeLocation_state(contact.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getState());
					sContact.setOfficeLocation_countryOrRegion(contact.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getCountryOrRegion());
					sContact.setOfficeLocation_postalCode(contact.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getPostalCode());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		}
		else{
			//contact is null
			Log.d(TAG, "ContactSerializable -> Given contact is null. Hence populating email id as display name");
			sContact.setDisplayName(email);
			sContact.setEmail(email);
		}
		return sContact;
	}

	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobilephone() {
		return mobilephone;
	}
	public void setMobilephone(String mobilephone) {
		this.mobilephone = mobilephone;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getWorkphone() {
		return workphone;
	}

	public void setWorkphone(String workphone) {
		this.workphone = workphone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	

	public String getOfficeLocation_street() {
		return officeLocation_street;
	}

	public void setOfficeLocation_street(String officeLocation_street) {
		this.officeLocation_street = officeLocation_street;
	}

	public String getOfficeLocation_city() {
		return officeLocation_city;
	}

	public void setOfficeLocation_city(String officeLocation_city) {
		this.officeLocation_city = officeLocation_city;
	}

	public String getOfficeLocation_state() {
		return officeLocation_state;
	}

	public void setOfficeLocation_state(String officeLocation_state) {
		this.officeLocation_state = officeLocation_state;
	}

	public String getOfficeLocation_countryOrRegion() {
		return officeLocation_countryOrRegion;
	}

	public void setOfficeLocation_countryOrRegion(
			String officeLocation_countryOrRegion) {
		this.officeLocation_countryOrRegion = officeLocation_countryOrRegion;
	}

	public String getOfficeLocation_postalCode() {
		return officeLocation_postalCode;
	}

	public void setOfficeLocation_postalCode(String officeLocation_postalCode) {
		this.officeLocation_postalCode = officeLocation_postalCode;
	}

	public boolean isTryResolveNamesInDirectory() {
		return tryResolveNamesInDirectory;
	}

	public void setTryResolveNamesInDirectory(boolean tryResolveNamesInDirectory) {
		this.tryResolveNamesInDirectory = tryResolveNamesInDirectory;
	}

    public boolean isResolveOnLoad() {
        return resolveOnLoad;
    }

    public void setResolveOnLoad(boolean resolveOnLoad) {
        this.resolveOnLoad = resolveOnLoad;
    }

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContactSerializable [displayName=");
		builder.append(displayName);
		builder.append(", email=");
		builder.append(email);
		builder.append(", department=");
		builder.append(department);
		builder.append(", companyName=");
		builder.append(companyName);
		builder.append(", designation=");
		builder.append(designation);
		builder.append(", workphone=");
		builder.append(workphone);
		builder.append(", mobilephone=");
		builder.append(mobilephone);
		builder.append(", fax=");
		builder.append(fax);
		builder.append(", officeLocation_street=");
		builder.append(officeLocation_street);
		builder.append(", officeLocation_city=");
		builder.append(officeLocation_city);
		builder.append(", officeLocation_state=");
		builder.append(officeLocation_state);
		builder.append(", officeLocation_countryOrRegion=");
		builder.append(officeLocation_countryOrRegion);
		builder.append(", officeLocation_postalCode=");
		builder.append(officeLocation_postalCode);
		builder.append(", tryResolveNamesInDirectory=");
		builder.append(tryResolveNamesInDirectory);
		builder.append("]");
		return builder.toString();
	}


}
