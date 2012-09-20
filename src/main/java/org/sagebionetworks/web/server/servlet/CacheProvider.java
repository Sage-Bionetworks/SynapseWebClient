package org.sagebionetworks.web.server.servlet;

public interface CacheProvider {
	String getCacheProviderId();
	String getCacheValue();
}
