package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.JiraClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;


public class JiraURLHelperImpl implements JiraURLHelper {
	/*
	Based on
	https://confluence.atlassian.com/display/JIRA051/Creating+Issues+via+direct+HTML+links
	A sample URL is:
	https://sagebionetworks.jira.com/secure/CreateIssueDetails!init.jspa?pid=10830&issuetype=10&summary=say+hello+world&reporter=synapse-jira-service
	 
	where the project IDs are:
	10830 - dev Synapse Governance
	10831 - Synapse Governance
	
	where the issue types are:
	10 - Flag issue type
	11 -  Access Restriction issue type
	12 -  Access Request issue type

	where the custom fields are:
	customfield_10740 Synapse Principal ID
	customfield_10741 Synapse User Display Name
	customfield_10742 Synapse Data Object
 
	 */
	
	private static final String CREATE_JIRA_ISSUE_URI = "/secure/CreateIssueDetails!init.jspa";
	private static final String ISSUE_FIELD_DESCRIPTION = "description";
	
	private static final String FLAG_ISSUE_SUMMARY = "Request for ACT to review data";
	private static final String ACCESS_RESTRCTION_ISSUE_SUMMARY = "Request for ACT to add data restriction";
	private static final String ACCESS_REQUEST_ISSUE_SUMMARY = "Request for ACT to grant access to data";
	private static final String ACCESS_REQUEST_REVOKE_ISSUE_SUMMARY = "Request for ACT to revoke access to data";
	private static final String REPORT_ABUSE_REQUEST_ISSUE_SUMMARY = "Request for ACT to review abusive content";
	
	private static final String DEFAULT_FLAG_DESCRIPTION = "By creating this issue, I wish to alert "+
	"the Synapse Access and Compliance Team that this data is posted inappropriately because...";
	private static final String DEFAULT_RESTRICTION_DESCRIPTION = "By clicking 'Create' below, I request that the Synapse ACT contact me "+
		"to assign the appropriate access restrictions for this dataset.";
	
	private static final String DEFAULT_ACCESS_DESCRIPTION = "By clicking 'Create', below, I request that the Synapse Access "+
		"and Compliance Team contact me with further information on how to access this data.";
	
	private static final String DEFAULT_REPORT_ABUSE_DESCRIPTION = "By creating this issue, I wish to alert "+
			"the Synapse team that the page above is in violation (for example: abusive or harmful content, spam, inappropriate ads).\n"
			+ "Please provide additional information about the issue that you’re reporting:\n\n";
	
	private static final String DEFAULT_REVOKE_ACCESS_DESCRIPTION = "By creating this issue, I request that the Synapse Access "+
			"and Compliance Team revoke access to this dataset.\n"
			+ "Please provide the list of Synapse users who should no longer have access:\n\n\n\n\n\n\n\n";
	
	/**
	 * properties used in the interface to Jira for Governance
	 * 
	 */
	public static String CONFLUENCE_ENDPOINT = "org.sagebionetworks.jira.governance.confluence.endpoint";
	public static String FLAG_ISSUE_TYPE = "org.sagebionetworks.jira.governance.flag.issue.type";
	public static String ACCESS_RESTRCTION_ISSUE_TYPE = "org.sagebionetworks.jira.governance.access.restriction.issue.type";
	public static String ACCESS_REQUEST_ISSUE_TYPE = "org.sagebionetworks.jira.governance.access.request.issue.type";
	public static String DEFAULT_ISSUE_REPORTER = "org.sagebionetworks.jira.governance.default.issue.reporter";
	public static String ISSUE_FIELD_PRINCIPAL_ID = "org.sagebionetworks.jira.governance.issue.field.principal.id";
	public static String ISSUE_FIELD_USER_DISPLAY_NAME = "org.sagebionetworks.jira.governance.issue.field.user.display.name";
	public static String ISSUE_FIELD_DATA_OBJECT_ID = "org.sagebionetworks.jira.governance.issue.field.data.object.id";
	public static String ISSUE_FIELD_USER_EMAIL = "org.sagebionetworks.jira.governance.issue.field.user.email";
	public static String ISSUE_FIELD_ACCESS_REQUIREMENT_OBJECT_ID = "org.sagebionetworks.jira.governance.issue.field.access.requirement.object.id";
	public static String MAJOR_PRIORITY = "org.sagebionetworks.jira.governance.major.priority"; 

	private static String confluence_endpoint;
	private static int flag_issue_type;
	private static int access_restriction_type;
	private static int access_request_issue_type;
	private static String default_issue_reporter;
	private static String issue_field_principal_id;
	private static String issue_field_user_display_name;
	private static String issue_field_data_object_id;
	private static String issue_field_user_email;
	private static String issue_field_access_requirement_object_id;
	private static int major_priority; 

	private int jiraProjectId;
	public JiraClientAsync jiraClient;
	public GWTWrapper gwt;
	public AuthenticationController authenticationController;
	
	@Inject
	public JiraURLHelperImpl(
			JiraGovernanceConstants constants, 
			JiraClientAsync jiraClient,
			GWTWrapper gwt, 
			AuthenticationController authenticationController
			) {
		this.jiraClient = jiraClient;
		fixServiceEntryPoint(jiraClient);
		this.gwt = gwt;
		this.authenticationController = authenticationController;
		jiraProjectId = constants.jiraProjectId();
		confluence_endpoint = constants.confluenceEndpoint();
		flag_issue_type = constants.flagIssueType();
		access_restriction_type = constants.accessRestrictionType();
		access_request_issue_type = constants.accessRequestIssueType();
		default_issue_reporter = constants.defaultIssueReporter();
		issue_field_principal_id = constants.issueFieldPrincipalId();
		issue_field_user_display_name = constants.issueFieldUserDisplayName();
		issue_field_data_object_id = constants.issueFieldDataObjectId();
		issue_field_user_email = constants.issueFieldUserEmail();
		issue_field_access_requirement_object_id = constants.issueFieldAccessRequirementObjectId();
		major_priority = constants.majorPriority(); 
	}
	


	public static String createIssueURL(
			int projectId, // required
			int issueType, // required
			String summary, // required
			String reporter, // required
			String description, // optional
			String principalId, // optional
			String userDisplayName, // optional
			String userEmailAddress, // optional
			String dataObjectId, // optional
			String requirementObjectId // optional
  			) {
		StringBuilder sb = new StringBuilder();
		sb.append(confluence_endpoint);
		sb.append(CREATE_JIRA_ISSUE_URI);
		sb.append("?pid="+projectId);
		sb.append("&issuetype="+issueType);
		sb.append("&summary="+summary);
		sb.append("&reporter="+reporter);
		sb.append("&priority="+major_priority);
		if (description!=null) sb.append("&"+ISSUE_FIELD_DESCRIPTION+"="+description);
		if (principalId!=null) sb.append("&"+issue_field_principal_id+"="+principalId);
		if (userDisplayName!=null) sb.append("&"+issue_field_user_display_name+"="+userDisplayName);
		if (userEmailAddress!=null) sb.append("&"+issue_field_user_email+"="+userEmailAddress);
		if (dataObjectId!=null) sb.append("&"+issue_field_data_object_id+"="+dataObjectId);
		if (requirementObjectId!=null) sb.append("&"+issue_field_access_requirement_object_id+"="+requirementObjectId);
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.sagebionetworks.web.client.widget.entity.JiraURLHelper#createFlagIssue(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String createFlagIssue(String userEmailAddress,
			String userDisplayName,
			String dataObjectId) {
		return createIssueURL(jiraProjectId, 
				flag_issue_type, 
				FLAG_ISSUE_SUMMARY, 
				default_issue_reporter, 
				DEFAULT_FLAG_DESCRIPTION,
				null, 
				userDisplayName, 
				userEmailAddress,
				dataObjectId,
				null);
	}
	
	/* (non-Javadoc)
	 * @see org.sagebionetworks.web.client.widget.entity.JiraURLHelper#createAccessRestrictionIssue(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String createAccessRestrictionIssue(String userEmailAddress,
			String userDisplayName,
			String dataObjectId) {
		return createIssueURL(jiraProjectId, 
				access_restriction_type, 
				ACCESS_RESTRCTION_ISSUE_SUMMARY, 
				default_issue_reporter, 
				DEFAULT_RESTRICTION_DESCRIPTION,
				null, 
				userDisplayName, 
				userEmailAddress,
				dataObjectId,
				null);		
	}
	
	/* (non-Javadoc)
	 * @see org.sagebionetworks.web.client.widget.entity.JiraURLHelper#createRequestAccessIssue(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String createRequestAccessIssue(String principalId,
			String userDisplayName,
			String userEmailAddress,
			String dataObjectId,
			String accessRequirementId) {
		return createIssueURL(jiraProjectId, 
				access_request_issue_type, 
				ACCESS_REQUEST_ISSUE_SUMMARY, 
				default_issue_reporter, 
				DEFAULT_ACCESS_DESCRIPTION,
				principalId, 
				userDisplayName, 
				userEmailAddress,
				dataObjectId,
				accessRequirementId);		
	}
	
	@Override
	public String createReportAbuseIssueURL() {
		String principalId = "";
		String userDisplayName = "";
		String userEmailAddress = "";
		if (authenticationController.isLoggedIn()) {
	    	UserProfile profile = authenticationController.getCurrentUserProfile();
	    	principalId = authenticationController.getCurrentUserPrincipalId();
	    	userDisplayName = DisplayUtils.getDisplayName(profile);
	    	userEmailAddress = DisplayUtils.getPrimaryEmail(profile);
	    }
		String currentURL = gwt.getCurrentURL();
		return createIssueURL(jiraProjectId, 
				flag_issue_type, 
				REPORT_ABUSE_REQUEST_ISSUE_SUMMARY, 
				default_issue_reporter, 
				gwt.encodeQueryString(currentURL + "\n\n" + DEFAULT_REPORT_ABUSE_DESCRIPTION),
				principalId, 
				userDisplayName, 
				userEmailAddress,
				null,
				null);
	}
	
	@Override
	public String createRevokeAccessIssue(String principalId,
			String userDisplayName,
			String userEmailAddress,
			String accessRequirementId) {
		return createIssueURL(jiraProjectId, 
				flag_issue_type, 
				ACCESS_REQUEST_REVOKE_ISSUE_SUMMARY, 
				default_issue_reporter, 
				URL.encodeQueryString(DEFAULT_REVOKE_ACCESS_DESCRIPTION + "(https://www.synapse.org/#!ACTDataAccessSubmissions:AR_ID=" + accessRequirementId + ")"),
				principalId, 
				userDisplayName, 
				userEmailAddress,
				null,
				accessRequirementId);
	}
	
	@Override
	public void createIssueOnBackend(
			String stepsToRepro,
			Throwable t,
			String errorMessage,
			AsyncCallback<String> callback) {
		Map<String, String> fieldValues = new HashMap<String, String>();
		
		String stackTrace = DisplayUtils.getStackTrace(t);
		
		StringBuilder description = new StringBuilder(); 
		
	    if (authenticationController.isLoggedIn()) {
	    	UserProfile profile = authenticationController.getCurrentUserProfile();
	    	description.append("Submitted by " + DisplayUtils.getDisplayName(profile));
	    }
	    description.append("\n\n" + stepsToRepro);
	    description.append("\n\n" + gwt.getUserAgent());
	    description.append("\n\n" + gwt.getAppVersion());
	    description.append("\n\n" + gwt.getCurrentURL());
	    description.append("\n\n" + t.getMessage());
	    description.append("\n" + stackTrace);
	    jiraClient.createJiraIssue(errorMessage, description.toString(), default_issue_reporter, fieldValues, callback);
	}

}
