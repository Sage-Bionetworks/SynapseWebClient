package org.sagebionetworks.web.client.widget.lazyload;

import java.util.ArrayList;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.inject.Inject;

public class LazyLoadCallbackQueue {
	GWTWrapper gwt;
	public final static int DELAY = 400;
	Callback checkForMoreWorkCallback;
	ArrayList<Callback> callbacks = new ArrayList<Callback>();
	
	@Inject
	public LazyLoadCallbackQueue(
			GWTWrapper gwt
			) {
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
