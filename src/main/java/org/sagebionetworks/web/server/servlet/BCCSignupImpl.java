package org.sagebionetworks.web.server.servlet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.bccsetup.SpreadsheetHelper;
import org.sagebionetworks.utils.EmailUtils;
import org.sagebionetworks.web.client.BCCSignup;
import org.sagebionetworks.web.shared.BCCSignupProfile;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class BCCSignupImpl extends RemoteServiceServlet implements BCCSignup {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8044805098904784489L;

	private static final String APPROVAL_EMAIL_ADDRESS = StackConfiguration.getBCCApprovalEmail();

	public static final String EMAIL_SUBJECT = "Request for NDA for Cancer Challenge 2012";
	
	public BCCSignupImpl() {
	}
	
	// per the requirements there must be no space characters
	public static String signupEmailMessage(BCCSignupProfile profile) {
		return "{\"FirstName\":\""+profile.getFname().trim()+
				"\",\n\"LastName\":\""+profile.getLname().trim()+
				"\",\n\"Organization\":\""+profile.getOrganization().trim()+
				"\",\n\"ContactEmail\":\""+profile.getEmail().trim()+
				"\",\n\"ContactPhone\":\""+profile.getPhone().trim()+"\"}";
	}
	
	private static final String BRIDGE_SPREADSHEET_TITLE = StackConfiguration.getBridgeSpreadsheetTitle();
	
	public static void addBRIDGESpreadsheetRecord(BCCSignupProfile profile) {
		Map<String,String> data = new HashMap<String,String>();
		data.put("Title", profile.getTitle());
		data.put("FirstName", profile.getFname());
		data.put("LastName", profile.getLname());
		data.put("Team", profile.getTeam());
		data.put("Lab", profile.getLab());
		data.put("Organization", profile.getOrganization());
		data.put("Email", profile.getEmail());
		data.put("Phone", profile.getPhone());
		data.put("PostToBRIDGE", profile.getPostToBridge().toString());
		
		SpreadsheetHelper ssh = new SpreadsheetHelper();
		ssh.addSpreadsheetRow(BRIDGE_SPREADSHEET_TITLE, data);
	}

	private static final String DEFAULT_SIGNUP_SPREADSHEET_TITLE = "BCC Registrants";
	
	private static String SIGNUP_SPREADSHEET_TITLE = DEFAULT_SIGNUP_SPREADSHEET_TITLE;
	{
		String signupSpreadSheetTitle = System.getProperty("org.sagebionetworks.signup.spreadsheet.title");
		if (signupSpreadSheetTitle!=null && signupSpreadSheetTitle.length()>0) {
			SIGNUP_SPREADSHEET_TITLE = signupSpreadSheetTitle;
		}
	}

	// ex.: Thu, 14 Jun 2012 15:05:34 -0700 (PDT)
	private static final String DATE_FORMAT = "EEE MMM dd hh:mm:ss yyyy";
	
	public static void addSignupSpreadsheetRecord(BCCSignupProfile profile) {
		Map<String,String> data = new HashMap<String,String>();
		data.put("First Name", profile.getFname());
		data.put("Last Name", profile.getLname());
		data.put("Organization Name", profile.getOrganization());
		data.put("Contact Email", profile.getEmail());
		data.put("Contact phone", profile.getPhone());
		DateFormat df = new SimpleDateFormat(DATE_FORMAT);
		data.put("Date request received", df.format(new Date()));
		
		SpreadsheetHelper ssh = new SpreadsheetHelper();
		ssh.addSpreadsheetRow(SIGNUP_SPREADSHEET_TITLE, data);
	}

	
	@Override
	public void sendSignupEmail(BCCSignupProfile profile) {
			// per SWC-138 we discontinue sending the email and instead write directly into the BCC Signup spreadsheet
			//EmailUtils.sendMail(APPROVAL_EMAIL_ADDRESS, EMAIL_SUBJECT, signupEmailMessage(profile));
			addSignupSpreadsheetRecord(profile);
			addBRIDGESpreadsheetRecord(profile);
	}

}
