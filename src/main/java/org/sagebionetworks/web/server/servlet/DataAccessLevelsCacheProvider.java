package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.client.DisplayUtils;

public class DataAccessLevelsCacheProvider implements CacheProvider {
	@Override
	public String getCacheProviderId() {
		return DisplayUtils.DATA_ACCESS_LEVELS_PROVIDER_ID;
	}

	@Override
	public String getValueToCache() {
	 	return RssFeedUtils.getWikiPageContent(DisplayUtils.DATA_ACCESS_LEVELS_CONTENT_PAGE_ID);
	}

}
