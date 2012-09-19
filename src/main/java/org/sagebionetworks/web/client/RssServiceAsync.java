package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RssServiceAsync {
	
	/**
	 * Return all feed data (as an html string)
	 * @param url
	 * @param callback
	 * @throws RestServiceException
	 */
	void getAllFeedData(String url,	AsyncCallback<String> callback);
	
	/**
	 * Return the top n items from the feed (or max available if less than the given limit) in html.  Will give back only the short descriptions if summariesOnly is true.
	 * @param url
	 * @param limit
	 * @param callback
	 * @throws RestServiceException
	 */
	void getFeedData(String url, Integer limit, boolean summariesOnly,	AsyncCallback<String> callback);
	
	/**
	 * return the content from the wiki
	 * @param urlString
	 * @param callback
	 */
	void getWikiPageContent(String pageId, AsyncCallback<String> callback);
	
	/**
	 * return the source content from the wiki
	 * @param urlString
	 * @param callback
	 */
	void getWikiPageSourceContent(String pageId, AsyncCallback<String> callback);
	
	/**
	 * return the source content from the wiki. 
	 * WARNING: caching is not set up for this page, load time could be high.
	 * @param urlString
	 * @param callback
	 */
	void getUncachedWikiPageSourceContent(String pageId, AsyncCallback<String> callback);
}