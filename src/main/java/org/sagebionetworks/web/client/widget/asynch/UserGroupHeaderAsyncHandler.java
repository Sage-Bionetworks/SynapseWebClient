package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.UserGroupHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserGroupHeaderAsyncHandler {
	void getUserGroupHeader(String principalId, AsyncCallback<UserGroupHeader> callback);
}
