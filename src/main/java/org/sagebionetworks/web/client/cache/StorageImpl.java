package org.sagebionetworks.web.client.cache;

import com.google.gwt.storage.client.Storage;

public class StorageImpl implements StorageWrapper {

  private Storage storage;

  public StorageImpl() {
    storage = Storage.getLocalStorageIfSupported();
  }

  @Override
  public void clear() {
    if (isStorageSupported()) storage.clear();
  }

  @Override
  public String getItem(String key) {
    if (isStorageSupported()) return storage.getItem(key); else return null;
  }

  @Override
  public void removeItem(String key) {
    if (isStorageSupported()) storage.removeItem(key);
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

  @Override
  public double getBytesUsed() {
    return _getBytesUsed();
  }

  private static native double _getBytesUsed() /*-{
		// Based on https://stackoverflow.com/questions/4391575/how-to-find-the-size-of-localstorage
		try {
			var total = 0,
				valueSize, key;
			for (key in localStorage) {
				if (!localStorage.hasOwnProperty(key)) {
					continue;
				}
				// multiplied by 2 because the char in javascript stores as UTF-16, which _may_ take up 2 bytes.
				// so this will return the upper bound.
				valueSize = ((localStorage[key].length + key.length) * 2);
				total += valueSize;
			};
			return total;
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
