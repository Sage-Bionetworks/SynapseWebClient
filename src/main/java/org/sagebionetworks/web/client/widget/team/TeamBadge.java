package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.HasNotificationUI;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamBadge implements TeamBadgeView.Presenter, SynapseWidgetPresenter, HasNotificationUI, IsWidget {
	
	private TeamBadgeView view;
	private SynapseJavascriptClient jsClient;
	private Integer maxNameLength;
	private AuthenticationController authController;
	private String teamName;
	private ClickHandler customClickHandler = null;
	
	@Inject
	public TeamBadge(TeamBadgeView view, SynapseJavascriptClient synapseClient, AuthenticationController authController) {
		this.view = view;
		this.jsClient = synapseClient;
		this.authController = authController;
		view.setPresenter(this);
	}
	
	public void setMaxNameLength(Integer maxLength) {
		this.maxNameLength = maxLength;
	}
	public void configure(String teamId, ClickHandler customClickHandler) {
		this.customClickHandler = customClickHandler;
		configure(teamId);
	}
	
	public void configure(final String teamId) {
		if (teamId != null && teamId.trim().length() > 0) {
			view.showLoading();
			jsClient.getTeam(teamId, new AsyncCallback<Team>() {
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
		view.setTeam(team, maxNameLength, authController.getCurrentXsrfToken(), customClickHandler);
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
	
	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}
	
	@Override
	public void setNotificationValue(String value) {
		view.setRequestCount(value);
	}
	
	public void addStyleName(String style) {
		view.addStyleName(style);
	}
	
	public void setOpenNewWindow(boolean isNewWindow) {
		String target = isNewWindow ? "_blank" : "";
		view.setTarget(target);
	}
}
