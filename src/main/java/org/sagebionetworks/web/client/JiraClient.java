package org.sagebionetworks.web.client;

import java.util.Map;

import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("jira")
public interface JiraClient extends RemoteService {
	public String createJiraIssue(String summary, String description, String reporter, Map<String, String> customFieldValues) throws RestServiceException ;
}
