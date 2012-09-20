package org.sagebionetworks.web.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("rss")
public interface RssService extends RemoteService {



	public String getCachedContent(String cacheproviderId);
}