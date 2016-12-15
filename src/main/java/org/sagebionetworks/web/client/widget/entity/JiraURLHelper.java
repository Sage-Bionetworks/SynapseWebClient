package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JiraURLHelper {

	public String createFlagIssue(String userEmailAddress,
			String userDisplayName, String dataObjectId);

	public String createAccessRestrictionIssue(String userEmailAddress,
			String userDisplayName, String dataObjectId);

	public String createRequestAccessIssue(String principalId,
			String userDisplayName, String userEmailAddress,
			String dataObjectId, String accessRequirementId);

	public String createReportAbuseIssueURL();

	public void createIssueOnBackend(
			String stepsToRepro,
			Throwable t,
			String errorMessage,
			AsyncCallback<String> callback);

	
}