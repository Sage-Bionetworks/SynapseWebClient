package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EntityHeaderAsyncHandlerImpl extends BaseEntityHeaderAsyncHandlerImpl implements EntityHeaderAsyncHandler {

	@Inject
	public EntityHeaderAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		super(jsClient, gwt);
		isUsingVersion = false;
	}
	
	public void getEntityHeader(String entityId, AsyncCallback<EntityHeader> callback) {
		getEntityHeaderShared(entityId, null, callback);
	}
}
