package org.sagebionetworks.web.client.widget.lazyload;

import java.util.ArrayList;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;

public class LazyLoadCallbackQueue {
	public final static int DELAY = 400;
	Callback checkForMoreWorkCallback;
	ArrayList<Callback> callbacks = new ArrayList<Callback>();
	GWTWrapper gwt;
	
	private static LazyLoadCallbackQueue instance = null;
	public static LazyLoadCallbackQueue getInstance(GWTWrapper gwt) {
		if (instance == null) {
			instance = new LazyLoadCallbackQueue(gwt);
			instance.fire();
		}
		return instance;
	}
	
	private LazyLoadCallbackQueue(GWTWrapper gwt) {
		this.gwt = gwt;
		checkForMoreWorkCallback = () -> {
			fire();
		};
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
