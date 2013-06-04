package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.web.client.DisplayUtils;

public class ChallengeOverviewCacheProvider implements CacheProvider {
	@Override
	public String getCacheProviderId() {
		return DisplayUtils.CHALLENGE_OVERVIEW_CONTENT_PROVIDER_ID;
	}

	@Override
	public String getValueToCache() {
	 	return RssFeedUtils.getWikiPageSourceContent(DisplayUtils.CHALLENGE_OVERVIEW_CONTENT_PAGE_ID);
	}

}
