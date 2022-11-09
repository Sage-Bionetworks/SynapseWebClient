package org.sagebionetworks.web.client.widget.asynch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sagebionetworks.repo.model.Team;

public interface TeamAsyncHandler {
  void getTeam(String teamId, AsyncCallback<Team> callback);
}
