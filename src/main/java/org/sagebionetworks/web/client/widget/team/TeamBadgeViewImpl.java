package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class TeamBadgeViewImpl extends HorizontalPanel implements TeamBadgeView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	SimplePanel notificationsPanel;
	Long globeId;
	
	@Inject
	public TeamBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		notificationsPanel = new SimplePanel();
		notificationsPanel.addStyleName("displayInline");
	}
	
	@Override
	public void setTeam(final Team team, Integer maxNameLength) {
		clear();
		notificationsPanel.clear();
		if(team == null)  throw new IllegalArgumentException("Team is required");
		
		if(team != null) {
			String name = maxNameLength == null ? team.getName() : DisplayUtils.stubStrPartialWord(team.getName(), maxNameLength); 
			
			final Anchor anchor = new Anchor();
			anchor.setText(name);
			anchor.addStyleName("usernameLink");
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					globalApplicationState.getPlaceChanger().goTo(new org.sagebionetworks.web.client.place.Team(team.getId()));
				}
			});
			
			ClickHandler clickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					anchor.fireEvent(event);
				}
			};
			if (team.getIcon() != null && team.getIcon().length() > 0) {
				Image profilePicture = new Image();
				profilePicture.setUrl(DisplayUtils.createTeamIconUrl(synapseJSNIUtils.getBaseFileHandleUrl(), team.getId()));
				profilePicture.setWidth("16px");
				profilePicture.setHeight("16px");
				profilePicture.addStyleName("imageButton userProfileImage displayInline");
				profilePicture.addClickHandler(clickHandler);
				add(profilePicture);
				setCellWidth(profilePicture, "20px");
			} else {
				HTML profilePicture = new HTML(DisplayUtils.getFontelloIcon("users font-size-13 imageButton userProfileImage lightGreyText margin-0-imp-before displayInline movedown-4"));
				profilePicture.addClickHandler(clickHandler);
				add(profilePicture);
				setCellWidth(profilePicture, "20px");
			}
			add(anchor);
			add(notificationsPanel);
		} 		
		
	}
	
	@Override
	public void setTeamWithoutLink(String name, String teamId) {
		clear();
		notificationsPanel.clear();
		
		Label nameLabel = new Label(name);
		nameLabel.addStyleName("font-size-13 boldText");
		
		HTML profilePicture;
		if (globeId != null && Long.parseLong(teamId) == globeId) {
			//profilePicture = new HTML(DisplayUtils.getFontelloIcon("globe font-size-13 userProfileImage lightGreyText margin-0-imp-before displayInline movedown-4"));
			String html = AbstractImagePrototype.create(iconsImageBundle.globe16()).getHTML();
			profilePicture = new HTML(html);
		} else {
			profilePicture = new HTML(DisplayUtils.getFontelloIcon("users font-size-13 userProfileImage lightGreyText margin-0-imp-before displayInline movedown-4"));
		}
		
		add(profilePicture);
		setCellWidth(profilePicture, "20px");
			
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
		add(new HTML(DisplayUtils.getLoadingHtml(sageImageBundle)));
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
		DisplayUtils.addTooltip(widget, DisplayConstants.PENDING_JOIN_REQUESTS_TOOLTIP);
		notificationsPanel.setWidget(widget);
	}

	@Override
	public void setGlobeId(Long globeId) {
		this.globeId = globeId;
	}

	/*
	 * Private Methods
	 */

}
