package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("rss")
public interface RssService extends RemoteService {

	public String getAllFeedData(String url) throws RestServiceException;
	public String getFeedData(String url, Integer limit, boolean summariesOnly) throws RestServiceException;
	public String getPageContent(String urlString);
}