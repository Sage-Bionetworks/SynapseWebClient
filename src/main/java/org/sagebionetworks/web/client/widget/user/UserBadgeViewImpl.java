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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadgeViewImpl extends FlowPanel implements UserBadgeView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	HorizontalPanel container;
	ClickHandler customClickHandler;
	
	@Inject
	public UserBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		
		customClickHandler = null;
		container = new HorizontalPanel();
		container.addStyleName("nobordertable-imp displayInline");
		container.setVerticalAlignment( HasVerticalAlignment.ALIGN_MIDDLE);
		this.add(container);
		addStyleName("inline-block");
	}
	
	@Override
	public void setProfile(final UserProfile profile, Integer maxNameLength) {
		container.clear();
		
		if(profile == null)  throw new IllegalArgumentException("Profile is required");
		
		if(profile != null) {
			String displayName = DisplayUtils.getDisplayName(profile);
			String name = maxNameLength == null ? displayName : DisplayUtils.stubStrPartialWord(displayName, maxNameLength); 
			
			Widget nameWidget;
			final Anchor userAnchor = new Anchor();
			if(profile.getOwnerId() != null) {				
				userAnchor.setText(name);
				userAnchor.addStyleName("usernameLink margin-left-5");
				userAnchor.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (customClickHandler == null) 
							globalApplicationState.getPlaceChanger().goTo(new Profile(profile.getOwnerId()));
						else
							customClickHandler.onClick(event);
					}
				});
				nameWidget = userAnchor;
			} else {
				HTML html = new HTML(name);
				html.addStyleName("usernamelink margin-left-5");
				nameWidget = html;
			}
			//also add the username in a popup (in the case when the name shown does not show the entire display name)
			if (displayName.length() != name.length())
				DisplayUtils.addTooltip(nameWidget, displayName);
			ClickHandler clickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					userAnchor.fireEvent(event);
				}
			};
			
			if (profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0) {
				Image profilePicture = new Image();
				profilePicture.setUrl(DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null));
				profilePicture.setWidth("16px");
				profilePicture.setHeight("16px");
				profilePicture.addStyleName("imageButton userProfileImage");
				profilePicture.addClickHandler(clickHandler);
				container.add(profilePicture);	
			} else {
				HTML profilePicture = new HTML(DisplayUtils.getFontelloIcon("user font-size-13 imageButton userProfileImage lightGreyText margin-0-imp-before"));
				profilePicture.addClickHandler(clickHandler);
				container.add(profilePicture);
			}
			
			container.add(nameWidget);				 
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
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}

	@Override
	public void setCustomClickHandler(ClickHandler clickHandler) {
		customClickHandler = clickHandler;
	}


	/*
	 * Private Methods
	 */

}
