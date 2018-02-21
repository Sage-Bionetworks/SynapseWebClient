package org.sagebionetworks.web.client;

import java.util.HashMap;

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
	void getDoiPrefix(AsyncCallback<String> callback);
	void getSynapseVersions(AsyncCallback<String> callback);
	void getSynapseProperties(AsyncCallback<HashMap<String, String>> callback);
}
