package org.sagebionetworks.web.client.widget.lazyload;

import java.util.ArrayList;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class LazyLoadCallbackQueueImpl implements LazyLoadCallbackQueue {
	public final static int DELAY = 300;
	Callback checkForMoreWorkCallback;
	ArrayList<Callback> callbacks = new ArrayList<Callback>();
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
		//execute callbacks, and schedule the next execution
		for (Callback callback : callbacks) {
			callback.invoke();
		}
		gwt.scheduleExecution(checkForMoreWorkCallback, DELAY);
	}
}
