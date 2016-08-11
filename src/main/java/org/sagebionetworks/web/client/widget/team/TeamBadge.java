package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.HasNotificationUI;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamBadge implements TeamBadgeView.Presenter, SynapseWidgetPresenter, HasNotificationUI {
	
	private TeamBadgeView view;
	private SynapseClientAsync synapseClient;
	private Integer maxNameLength;
	private AuthenticationController authController;
	private String teamName;
	
	@Inject
	public TeamBadge(final TeamBadgeView view, SynapseClientAsync synapseClient, AuthenticationController authController) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.authController = authController;
		view.setPresenter(this);
	}
	
	public void setMaxNameLength(Integer maxLength) {
		this.maxNameLength = maxLength;
	}
	
	public void configure(final String teamId) {
		if (teamId != null && teamId.trim().length() > 0) {
			view.showLoading();
			synapseClient.getTeam(teamId, new AsyncCallback<Team>() {
				@Override
				public void onSuccess(Team team) {
						configure(team);
				}
				@Override
				public void onFailure(Throwable caught) {
					if (teamName != null) {
						view.setTeamWithoutLink(teamName, teamId);
					} else {
						view.showLoadError(teamId);
					}
				}
			});
		}
		
	}
	
	public void configure(Team team) {
		view.setTeam(team, maxNameLength, authController.getCurrentXsrfToken());
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
