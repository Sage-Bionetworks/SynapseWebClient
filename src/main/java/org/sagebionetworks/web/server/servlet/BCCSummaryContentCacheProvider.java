package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.client.DisplayUtils;

public class BCCSummaryContentCacheProvider implements CacheProvider {
	@Override
	public String getCacheProviderId() {
		return DisplayUtils.BCC_SUMMARY_PROVIDER_ID;
	}

	@Override
	public String getValueToCache() {
	 	return RssFeedUtils.getWikiPageContent(DisplayUtils.BCC_SUMMARY_CONTENT_PAGE_ID);
	}

}
