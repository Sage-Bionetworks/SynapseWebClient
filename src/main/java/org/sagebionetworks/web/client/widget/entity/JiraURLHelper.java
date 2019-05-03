package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JiraURLHelper {
	public void createIssueOnBackend(
			String stepsToRepro,
			Throwable t,
			String errorMessage,
			AsyncCallback<String> callback);
}