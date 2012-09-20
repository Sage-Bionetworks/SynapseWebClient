package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RssServiceAsync {
	
	/**
	 * Return the cached external data (that must have an associated registered CacheProvider)
	 * @param cacheproviderId
	 * @param callback
	 * @throws RestServiceException
	 */
	void getCachedContent(String cacheproviderId, AsyncCallback<String> callback);
}