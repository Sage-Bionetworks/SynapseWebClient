package org.sagebionetworks.web.client.cache;

public interface StorageWrapper {
	void clear();
	String getItem(String key);
	void removeItem(String key);
	void setItem(String key, String data);
	boolean isStorageSupported();
}
