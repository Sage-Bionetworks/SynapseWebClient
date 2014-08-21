package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.client.ClientProperties;

public class NewsFeedCacheProvider implements CacheProvider {
	@Override
	public String getCacheProviderId() {
		return ClientProperties.NEWS_FEED_PROVIDER_ID;
	}

	@Override
	public String getValueToCache() {
		return RssFeedUtils.fixNewsFeed(RssFeedUtils.getFeedData(ClientProperties.NEWS_FEED_URL, 4, true));
	}

}
