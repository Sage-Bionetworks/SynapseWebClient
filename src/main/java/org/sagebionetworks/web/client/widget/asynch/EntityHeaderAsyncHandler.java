package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.EntityHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EntityHeaderAsyncHandler {
	void getEntityHeader(String entityId, AsyncCallback<EntityHeader> callback);
}
