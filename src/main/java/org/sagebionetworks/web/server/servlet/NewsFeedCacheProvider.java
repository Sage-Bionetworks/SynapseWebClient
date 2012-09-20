package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.client.DisplayUtils;

public class NewsFeedCacheProvider implements CacheProvider {
	@Override
	public String getCacheProviderId() {
		return DisplayUtils.NEWS_FEED_PROVIDER_ID;
	}

	@Override
	public String getCacheValue() {
	 	return RssFeedUtils.getFeedData(DisplayUtils.NEWS_FEED_URL, 4, true);
	}

}
