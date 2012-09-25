package org.sagebionetworks.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * This interface allows the client to get stack configuration information.
 * It can be extended, as needed, with methods from StackConfiguration.
 * 
 * @author brucehoff
 *
 */
public interface StackConfigServiceAsync {

	void getBCCSignupEnabled(AsyncCallback<String> callback);
}
