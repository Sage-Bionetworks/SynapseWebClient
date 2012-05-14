package org.sagebionetworks.web.server.servlet;

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
	
	public static void addSpreadsheetRecord(BCCSignupProfile profile) {
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

	
	@Override
	public void sendSignupEmail(BCCSignupProfile profile) {
			EmailUtils.sendMail(APPROVAL_EMAIL_ADDRESS, EMAIL_SUBJECT, signupEmailMessage(profile));
			addSpreadsheetRecord(profile);
	}

}
