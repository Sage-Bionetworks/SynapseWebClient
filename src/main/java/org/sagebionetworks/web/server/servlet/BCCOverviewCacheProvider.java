package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.client.DisplayUtils;

public class BCCOverviewCacheProvider implements CacheProvider {
	@Override
	public String getCacheProviderId() {
		return DisplayUtils.BCC_OVERVIEW_CONTENT_PROVIDER_ID;
	}

	@Override
	public String getCacheValue() {
	 	return RssFeedUtils.getWikiPageSourceContent(DisplayUtils.BCC_CONTENT_PAGE_ID);
	}

}
