package org.sagebionetworks.web.client.cache;

import java.util.HashMap;

import com.google.inject.Inject;

public class ClientCacheImpl implements ClientCache {
	//key suffixes (used to avoid collision in the cache)
	public static final String USER_PROFILE_SUFFIX = "_USER_PROFILE";
	
	private HashMap<String, Long> key2ExpireTime;
	private StorageWrapper storage;

	//default to 20 minutes
	private static final Long DEFAULT_CACHE_TIME_MS = 1000L*60L*20L;
	
	@Inject
	public ClientCacheImpl(StorageWrapper storage) {
		this.storage = storage;
		key2ExpireTime = new HashMap<String, Long>();
	}

	
	@Override
	public String get(String key) {
		String value = null;
		if (contains(key)) {
			value = storage.getItem(key);
		}
		return value;
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
	public boolean contains(String key) {
		boolean isContained = false;
		if (storage.isStorageSupported() && key2ExpireTime.containsKey(key)) {
			Long expireTime = key2ExpireTime.get(key);
			if (System.currentTimeMillis() < expireTime) {
				isContained = true;
			} else {
				//expired, clean up
				storage.removeItem(key);
				key2ExpireTime.remove(key);
			}
		}
		return isContained;
	}
}
