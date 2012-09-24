package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.i18n.client.Constants;

public interface JiraGovernanceConstants extends Constants {
	
	String confluenceEndpoint();
	
	int flagIssueType();
	
	int accessRestrictionType();
	
	int accessRequestIssueType();
	
	String defaultIssueReporter();
	
	String issueFieldPrincipalId();
	
	String issueFieldUserDisplayName();
	
	String issueFieldDataObjectId();
	
	String issueFieldUserEmail();
	
	String issueFieldAccessRequirementObjectId();
	
	int majorPriority();
	
}
