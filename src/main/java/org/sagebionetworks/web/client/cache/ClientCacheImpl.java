package org.sagebionetworks.web.client.cache;

import java.util.HashMap;

import com.google.inject.Inject;

public class ClientCacheImpl implements ClientCache {
	private StorageWrapper storage;

	//default to an hour
	public static final Long DEFAULT_CACHE_TIME_MS = 1000L*60L*60L;
	public static final String SUFFIX = "_EXPIRE_TIME";
	public static final String PROTECTED_KEYS = "PROTECTED_FROM_CLEAR";
	
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
				//expired, clean up
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
		put(key, value, expireTime, false);
	}
	
	@Override
	public void put(String key, String value, Long expireTime, boolean isProtected) {
		if (storage.isStorageSupported()) {
			storage.setItem(key + SUFFIX, expireTime.toString());
			storage.setItem(key, value);
			if (isProtected) {
				storage.setItem(PROTECTED_KEYS, key + "," + getProtectedKeys());
			}
		}
	}
	
	private String getProtectedKeys() {
		String protectedKeys = storage.getItem(PROTECTED_KEYS);
		if (protectedKeys == null ) {
			protectedKeys = "";
		}
		return protectedKeys;
	}
	
	@Override
	public void clear() {
		if (storage.isStorageSupported()) {
			// remember all protected values
			String protectedKeys = getProtectedKeys();
			HashMap<String, String> protectedKeyValues = new HashMap<>();
			for (String key : protectedKeys.split(",")) {
				if (!key.isEmpty()) {
					protectedKeyValues.put(key, storage.getItem(key));
					protectedKeyValues.put(key + SUFFIX, storage.getItem(key + SUFFIX));
				}
			}
			storage.clear();
			
			storage.setItem(PROTECTED_KEYS, protectedKeys);
			for (String key : protectedKeyValues.keySet()) {
				storage.setItem(key, protectedKeyValues.get(key));
			}
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
