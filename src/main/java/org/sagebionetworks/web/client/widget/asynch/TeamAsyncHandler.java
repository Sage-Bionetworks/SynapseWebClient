package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.Team;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TeamAsyncHandler {
	void getTeam(String teamId, AsyncCallback<Team> callback);
}
