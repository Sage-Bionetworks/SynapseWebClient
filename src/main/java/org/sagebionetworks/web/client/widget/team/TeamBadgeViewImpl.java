package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.inject.Inject;

public class TeamBadgeViewImpl extends LayoutContainer implements TeamBadgeView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	HorizontalPanel container;
	
	@Inject
	public TeamBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		
		container = new HorizontalPanel();
		this.add(container);
		addStyleName("displayInline movedown-4");
	}
	
	@Override
	public void setTeam(final Team team, Integer maxNameLength) {
		container.clear();
		if(team == null)  throw new IllegalArgumentException("Team is required");
		
		if(team != null) {
			String name = maxNameLength == null ? team.getName() : DisplayUtils.stubStrPartialWord(team.getName(), maxNameLength); 
			
			final Anchor anchor = new Anchor();
			anchor.setText(name);
			anchor.addStyleName("usernameLink margin-left-5");
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
				profilePicture.addStyleName("imageButton userProfileImage");
				profilePicture.addClickHandler(clickHandler);
				container.add(profilePicture);
			} else {
				HTML profilePicture = new HTML(DisplayUtils.getFontelloIcon("users font-size-13 movedown-2 imageButton userProfileImage lightGreyText margin-0-imp-before"));
				profilePicture.addClickHandler(clickHandler);
				container.add(profilePicture);
			}
			
			container.add(anchor);
		} 		
		
	}

	@Override
	public void showLoadError(String principalId) {
		container.clear();
		container.add(new HTML(DisplayConstants.ERROR_LOADING));		
	}
	
	@Override
	public void showLoading() {
		container.clear();
		container.add(new HTML(DisplayUtils.getLoadingHtml(sageImageBundle)));
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}

	@Override
	public void clear() {
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}


	/*
	 * Private Methods
	 */

}
