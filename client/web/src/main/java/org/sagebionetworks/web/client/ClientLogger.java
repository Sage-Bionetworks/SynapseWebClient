package org.sagebionetworks.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Provides logging for client-side code.  The log message is sent to the server, where it is logged with Log4J.
 * 
 * @author jmhill
 *
 */
public class ClientLogger {
	
	private SynapseClientAsync synapseClient;
	
	@Inject
	public ClientLogger(SynapseClientAsync synapseClient){
		this.synapseClient = synapseClient;
	}
	
	public void debug(String message){
		this.synapseClient.logDebug(message, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				// Nothing to do here.
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// Nothing to do here.
			}
		});
	}

}
