package org.sagebionetworks.web.unitclient.widget.asynch;

import org.sagebionetworks.web.client.widget.asynch.TimerProvider;

/**
 * A simple stub that does not wait.
 * @author jmhill
 *
 */
public class TimerProviderStub implements TimerProvider{

	FireHandler handler;
	
	@Override
	public void setHandler(FireHandler handler) {
		this.handler = handler;
	}

	@Override
	public void schedule(int delayMillis) {
		// Just call the handler fire with no delay.
		this.handler.fire();
	}

	@Override
	public void cancel() {
		// Nothing to do her.
	}

}
