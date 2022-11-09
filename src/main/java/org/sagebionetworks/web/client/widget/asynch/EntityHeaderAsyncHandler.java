package org.sagebionetworks.web.client.widget.asynch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sagebionetworks.repo.model.EntityHeader;

//The response always contains the version.  So use this handler if you do not have a version.  If you do, use VersionedEntityHeaderAsyncHandler.
public interface EntityHeaderAsyncHandler {
  void getEntityHeader(String entityId, AsyncCallback<EntityHeader> callback);
}
