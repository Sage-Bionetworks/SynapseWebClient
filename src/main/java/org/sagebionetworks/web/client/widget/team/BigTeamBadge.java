package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.HasNotificationUI;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BigTeamBadge implements SynapseWidgetPresenter, HasNotificationUI {

	private BigTeamBadgeView view;
	SynapseJavascriptClient jsClient;
	AuthenticationController authController;
	TeamMemberCountWidget teamMemberCountWidget;

	@Inject
	public BigTeamBadge(BigTeamBadgeView view, SynapseJavascriptClient jsClient, AuthenticationController authController, TeamMemberCountWidget teamMemberCountWidget) {
		this.view = view;
		this.jsClient = jsClient;
		this.authController = authController;
		this.teamMemberCountWidget = teamMemberCountWidget;
		view.setMemberCountWidget(teamMemberCountWidget);
	}

	public void configure(Team team, String description) {
		configure(team, description, null);
	}

	public void configure(Team team, String description, TeamMembershipStatus teamMembershipStatus) {
		teamMemberCountWidget.configure(team.getId());
		view.setTeam(team, description);
		boolean canSendEmail = teamMembershipStatus != null && teamMembershipStatus.getCanSendEmail();
		view.setTeamEmailAddress(getTeamEmail(team.getName(), canSendEmail));
	}

	public void configure(final String teamId) {
		if (teamId != null && teamId.trim().length() > 0) {
			view.showLoading();
			jsClient.getTeam(teamId, new AsyncCallback<Team>() {
				@Override
				public void onSuccess(Team team) {
					configure(team, team.getDescription());
				}

				@Override
				public void onFailure(Throwable caught) {
					view.showLoadError(teamId);
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setNotificationValue(String value) {
		view.setRequestCount(value);
	}

	public void addStyleName(String style) {
		view.addStyleName(style);
	}

	public void setHeight(String height) {
		view.setHeight(height);
	}

	public String getTeamEmail(String teamName, boolean canSendEmail) {
		if (authController.isLoggedIn() && canSendEmail) {
			// strip out any non-word character. Not a (letter, number, underscore)
			return teamName.replaceAll("\\W", "") + "@synapse.org";
		} else {
			return "";
		}
	}

}
