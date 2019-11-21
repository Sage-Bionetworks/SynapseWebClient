package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.EntityHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;

// The response always contains the version.  So use this handler if you have an entity version.  Otherwise use EntityHeaderAsyncHandler. 
public interface VersionedEntityHeaderAsyncHandler {
	void getEntityHeader(String entityId, Long versionNumber, AsyncCallback<EntityHeader> callback);
}
