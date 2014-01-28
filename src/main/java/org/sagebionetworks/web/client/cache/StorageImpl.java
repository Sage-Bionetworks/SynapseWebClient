package org.sagebionetworks.web.client.cache;

import com.google.gwt.storage.client.Storage;

public class StorageImpl implements StorageWrapper {

	private Storage storage;
	public StorageImpl() {
		storage = Storage.getSessionStorageIfSupported();
	}

	@Override
	public void clear() {
		storage.clear();
	}

	@Override
	public String getItem(String key) {
		return storage.getItem(key);
	}

	@Override
	public void removeItem(String key) {
		storage.removeItem(key);
	}

	@Override
	public void setItem(String key, String data) {
		storage.setItem(key, data);
	}
	
	@Override
	public boolean isStorageSupported() {
		return storage != null;
	}
}
