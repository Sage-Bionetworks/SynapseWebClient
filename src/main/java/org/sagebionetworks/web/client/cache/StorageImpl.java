package org.sagebionetworks.web.client.cache;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.storage.client.Storage;

public class StorageImpl implements StorageWrapper {

	private static Logger storageLogger = Logger.getLogger(StorageImpl.class.getName());
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
		if (isStorageSupported()) {
			try {
				storage.setItem(key, data);
			} catch (Throwable e) {
				storageLogger.log(Level.SEVERE, "Failed to set item in the cache: " + e.getMessage());
			}
		}
	}
	
	@Override
	public boolean isStorageSupported() {
		return storage != null;
	}
}
