package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class TeamBadgeViewImpl extends FlowPanel implements TeamBadgeView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	SimplePanel notificationsPanel;
	Long publicAclPrincipalId, authenticatedAclPrincipalId;
	Anchor anchor = new Anchor();
	@Inject
	public TeamBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		addStyleName("teamBadge displayInline");
		notificationsPanel = new SimplePanel();
		notificationsPanel.addStyleName("margin-left-5 displayInline");
		
		publicAclPrincipalId = Long.parseLong(globalApplicationState.getSynapseProperty(WebConstants.PUBLIC_ACL_PRINCIPAL_ID));
		authenticatedAclPrincipalId = Long.parseLong(globalApplicationState.getSynapseProperty(WebConstants.AUTHENTICATED_ACL_PRINCIPAL_ID));
	}
	
	@Override
	public void setTeam(final Team team, Integer maxNameLength, ClickHandler customClickHandler) {
		clear();
		notificationsPanel.clear();
		if(team == null)  throw new IllegalArgumentException("Team is required");
		
		if(team != null) {
			String name = maxNameLength == null ? team.getName() : DisplayUtils.stubStrPartialWord(team.getName(), maxNameLength);
			anchor.setText(name);
			if (customClickHandler == null) {
				anchor.setHref(DisplayUtils.getTeamHistoryToken(team.getId()));
			} else {
				anchor.addClickHandler(customClickHandler);
			}
			
			ClickHandler clickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					anchor.fireEvent(event);
				}
			};
			if (team.getIcon() != null && team.getIcon().length() > 0) {
				Image profilePicture = new Image();
				profilePicture.setUrl(DisplayUtils.createTeamIconUrl(synapseJSNIUtils.getBaseFileHandleUrl(), team.getId()));
				profilePicture.setHeight("24px");
				profilePicture.addStyleName("imageButton userProfileImage displayInline margin-right-4");
				profilePicture.addClickHandler(clickHandler);
				add(profilePicture);
			} else {
				Icon defaultProfilePicture = new Icon(IconType.USERS);
				defaultProfilePicture.addStyleName("imageButton lightGreyText margin-right-4");
				add(defaultProfilePicture);
			}
			add(anchor);
			add(notificationsPanel);
		}
	}
	
	@Override
	public void setTeamWithoutLink(String name, String teamId) {
		clear();
		notificationsPanel.clear();
		
		InlineLabel nameLabel = new InlineLabel(name);
		nameLabel.addStyleName("font-size-15 boldText");
		Icon profilePicture;
		if (publicAclPrincipalId != null && Long.parseLong(teamId) == publicAclPrincipalId) {
			profilePicture = new Icon(IconType.GLOBE);
			nameLabel.setText("Anyone on the web");
		} else if (authenticatedAclPrincipalId != null && Long.parseLong(teamId) == authenticatedAclPrincipalId) {
			profilePicture = new Icon(IconType.GLOBE);
			nameLabel.setText("All registered Synapse users");
		} else {
			profilePicture = new Icon(IconType.USERS);
		}
		profilePicture.addStyleName("font-size-lg imageButton lightGreyText margin-right-4 margin-left-5");
		add(profilePicture);
			
		add(nameLabel);
		add(notificationsPanel);
	}
	
	@Override
	public void showLoadError(String principalId) {
		clear();
		add(new HTML(DisplayConstants.ERROR_LOADING));		
	}
	
	@Override
	public void showLoading() {
		clear();
		add(DisplayUtils.getSmallLoadingWidget());
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}

	@Override
	public void setRequestCount(String count) {
		InlineHTML widget = new InlineHTML(DisplayUtils.getBadgeHtml(count));
		notificationsPanel.setWidget(DisplayUtils.addTooltip(widget, DisplayConstants.PENDING_JOIN_REQUESTS_TOOLTIP));
	}
	
	@Override
	public void setTarget(String target) {
		anchor.setTarget(target);
	}
}
