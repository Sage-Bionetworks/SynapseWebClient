package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.client.DisplayUtils;

public class SupportFeedCacheProvider implements CacheProvider {
	@Override
	public String getCacheProviderId() {
		return DisplayUtils.SUPPORT_FEED_PROVIDER_ID;
	}

	@Override
	public String getCacheValue() {
	 	return RssFeedUtils.getFeedData(DisplayUtils.SUPPORT_FEED_URL, 5, false);
	}

}
