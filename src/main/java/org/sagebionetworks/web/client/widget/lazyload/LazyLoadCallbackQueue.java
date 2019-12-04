package org.sagebionetworks.web.client.widget.lazyload;

import org.sagebionetworks.web.client.utils.Callback;

public interface LazyLoadCallbackQueue {
	void subscribe(Callback callback);

	void unsubscribe(Callback callback);
}
