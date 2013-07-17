package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.client.ClientProperties;

public class DataAccessLevelsCacheProvider implements CacheProvider {
	@Override
	public String getCacheProviderId() {
		return ClientProperties.DATA_ACCESS_LEVELS_PROVIDER_ID;
	}

	@Override
	public String getValueToCache() {
	 	return RssFeedUtils.getWikiPageSourceContent(ClientProperties.DATA_ACCESS_LEVELS_CONTENT_PAGE_ID);
	}

}
