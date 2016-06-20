package org.sagebionetworks.web.server.servlet;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.sagebionetworks.web.client.JiraClient;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalPropertiesHolder;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.util.concurrent.Promise;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

public class JiraClientImpl extends RemoteServiceServlet implements JiraClient {
	public static final int SUMMARY_MAX_SIZE = 250;

	private static Logger logger = Logger.getLogger(JiraClientImpl.class.getName());
		
	private JiraJavaClient jiraJavaClient;
		
	@Inject
	public void setJiraJavaClient(JiraJavaClient jiraJavaClient) {
		this.jiraJavaClient = jiraJavaClient;
	}
	
	@Override
	public String createJiraIssue(
			String summary, 
			String description,
			String reporter, 
			Map<String, String> customFieldValues)
			throws RestServiceException {
		validateService();
		String projectID = PortalPropertiesHolder.getProperty(WebConstants.JIRA_PROJECT_ID);
		String projectKey = PortalPropertiesHolder.getProperty(WebConstants.JIRA_PROJECT_KEY);
		IssueRestClient issueClient = jiraJavaClient.getIssueClient();
		IssueInputBuilder builder = new IssueInputBuilder(projectID, 1L); // 1=bug
		builder.setProjectKey(projectKey);
		//newlines are not allowed in the summary
		// SWC-3127: summary must be < 255 characters
		if (summary.length() > SUMMARY_MAX_SIZE) {
			description = summary + "\n\n\n"+ description;
			summary = StringUtils.abbreviate(summary, SUMMARY_MAX_SIZE);
		}
		builder.setSummary(summary.replaceAll("\n", " "));
		builder.setDescription(description);
		builder.setReporterName(reporter);
		for (String fieldKey : customFieldValues.keySet()) {
			builder.setFieldValue(fieldKey, customFieldValues.get(fieldKey));
		}

		Promise<BasicIssue> promise = issueClient.createIssue(builder.build());
		try {
			BasicIssue createdIssue = promise.get();
			return createdIssue.getKey();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	/**
	 * Validate that the service is ready to go.
	 */
	public void validateService() {
		if (jiraJavaClient == null)
			throw new IllegalStateException(
					"The Jira java client was not bound properly");
	}
}
