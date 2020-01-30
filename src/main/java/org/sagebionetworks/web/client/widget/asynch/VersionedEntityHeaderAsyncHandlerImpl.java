package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class VersionedEntityHeaderAsyncHandlerImpl extends BaseEntityHeaderAsyncHandlerImpl implements VersionedEntityHeaderAsyncHandler {
	@Inject
	public VersionedEntityHeaderAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		super(jsClient, gwt);
		isUsingVersion = true;	
	}

	public void getEntityHeader(String entityId, Long versionNumber, AsyncCallback<EntityHeader> callback) {
		getEntityHeaderShared(entityId, versionNumber, callback);
	}
}
