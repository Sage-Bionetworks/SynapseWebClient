package org.sagebionetworks.web.client.cache;


public interface ClientCache {
	String get(String key);
	void put(String key, String value);
	void put(String key, String value, Long expireTime);
	boolean contains(String key);
}
