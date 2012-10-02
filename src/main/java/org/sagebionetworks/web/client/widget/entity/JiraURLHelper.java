package org.sagebionetworks.web.client.widget.entity;

public interface JiraURLHelper {

	public String createFlagIssue(String userEmailAddress,
			String userDisplayName, String dataObjectId);

	public String createAccessRestrictionIssue(String userEmailAddress,
			String userDisplayName, String dataObjectId);

	public String createRequestAccessIssue(String principalId,
			String userDisplayName, String userEmailAddress,
			String dataObjectId, String accessRequirementId);

}