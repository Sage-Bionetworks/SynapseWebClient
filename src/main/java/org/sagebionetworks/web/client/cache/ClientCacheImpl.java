package org.sagebionetworks.web.client.cache;

import java.util.HashMap;

import com.google.inject.Inject;

public class ClientCacheImpl implements ClientCache {
	private HashMap<String, Long> key2ExpireTime;
	private StorageWrapper storage;

	//default to an hour
	private static final Long DEFAULT_CACHE_TIME_MS = 1000L*60L*60L;
	
	@Inject
	public ClientCacheImpl(StorageWrapper storage) {
		this.storage = storage;
		key2ExpireTime = new HashMap<String, Long>();
	}

	
	@Override
	public String get(String key) {
		Long expireTime = key2ExpireTime.get(key);
		if (storage.isStorageSupported() && expireTime != null) {
			if (System.currentTimeMillis() < expireTime) {
				return storage.getItem(key); 
			} else {
				//expired, clean up
				storage.removeItem(key);
				key2ExpireTime.remove(key);
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
			key2ExpireTime.put(key, expireTime);
			storage.setItem(key, value);
		}
	}
	
	@Override
	public void remove(String key) {
		if (storage.isStorageSupported()) {
			storage.removeItem(key);
		}
	}
	@Override
	public boolean contains(String key) {
		return get(key) != null;
	}
}
