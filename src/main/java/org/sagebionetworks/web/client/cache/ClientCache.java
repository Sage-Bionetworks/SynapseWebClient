package org.sagebionetworks.web.client.cache;


public interface ClientCache {
	/**
	 * Returns the value from storage, iff storage is supported by this browser and the key/value pair was put into storage
	 * If you try to get a value that has expired, this method will return null.
	 * @param key
	 * @return
	 */
	String get(String key);
	/**
	 * The implementation uses sessionStorage, and is NOT SECURE.  Do not store sensitive data here.
	 * Size only limited by system memory.  
	 * The given key/value pair will survive as long as the originating window or tab, or 20 minutes (whichever is shorter).
	 * 
	 * @param key
	 * @param value
	 */
	void put(String key, String value);
	void put(String key, String value, Long expireTime);
	
	/**
	 * Returns true iff storage is supported by this browser and the key/value pair was put into storage.
	 * Even if the value is contained in the cache, it may be expired.
	 * @param key
	 * @return
	 */
	boolean contains(String key);
}
