package org.sagebionetworks.web.client.widget.asynch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sagebionetworks.repo.model.UserGroupHeader;

public interface UserGroupHeaderAsyncHandler {
  void getUserGroupHeader(
    String principalId,
    AsyncCallback<UserGroupHeader> callback
  );
}
