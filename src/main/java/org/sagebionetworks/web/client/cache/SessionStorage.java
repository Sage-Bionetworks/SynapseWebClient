package org.sagebionetworks.web.client.cache;

/**
 * If HTML5 session storage is not supported on the running platform then these methods are no-ops,
 * and getItem returns null.
 */
public interface SessionStorage {
	/**
	 * Removes all items in the Storage
	 */
	void clear();

	/**
	 * Returns the item in the Storage associated with the specified key.
	 */
	String getItem(String key);

	/**
	 * Removes the item in the Storage associated with the specified key.
	 */
	void removeItem(String key);

	/**
	 * Sets the value in the Storage associated with the specified key to the specified data.
	 */
	void setItem(String key, String data);

	/**
	 * Returns true if the Storage API is supported on the running platform.
	 */
	boolean isStorageSupported();
}
