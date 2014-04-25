package org.sagebionetworks.web.server.servlet;

import java.net.URI;
import java.net.URISyntaxException;

import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalPropertiesHolder;
import org.sagebionetworks.web.shared.WebConstants;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

public class JiraJavaClientImpl implements JiraJavaClient {
	private JiraRestClient jiraRestClient;
	
	public JiraJavaClientImpl() {
		initJiraRestClient();	
	}
	
	public void initJiraRestClient() {
		String jiraBaseURL = PortalPropertiesHolder.getProperty(WebConstants.CONFLUENCE_ENDPOINT);
		try {
			final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
			URI jiraServerUri = new URI(jiraBaseURL);
			AnonymousAuthenticationHandler anonymousAuthHandler = new AnonymousAuthenticationHandler();
			jiraRestClient = factory.create(jiraServerUri, anonymousAuthHandler);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid URL in properties: " + jiraBaseURL);
		}
	}
	
	@Override
	public IssueRestClient getIssueClient() {
		return jiraRestClient.getIssueClient();
	}
	

}
