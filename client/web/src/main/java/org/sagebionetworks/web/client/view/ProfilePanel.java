package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.shared.BCCSignupProfile;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public	class ProfilePanel extends FormPanel {
	private TextField<String> firstName = null;
	private TextField<String> lastName = null;
	private TextField<String> organization = null;
	private TextField<String> email = null;
	private TextField<String> phone = null;
	
	public ProfilePanel(BCCSignupProfile profile) {
		FormData formData = new FormData("-20");
		
		ProfilePanel profilePanel = this;  
	    profilePanel.setFrame(true);   
	    profilePanel.setWidth(350);  
	    profilePanel.setLayout(new FlowLayout());  

	    FormLayout layout = new FormLayout();  
	    layout.setLabelWidth(75);  
	    profilePanel.setLayout(layout);  
	  
	    TextField<String> firstName = new TextField<String>(); 
	    profilePanel.setFirstName(firstName);
	    firstName.setFieldLabel("First Name");  
	    firstName.setAllowBlank(false);  
        if (null!=profile.getFname()) firstName.setValue(profile.getFname());
        profilePanel.add(firstName, formData);  
	  
	    TextField<String> lastName = new TextField<String>();  
	    profilePanel.setLastName(lastName);
	    lastName.setFieldLabel("Last Name");  
	    lastName.setAllowBlank(false);  
	    if (null!=profile.getLname()) lastName.setValue(profile.getLname());
	    profilePanel.add(lastName, formData);  
	  
	    TextField<String> organization = new TextField<String>(); 
	    profilePanel.setOrganization(organization);
	    organization.setFieldLabel("Organization");  
	    organization.setAllowBlank(false);  
        if (null!=profile.getOrganization()) organization.setValue(profile.getOrganization());
        profilePanel.add(organization, formData);  
	  
	    TextField<String> email = new TextField<String>();  
	    profilePanel.setEmail(email);
	    email.setFieldLabel("Email");  
	    email.setAllowBlank(false);  
	    email.setRegex("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
        if (null!=profile.getEmail()) email.setValue(profile.getEmail());
        profilePanel.add(email, formData);  
	  
	    TextField<String> phone = new TextField<String>(); 
	    profilePanel.setPhone(phone);
	    phone.setFieldLabel("Phone");  
	    phone.setAllowBlank(false);  
	    phone.setRegex("^([0-9\\(\\)\\/\\+ \\-]*)$");
	    if (null!=profile.getEmail()) phone.setValue(profile.getPhone());
        profilePanel.add(phone, formData);  
	}		
	
	
	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(TextField<String> firstName) {
		this.firstName = firstName;
	}



	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(TextField<String> lastName) {
		this.lastName = lastName;
	}



	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(TextField<String> organization) {
		this.organization = organization;
	}



	/**
	 * @param email the email to set
	 */
	public void setEmail(TextField<String> email) {
		this.email = email;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(TextField<String> phone) {
		this.phone = phone;
	}

	public String getFirstName() {return firstName.getValue();}
	
	public String getLastName() {return lastName.getValue();}
	
	public String getOrganization() {return organization.getValue();}
	
	public String getEmail() {return email.getValue();}
	
	public String getPhone() {return phone.getValue();}


}

