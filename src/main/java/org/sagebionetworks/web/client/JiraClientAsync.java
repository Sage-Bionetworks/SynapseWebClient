package org.sagebionetworks.web.client;

import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JiraClientAsync {
	void createJiraIssue(String summary, String description, String reporter, Map<String, String> fieldValues, AsyncCallback<String> callback);
}
