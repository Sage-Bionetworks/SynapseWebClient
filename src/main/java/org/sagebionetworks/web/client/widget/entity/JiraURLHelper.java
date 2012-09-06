package org.sagebionetworks.web.client.widget.entity;

public class JiraURLHelper {
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

	where the custom fields are:
	customfield_10740 Synapse Principal ID
	customfield_10741 Synapse User Display Name
	customfield_10742 Synapse Data Object
 
	 */
	
	private static final String CONFLUENCE_ENDPOINT = "https://sagebionetworks.jira.com";
	private static final String CREATE_JIRA_ISSUE_URI = "/secure/CreateIssueDetails!init.jspa";
	private static final int FLAG_ISSUE_TYPE = 10;
	private static final int ACCESS_RESTRCTION_ISSUE_TYPE = 11;
	private static final String FLAG_ISSUE_SUMMARY = "Request for ACT to review data";
	private static final String ACCESS_RESTRCTION_ISSUE_SUMMARY = "Request for ACT to add data restriction";
	private static final String DEFAULT_ISSUE_REPORTER = "synapse-jira-service";
	private static final String ISSUE_FIELD_PRINCIPAL_ID = "customfield_10740";
	private static final String ISSUE_FIELD_USER_DISPLAY_NAME = "customfield_10741";
	private static final String ISSUE_FIELD_DATA_OBJECT_ID = "customfield_10742";
	private static final int MAJOR_PRIORITY = 3; // priorities are blocker=1, critical=2, major=3, ...
	
	public static String createIssueURL(
			int projectId, // required
			int issueType, // required
			String summary, // required
			String reporter, // required
			String principalId, // optional
			String userDisplayName, // optional
			String dataObjectId // optional
			) {
		StringBuilder sb = new StringBuilder();
		sb.append(CONFLUENCE_ENDPOINT);
		sb.append(CREATE_JIRA_ISSUE_URI);
		sb.append("?pid="+projectId);
		sb.append("&issuetype="+issueType);
		sb.append("&summary="+summary);
		sb.append("&reporter="+reporter);
		sb.append("&priority="+MAJOR_PRIORITY);
		if (principalId!=null) sb.append("&"+ISSUE_FIELD_PRINCIPAL_ID+"="+principalId);
		if (userDisplayName!=null) sb.append("&"+ISSUE_FIELD_USER_DISPLAY_NAME+"="+userDisplayName);
		if (dataObjectId!=null) sb.append("&"+ISSUE_FIELD_DATA_OBJECT_ID+"="+dataObjectId);
		return sb.toString();
	}
	
	public static String createFlagIssue(String principalId,
			String userDisplayName,
			String dataObjectId) {
		int projectId = 10830; // get from config file
		return createIssueURL(projectId, 
				FLAG_ISSUE_TYPE, 
				FLAG_ISSUE_SUMMARY, 
				DEFAULT_ISSUE_REPORTER, 
				principalId, 
				userDisplayName, 
				dataObjectId);
	}
	
	public static String createAccessRestrictionIssue(String principalId,
			String userDisplayName,
			String dataObjectId) {
		int projectId = 10830; // get from config file
		return createIssueURL(projectId, 
				ACCESS_RESTRCTION_ISSUE_TYPE, 
				ACCESS_RESTRCTION_ISSUE_SUMMARY, 
				DEFAULT_ISSUE_REPORTER, 
				principalId, 
				userDisplayName, 
				dataObjectId);		
	}
	

}
