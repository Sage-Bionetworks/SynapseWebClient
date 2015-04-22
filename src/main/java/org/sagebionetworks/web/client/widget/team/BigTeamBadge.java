package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.HasNotificationUI;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BigTeamBadge implements BigTeamBadgeView.Presenter, SynapseWidgetPresenter, HasNotificationUI {
	
	private BigTeamBadgeView view;
	SynapseClientAsync synapseClient;
	
	private String teamName;
	
	@Inject
	public BigTeamBadge(BigTeamBadgeView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
	}
	
	public void configure(Team team, String description) {
		view.setTeam(team, description);
	}
	
	public void configure(final String teamId) {
		if (teamId != null && teamId.trim().length() > 0) {
			view.showLoading();
			synapseClient.getTeam(teamId, new AsyncCallback<Team>() {
				@Override
				public void onSuccess(Team team) {
						configure(team, team.getDescription());
				}
				@Override
				public void onFailure(Throwable caught) {
					if (teamName != null) {
						view.setTeamWithoutLink(teamName);
					} else {
						view.showLoadError(teamId);
					}
				}
			});
		}
	}
	
	/**
	 * If the teamId is not valid, a badge will be created
	 * from the given teamName.
	 */
	public void configure(String teamId, String teamName) {
		this.teamName = teamName;
		configure(teamId);
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
