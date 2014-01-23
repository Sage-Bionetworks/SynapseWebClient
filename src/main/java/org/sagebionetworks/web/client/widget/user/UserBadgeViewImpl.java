package org.sagebionetworks.web.client.widget.user;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Profile;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadgeViewImpl extends FlowPanel implements UserBadgeView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	
	@Inject
	public UserBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		
	}
	
	@Override
	public void setProfile(final UserProfile profile, Integer maxNameLength) {
		clear();
		if(profile == null)  throw new IllegalArgumentException("Profile is required");
		
		if(profile != null) {
			String displayName = DisplayUtils.getDisplayName(profile);
			String name = maxNameLength == null ? displayName : DisplayUtils.stubStrPartialWord(displayName, maxNameLength); 
			
			Widget nameWidget;
			final Anchor userAnchor = new Anchor();
			if(profile.getOwnerId() != null) {				
				userAnchor.setText(name);
				userAnchor.addStyleName("usernameLink");
				userAnchor.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						globalApplicationState.getPlaceChanger().goTo(new Profile(profile.getOwnerId()));
					}
				});
				nameWidget = userAnchor;
			} else {
				HTML html = new HTML(name);
				html.addStyleName("usernamelink");
				nameWidget = html;
			}
			//also add the username in a popup (in the case when the name shown does not show the entire display name)
			if (displayName.length() != name.length())
				DisplayUtils.addToolTip(nameWidget, displayName);
			Image profilePicture; 
			if (profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0) {
				profilePicture = new Image();
				profilePicture.setUrl(DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null));
			} else {
				profilePicture = new Image(sageImageBundle.defaultProfilePicture20().getSafeUri());
				profilePicture.setPixelSize(16,16);
			}
			
			profilePicture.setWidth("16px");
			profilePicture.setHeight("16px");
			profilePicture.addStyleName("imageButton userProfileImage left");
			profilePicture.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					userAnchor.fireEvent(event);
				}
			});
			add(profilePicture);
			add(nameWidget);				 
		} 		
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}


	/*
	 * Private Methods
	 */

}
