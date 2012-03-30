package org.sagebionetworks.web.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BCCSignupProfile implements Serializable, IsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8840134273627203381L;
	private String fname;
	private String lname;
	private String email;
	private String organization;
	private String phone;
	
	public static final String FIRST_NAME = "First name";
	public static final String LAST_NAME = "Last name";
	public static final String EMAIL = "Email";
	public static final String ORGANIZATION = "Organization";
	public static final String PHONE = "Phone";
	
	/**
	 * @return the fname
	 */
	public String getFname() {
		return fname;
	}
	/**
	 * @param fname the fname to set
	 */
	public void setFname(String fname) {
		this.fname = fname;
	}
	/**
	 * @return the lname
	 */
	public String getLname() {
		return lname;
	}
	/**
	 * @param lname the lname to set
	 */
	public void setLname(String lname) {
		this.lname = lname;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}
	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}
	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	

}
