package org.sagebionetworks.web.client.widget.lazyload;

import java.util.HashSet;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.inject.Inject;

public class LazyLoadCallbackQueueImpl implements LazyLoadCallbackQueue {
	public final static int DELAY = 300;
	Callback checkForMoreWorkCallback;
	HashSet<Callback> callbacks = new HashSet<Callback>();
	GWTWrapper gwt;

	@Inject
	public LazyLoadCallbackQueueImpl(GWTWrapper gwt) {
		this.gwt = gwt;
		checkForMoreWorkCallback = () -> {
			fire();
		};
		fire();
	}

	public void subscribe(Callback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	public void unsubscribe(Callback callback) {
		callbacks.remove(callback);
	}

	public void fire() {
		// execute callbacks, and schedule the next execution
		HashSet<Callback> callbackCopy = new HashSet<Callback>(callbacks);
		for (Callback callback : callbackCopy) {
			callback.invoke();
		}
		gwt.scheduleExecution(checkForMoreWorkCallback, DELAY);
	}
}
