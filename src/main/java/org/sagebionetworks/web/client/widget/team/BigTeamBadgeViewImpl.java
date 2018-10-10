package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.Linkify;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class BigTeamBadgeViewImpl extends FlowPanel implements BigTeamBadgeView {
	
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	SimplePanel notificationsPanel;
	Linkify linkify;
	@Inject
	public BigTeamBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			Linkify linkify) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.linkify = linkify;
		addStyleName("bigTeamBadge");
		notificationsPanel = new SimplePanel();
		notificationsPanel.addStyleName("displayInline pull-left margin-left-5");
	}
	
	@Override
	public void setTeam(final Team team, String description) {
		clear();
		notificationsPanel.clear();
		if(team == null)  throw new IllegalArgumentException("Team is required");
		
		String name = team.getName();
		ClickHandler clickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				event.preventDefault();
				globalApplicationState.getPlaceChanger().goTo(new org.sagebionetworks.web.client.place.Team(team.getId()));
			}
		};
		String pictureUrl = null;
		if (team.getIcon() != null && team.getIcon().length() > 0) {
			pictureUrl = synapseJSNIUtils.getFileHandleAssociationUrl(team.getId(), FileHandleAssociateType.TeamAttachment, team.getIcon());
		}
		String descriptionWithoutHtml = SafeHtmlUtils.htmlEscape(description);
		addBadgeMedia(team.getId(), DisplayUtils.getMediaObject(name, linkify.linkify(descriptionWithoutHtml), clickHandler,  pictureUrl, false, 5));
	}
	
	@Override
	public void setTeamWithoutLink(String name) {
		clear();
		notificationsPanel.clear();
		addBadgeMedia(null, DisplayUtils.getMediaObject(name, null, null,  null, false, 5));
	}
	
	private void addBadgeMedia(String teamId, FlowPanel mediaObjectPanel) {
		Anchor anchor = new Anchor();
		if (teamId != null) {
			anchor.setHref(DisplayUtils.getTeamHistoryToken(teamId));	
		}
		anchor.add(mediaObjectPanel);
		anchor.addStyleName("clearfix");
		mediaObjectPanel.addStyleName("displayInline");
		add(notificationsPanel);
		add(anchor);
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
	public void showInfo(String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}

	@Override
	public void setRequestCount(String count) {
		InlineHTML widget = new InlineHTML(DisplayUtils.getBadgeHtml(count));
		notificationsPanel.setWidget(DisplayUtils.addTooltip(widget, DisplayConstants.PENDING_JOIN_REQUESTS_TOOLTIP));
	}

	/*
	 * Private Methods
	 */

}
