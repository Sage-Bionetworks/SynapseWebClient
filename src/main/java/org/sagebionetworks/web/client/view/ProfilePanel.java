package org.sagebionetworks.web.client.view;



import org.sagebionetworks.web.shared.BCCSignupProfile;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public	class ProfilePanel extends FormPanel {
	private TextField<String> title = null;
	private TextField<String> firstName = null;
	private TextField<String> lastName = null;
	private TextField<String> team = null;
	private TextField<String> lab = null;
	private TextField<String> organization = null;
	private TextField<String> email = null;
	private TextField<String> phone = null;
	private CheckBox bridge = null;
	
	public static final int WIDTH = 400;
	
	public ProfilePanel(BCCSignupProfile profile) {
		
		// I think "-20" means shift the RHS of the contained widgets 20 pixels left of the RHS of the panel
		FormData formData = new FormData("-20");
		
		ProfilePanel profilePanel = this;  
	    profilePanel.setFrame(true);   
	    profilePanel.setWidth(WIDTH);  
	    profilePanel.setLayout(new FlowLayout());  

	    FormLayout layout = new FormLayout();  
	    layout.setLabelWidth(75);  
	    profilePanel.setLayout(layout);  
	  
	    TextField<String> title = new TextField<String>(); 
	    profilePanel.setTitle(title);
	    title.setFieldLabel("Tile");  
	    title.setAllowBlank(true);  
        if (null!=profile.getTitle()) title.setValue(profile.getTitle());
        profilePanel.add(title, formData);  
	  
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
	  
	    TextField<String> team = new TextField<String>(); 
	    profilePanel.setTeam(team);
	    team.setFieldLabel("Team Affiliation (if any)");  
	    team.setAllowBlank(true);  
        if (null!=profile.getTeam()) team.setValue(profile.getTeam());
        profilePanel.add(team, formData);
	  
	    TextField<String> lab = new TextField<String>(); 
	    profilePanel.setLab(lab);
	    lab.setFieldLabel("Lab");  
	    lab.setAllowBlank(true);  
        if (null!=profile.getLab()) lab.setValue(profile.getLab());
        profilePanel.add(lab, formData);  
	  
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
        
        profilePanel.add(new Label(""), formData); // a blank row
        
        Label bridgeLabel = new Label("Please check the box below if you would like to have your name and team affiliation "+
        		"posted onto BRIDGE.  BRIDGE is an open growth community platform linked to Synapse, where citizen-patients, "+
        		"researchers and funders are building models of disease together.\nPlease allow a week delay before expecting to find your information posted on BRIDGE");
        bridgeLabel.setAutoWidth(false);
        bridgeLabel.setWidth(WIDTH-50);
        
        profilePanel.add(bridgeLabel, formData);
        
        profilePanel.add(new Label(""), formData); // a blank row
        
	    CheckBox bridge = new CheckBox(); 
	    bridge.setLabelSeparator("");
	    profilePanel.setBridge(bridge);
	    //bridge.setFieldLabel("Post to BRIDGE");  
        if (null!=profile.getPostToBridge()) bridge.setValue(profile.getPostToBridge());
        profilePanel.add(bridge, new FormData("-100"));       


        profilePanel.add(new Label(""), formData); // a blank row
        
        Label bridgeLabel2 = new Label("On submission of your registration we will generate an email with details of how to access contest resources. "+
        		"Please allow several minutes for this message to be generated.");
        bridgeLabel2.setAutoWidth(false);
        bridgeLabel2.setWidth(WIDTH-50);
        
        profilePanel.add(bridgeLabel2, formData);

        
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

	/**
	 * @param title the title to set
	 */
	public void setTitle(TextField<String> title) {
		this.title = title;
	}


	/**
	 * @param team the team to set
	 */
	public void setTeam(TextField<String> team) {
		this.team = team;
	}


	/**
	 * @param lab the lab to set
	 */
	public void setLab(TextField<String> lab) {
		this.lab = lab;
	}

	public void setBridge(CheckBox bridge) {
		this.bridge = bridge;
	}


	public String getFirstName() {return firstName.getValue();}
	
	public String getLastName() {return lastName.getValue();}
	
	public String getOrganization() {return organization.getValue();}
	
	public String getEmail() {return email.getValue();}
	
	public String getPhone() {return phone.getValue();}

	public String getTitle() {return title.getValue();}
	
	public String getTeam() {return team.getValue();}
	
	public String getLab() {return lab.getValue();}
	
	public Boolean getBridge() {return bridge.getValue();}

}

