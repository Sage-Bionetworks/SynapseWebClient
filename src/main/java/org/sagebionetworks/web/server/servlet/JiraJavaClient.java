package org.sagebionetworks.web.server.servlet;

import com.atlassian.jira.rest.client.IssueRestClient;

public interface JiraJavaClient {
	IssueRestClient getIssueClient();
}
