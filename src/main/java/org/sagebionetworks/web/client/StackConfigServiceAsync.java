package org.sagebionetworks.web.client;

import java.util.HashMap;
import org.sagebionetworks.repo.model.status.StackStatus;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This interface allows the client to get stack configuration information.
 *
 */
public interface StackConfigServiceAsync {
	void getSynapseProperties(AsyncCallback<HashMap<String, String>> callback);

	// SWC-4116: CORS preflight failing for the request for stack status (in some cases), use gwt rpc
	// instead.
	void getCurrentStatus(AsyncCallback<StackStatus> callback);
}
