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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

public class BigTeamBadgeViewImpl extends FlowPanel implements BigTeamBadgeView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	
	@Inject
	public BigTeamBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
	}
	
	@Override
	public void setTeam(final Team team, String description) {
		clear();
		if(team == null)  throw new IllegalArgumentException("Team is required");
		
		String name = team.getName();
		ClickHandler clickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new org.sagebionetworks.web.client.place.Team(team.getId()));
			}
		};
		String pictureUrl = null;
		if (team.getIcon() != null && team.getIcon().length() > 0) {
			pictureUrl = DisplayUtils.createTeamIconUrl(synapseJSNIUtils.getBaseFileHandleUrl(), team.getId());
		}
		
		FlowPanel mediaObjectPanel = DisplayUtils.getMediaObject(name, description, clickHandler,  pictureUrl, false, 5);
		add(mediaObjectPanel);
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


	/*
	 * Private Methods
	 */

}
