package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.widget.HasNotificationUI;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.asynch.TeamAsyncHandler;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamBadge implements SynapseWidgetPresenter, HasNotificationUI, IsWidget {

	private TeamBadgeView view;
	TeamAsyncHandler teamAsyncHandler;
	private Integer maxNameLength;
	private String teamName;
	private ClickHandler customClickHandler = null;
	String publicAclPrincipalId, authenticatedAclPrincipalId;
	public static final String PUBLIC_GROUP_NAME = "Anyone on the web";
	public static final String AUTHENTICATED_USERS_GROUP_NAME = "All registered Synapse users";
	SynapseJSNIUtils synapseJsniUtils;

	@Inject
	public TeamBadge(TeamBadgeView view, TeamAsyncHandler teamAsyncHandler, SynapseProperties synapseProperties, SynapseJSNIUtils synapseJsniUtils) {
		this.view = view;
		this.teamAsyncHandler = teamAsyncHandler;
		this.synapseJsniUtils = synapseJsniUtils;
		publicAclPrincipalId = synapseProperties.getSynapseProperty(WebConstants.PUBLIC_ACL_PRINCIPAL_ID);
		authenticatedAclPrincipalId = synapseProperties.getSynapseProperty(WebConstants.AUTHENTICATED_ACL_PRINCIPAL_ID);
	}

	public void setMaxNameLength(Integer maxLength) {
		this.maxNameLength = maxLength;
	}

	public void configure(String teamId, ClickHandler customClickHandler) {
		this.customClickHandler = customClickHandler;
		configure(teamId);
	}

	public void configure(String teamId) {
		if (teamId != null && teamId.trim().length() > 0) {
			if (teamId.equals(publicAclPrincipalId)) {
				view.setTeamWithoutLink(PUBLIC_GROUP_NAME, true);
			} else if (teamId.equals(authenticatedAclPrincipalId)) {
				view.setTeamWithoutLink(AUTHENTICATED_USERS_GROUP_NAME, true);
			} else {
				view.showLoading();
				teamAsyncHandler.getTeam(teamId, new AsyncCallback<Team>() {
					@Override
					public void onSuccess(Team team) {
						configure(team);
					}

					@Override
					public void onFailure(Throwable caught) {
						if (teamName != null) {
							view.setTeamWithoutLink(teamName, false);
						} else {
							view.showLoadError(teamId);
						}
					}
				});
			} ;
		}

	}

	public void configure(Team team) {
		String teamIconUrl = synapseJsniUtils.getFileHandleAssociationUrl(team.getId(), FileHandleAssociateType.TeamAttachment, team.getIcon());
		view.setTeam(team, maxNameLength, teamIconUrl, customClickHandler);
	}

	/**
	 * If the teamId is not valid, a badge will be created from the given teamName.
	 */
	public void configure(String teamId, String teamName) {
		this.teamName = teamName;
		configure(teamId);
	}

	@SuppressWarnings("unchecked")
	public void clearState() {}

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
