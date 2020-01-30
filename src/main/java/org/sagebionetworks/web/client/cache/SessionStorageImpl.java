package org.sagebionetworks.web.client.cache;

import com.google.gwt.storage.client.Storage;

public class SessionStorageImpl implements SessionStorage {

	private Storage storage;

	public SessionStorageImpl() {
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
		if (isStorageSupported()) {
			try {
				storage.setItem(key, data);
			} catch (Throwable e) {
				// unlikely
			}
		}
	}

	@Override
	public boolean isStorageSupported() {
		return storage != null;
	}
}
