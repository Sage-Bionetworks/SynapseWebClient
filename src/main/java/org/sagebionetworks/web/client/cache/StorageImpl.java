package org.sagebionetworks.web.client.cache;

import com.google.gwt.storage.client.Storage;

public class StorageImpl implements StorageWrapper {

	private Storage storage;
	public StorageImpl() {
		storage = Storage.getSessionStorageIfSupported();
	}

	@Override
	public void clear() {
		if (isStorageSupported())
			storage.clear();
	}

	@Override
	public String getItem(String key) {
		if (isStorageSupported())
			return storage.getItem(key);
		else
			return null;
	}

	@Override
	public void removeItem(String key) {
		if (isStorageSupported())
			storage.removeItem(key);
	}

	@Override
	public void setItem(String key, String data) {
		if (isStorageSupported())
			storage.setItem(key, data);
	}
	
	@Override
	public boolean isStorageSupported() {
		return storage != null;
	}
}
