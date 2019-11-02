package org.sagebionetworks.web.client.cache;

import com.google.inject.Inject;

public class ClientCacheImpl implements ClientCache {
	private StorageWrapper storage;

	// default to an hour
	public static final Long DEFAULT_CACHE_TIME_MS = 1000L * 60L * 60L;
	public static final String SUFFIX = "_EXPIRE_TIME";

	@Inject
	public ClientCacheImpl(StorageWrapper storage) {
		this.storage = storage;
	}

	@Override
	public String get(String key) {
		String expireTimeString = storage.getItem(key + SUFFIX);
		if (storage.isStorageSupported() && expireTimeString != null) {
			Long expireTime = Long.parseLong(expireTimeString);
			if (System.currentTimeMillis() < expireTime) {
				return storage.getItem(key);
			} else {
				// expired, clean up
				storage.removeItem(key);
				storage.removeItem(key + SUFFIX);
			}
		}
		return null;
	}

	@Override
	public void put(String key, String value) {
		put(key, value, System.currentTimeMillis() + DEFAULT_CACHE_TIME_MS);
	}

	@Override
	public void put(String key, String value, Long expireTime) {
		if (storage.isStorageSupported()) {
			storage.setItem(key + SUFFIX, expireTime.toString());
			storage.setItem(key, value);
		}
	}

	@Override
	public void clear() {
		if (storage.isStorageSupported()) {
			storage.clear();
		}
	}

	@Override
	public void remove(String key) {
		if (storage.isStorageSupported()) {
			storage.removeItem(key);
			storage.removeItem(key + SUFFIX);
		}
	}

	@Override
	public boolean contains(String key) {
		return get(key) != null;
	}
}
