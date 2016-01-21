package org.sagebionetworks.web.client.widget.user;

import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadge implements UserBadgeView.Presenter, SynapseWidgetPresenter, WidgetRendererPresenter, IsWidget {
	
	private UserBadgeView view;
	SynapseClientAsync synapseClient;
	private Integer maxNameLength;
	UserProfile profile;
	ClickHandler customClickHandler;
	GlobalApplicationState globalApplicationState;
	SynapseJSNIUtils synapseJSNIUtils;
	boolean isShowCompany;
	String description;
	boolean useCachedImage;
	private AdapterFactory adapterFactory;
	private ClientCache clientCache;
	public static final String[] COLORS = {"silver","gray","black","red","maroon","olive","lime","green","aqua","teal","blue","navy","fuchsia","purple"};
	
	@Inject
	public UserBadge(UserBadgeView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState,
			SynapseJSNIUtils synapseJSNIUtils,
			AdapterFactory adapterFactory,
			ClientCache clientCache) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.adapterFactory = adapterFactory;
		this.clientCache = clientCache;
		
		view.setPresenter(this);
		view.setSize(BadgeSize.SMALL);
		clearState();
	}
	
	public void setMaxNameLength(Integer maxLength) {
		this.maxNameLength = maxLength;
	}
	
	/**
	 * Simple configure, with user profile
	 * @param profile
	 */
	public void configure(UserProfile profile) {
		if (profile == null) {
			view.showLoadError("Missing profile");
			return;
		}
		this.profile = profile;
		String displayName = DisplayUtils.getDisplayName(profile);
		String shortDisplayName = maxNameLength == null ? displayName : DisplayUtils.stubStrPartialWord(displayName, maxNameLength); 
		view.setDisplayName(displayName, shortDisplayName);
		if (description != null) {
			view.showDescription(description);
		} else if (isShowCompany) {
			view.showDescription(profile.getCompany());
		}
		useCachedImage = true;
		configurePicture();
	}
	
	public void configure(UserProfile profile, String description) {
		this.description = description;
		configure(profile);
	}
	
	public void setSize(BadgeSize size) {
		view.setSize(size);
	}

	public void configurePicture() {
		if (profile != null && profile.getProfilePicureFileHandleId() != null) {
			String url = DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getProfilePicureFileHandleId(), true);
			if (!useCachedImage) {
				url += DisplayUtils.getParamForNoCaching();
			}
			view.showCustomUserPicture(url);
		} else {
			view.setDefaultPictureLetter(getDefaultLetter(profile).toUpperCase());
			view.setDefaultPictureColor(getColor(profile.getUserName().hashCode()));
			view.showAnonymousUserPicture();
		}
	}
	
	public String getColor(int hashcode) {
		int index = hashcode % COLORS.length;
		return COLORS[index];
	}
	
	public String getDefaultLetter(UserProfile profile) {
		if (DisplayUtils.isDefined(profile.getFirstName())) {
			return ""+profile.getFirstName().charAt(0);
		} else {
			return ""+profile.getUserName().charAt(0);
		}
	}
	/**
	 * Wiki configure
	 */
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		//get the user id from the descriptor, and pass to the other configure
		configure(widgetDescriptor.get(WidgetConstants.USERBADGE_WIDGET_ID_KEY));
	}
	
	/**
	 * Simple configure, without UserProfile
	 * @param principalId
	 */
	public void configure(final String principalId) {
		
		//get user profile and configure
		view.clear();
		if (principalId != null && principalId.trim().length() > 0) {
			view.showLoading();
			
			getUserProfile(principalId, adapterFactory, synapseClient, clientCache, new AsyncCallback<UserProfile>() {			
				@Override
				public void onSuccess(UserProfile result) {
					configure(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showLoadError(principalId);
				}
			});
		} else {
			view.showLoadError("Missing user ID");
		}
	}
	
	public void configure(String principalId, boolean isShowCompany) {
		this.isShowCompany = isShowCompany;
		configure(principalId);
	}

	
	public void configure(String principalId, String description) {
		this.description = description;
		configure(principalId);
		
	}
	
	/**
	 * When the username is clicked, call this clickhandler instead of the default behavior
	 * @param clickHandler
	 */
	public void setCustomClickHandler(ClickHandler clickHandler) {
		customClickHandler = clickHandler;
	}	
	
	@Override
	public void badgeClicked(ClickEvent event) {
		if (customClickHandler == null) 
			globalApplicationState.getPlaceChanger().goTo(new Profile(profile.getOwnerId()));
		else
			customClickHandler.onClick(event);
	}

	public static void getUserProfile(final String principalId, final AdapterFactory adapterFactory, SynapseClientAsync synapseClient, final ClientCache clientCache, final AsyncCallback<UserProfile> callback) {
		String profileString = clientCache.get(principalId + WebConstants.USER_PROFILE_SUFFIX);
		if (profileString != null) {
			try {
				UserProfile profile = new UserProfile(adapterFactory.createNew(profileString));
				callback.onSuccess(profile);
				return;
			} catch (JSONObjectAdapterException e) {
				//if any problems occur, try to get the user profile from with a rpc
			}	
		}
		
		synapseClient.getUserProfile(principalId, new AsyncCallback<UserProfile>() {			
			@Override
			public void onSuccess(UserProfile result) {
					JSONObjectAdapter adapter = adapterFactory.createNew();
					try {
						result.writeToJSONObject(adapter);
						clientCache.put(principalId + WebConstants.USER_PROFILE_SUFFIX, adapter.toJSONString());
						callback.onSuccess(result);
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
			
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	
	}
	
	public void clearState() {
		profile = null;
		description = null;
		customClickHandler = null;
		isShowCompany = false;
		maxNameLength = null;
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onImageLoadError() {
		if (useCachedImage) {
			//try not caching the image on load
			useCachedImage = false;
			configurePicture();	
		}
	}
}
