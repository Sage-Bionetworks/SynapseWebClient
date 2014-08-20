package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.Notifier;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamBadge implements TeamBadgeView.Presenter, SynapseWidgetPresenter, Notifier {
	
	private TeamBadgeView view;
	SynapseClientAsync synapseClient;
	NodeModelCreator nodeModelCreator;
	private Integer maxNameLength;
	
	@Inject
	public TeamBadge(TeamBadgeView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	public void setMaxNameLength(Integer maxLength) {
		this.maxNameLength = maxLength;
	}
	
	public void configure(final String teamId) {
		if (teamId != null && teamId.trim().length() > 0) {
			view.showLoading();
			synapseClient.getTeam(teamId, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String teamString) {
					try {
						Team team = nodeModelCreator.createJSONEntity(teamString, Team.class);
						configure(team);
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showLoadError(teamId);
				}
			});
		}
	}
	
	public void configure(Team team) {
		view.setTeam(team, maxNameLength);
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void setNotificationValue(String value) {
		view.setRequestCount(value);
	}

}
