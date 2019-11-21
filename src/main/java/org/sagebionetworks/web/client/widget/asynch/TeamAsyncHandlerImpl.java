package org.sagebionetworks.web.client.widget.asynch;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import java.util.List;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TeamAsyncHandlerImpl extends AsyncHandlerImpl implements TeamAsyncHandler {
	SynapseJavascriptClient jsClient;

	@Inject
	public TeamAsyncHandlerImpl(SynapseJavascriptClient jsClient, GWTWrapper gwt) {
		super(gwt);
		this.jsClient = jsClient;
	}

	@Override
	public void getTeam(String teamId, AsyncCallback<Team> callback) {
		super.get(teamId, callback);
	}

	@Override
	public void doCall(List ids, final AsyncCallback<List> callback) {
		jsClient.listTeams(ids).addCallback(new FutureCallback<List<Team>>() {
			@Override
			public void onSuccess(List<Team> teams) {
				callback.onSuccess(teams);
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		}, directExecutor());
	}

	@Override
	public String getId(Object singleItem) {
		return ((Team) singleItem).getId();
	}
}
