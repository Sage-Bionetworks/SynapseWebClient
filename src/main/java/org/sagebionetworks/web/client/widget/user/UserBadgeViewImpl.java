package org.sagebionetworks.web.client.widget.user;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Profile;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.inject.Inject;

public class UserBadgeViewImpl extends LayoutContainer implements UserBadgeView {

	private static final int MAX_DISPLAY_NAME_CHAR = 13;
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	HorizontalPanel container;
	
	@Inject
	public UserBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		
		container = new HorizontalPanel();
		this.add(container);
	}
	
	@Override
	public void setProfile(UserProfile profile, Integer maxNameLength) {
		container.clear();
		if(profile == null)  throw new IllegalArgumentException("Profile is required");
		
		final Anchor userAnchor = new Anchor();
		userAnchor.addStyleName("usernameLink");
		userAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Profile(Profile.VIEW_PROFILE_PLACE_TOKEN));
			}
		});
				
		if(profile != null) {
			//has user data, update the user name and add user commands (and set to the current user name)
			userAnchor.setText(DisplayUtils.stubStrPartialWord(profile.getDisplayName(), MAX_DISPLAY_NAME_CHAR));
			container.clear();
			Image profilePicture; 
			if (profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0) {
				profilePicture = new Image();
				profilePicture.setUrl(DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null));
			} else {
				profilePicture = new Image(sageImageBundle.defaultProfilePicture());
			}
			
			profilePicture.setWidth("16px");
			profilePicture.setHeight("16px");
			profilePicture.addStyleName("imageButton userProfileImage");
			profilePicture.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					userAnchor.fireEvent(event);
				}
			});
			container.add(profilePicture);
			container.add(userAnchor);				 
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}


	/*
	 * Private Methods
	 */
//	private Widget getUserProfileLink(UserProfile profile, final GlobalApplicationState globalApplicationState, SynapseJSNIUtils synapseJSNIUtils) {
//			
//	}

}
