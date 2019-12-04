package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamBadgeViewImpl extends FlowPanel implements TeamBadgeView {

	SynapseJSNIUtils synapseJSNIUtils;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	SimplePanel notificationsPanel;
	ClickHandler customClickHandler;
	Anchor anchor = new Anchor();
	String teamId;
	public static PlaceChanger placeChanger = null;
	public static final String TEAM_ID_ATTRIBUTE = "data-team-id";
	public static final ClickHandler STANDARD_CLICKHANDLER = event -> {
		if (!DisplayUtils.isAnyModifierKeyDown(event)) {
			event.preventDefault();
			Widget panel = (Widget) event.getSource();
			String teamId = panel.getElement().getAttribute(TEAM_ID_ATTRIBUTE);
			placeChanger.goTo(new org.sagebionetworks.web.client.place.Team(teamId));
		}
	};

	@Inject
	public TeamBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils, SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, GlobalApplicationState globalApplicationState) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		addStyleName("teamBadge displayInline");
		notificationsPanel = new SimplePanel();
		notificationsPanel.addStyleName("margin-left-5 displayInline");
		placeChanger = globalApplicationState.getPlaceChanger();
	}

	@Override
	public void setTeam(final Team team, Integer maxNameLength, String teamIconUrl, ClickHandler customClickHandler) {
		clear();
		teamId = team.getId();
		this.customClickHandler = customClickHandler;
		if (customClickHandler == null) {
			anchor.addClickHandler(STANDARD_CLICKHANDLER);
		} else {
			anchor.addClickHandler(event -> {
				if (!DisplayUtils.isAnyModifierKeyDown(event)) {
					event.preventDefault();
					customClickHandler.onClick(event);
				}
			});
		}
		notificationsPanel.clear();
		anchor.getElement().setAttribute(TEAM_ID_ATTRIBUTE, teamId);
		if (team != null) {
			String name = maxNameLength == null ? team.getName() : DisplayUtils.stubStrPartialWord(team.getName(), maxNameLength);
			anchor.setText(name);
			anchor.setHref(DisplayUtils.getTeamHistoryToken(team.getId()));

			ClickHandler clickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					anchor.fireEvent(event);
				}
			};
			if (team.getIcon() != null && team.getIcon().length() > 0) {
				Image profilePicture = new Image();
				profilePicture.setUrl(teamIconUrl);
				profilePicture.setHeight("24px");
				profilePicture.addStyleName("imageButton displayInline margin-right-4 img-circle");
				profilePicture.addClickHandler(clickHandler);
				add(profilePicture);
			} else {
				Icon defaultProfilePicture = new Icon(IconType.SYN_USERS);
				defaultProfilePicture.setSize(IconSize.LARGE);
				defaultProfilePicture.addStyleName("imageButton lightGreyText margin-right-4");
				add(defaultProfilePicture);
			}
			add(anchor);
			add(notificationsPanel);
		}
	}

	@Override
	public void setTeamWithoutLink(String name, boolean isPublic) {
		clear();
		notificationsPanel.clear();

		InlineLabel nameLabel = new InlineLabel(name);
		nameLabel.addStyleName("font-size-13 boldText");
		Icon profilePicture = isPublic ? new Icon(IconType.GLOBE) : new Icon(IconType.SYN_USERS);
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
	public void showInfo(String message) {}

	@Override
	public void showErrorMessage(String message) {}

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
