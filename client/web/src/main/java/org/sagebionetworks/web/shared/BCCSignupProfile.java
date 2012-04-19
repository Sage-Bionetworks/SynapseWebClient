package org.sagebionetworks.web.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BCCSignupProfile implements Serializable, IsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8840134273627203381L;
	private String title;
	private String fname;
	private String lname;
	private String email;
	private String team;
	private String lab;
	private String organization;
	private String phone;
	private Boolean postToBridge;
	
	public static final String TITLE = "Title (optional)";
	public static final String FIRST_NAME = "First name";
	public static final String LAST_NAME = "Last name";
	public static final String EMAIL = "Email";
	public static final String TEAM = "Team Affiliation (if any)";
	public static final String LAB = "Lab";
	public static final String ORGANIZATION = "Organization";
	public static final String PHONE = "Phone";
	public static final String POST_TO_BRIDGE = "Post to BRIDGE";
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the team
	 */
	public String getTeam() {
		return team;
	}
	/**
	 * @param team the team to set
	 */
	public void setTeam(String team) {
		this.team = team;
	}
	/**
	 * @return the lab
	 */
	public String getLab() {
		return lab;
	}
	/**
	 * @param lab the lab to set
	 */
	public void setLab(String lab) {
		this.lab = lab;
	}
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
	/**
	 * @return the postToBridge
	 */
	public Boolean getPostToBridge() {
		return postToBridge;
	}
	/**
	 * @param postToBridge the postToBridge to set
	 */
	public void setPostToBridge(Boolean postToBridge) {
		this.postToBridge = postToBridge;
	}
	
	

}
