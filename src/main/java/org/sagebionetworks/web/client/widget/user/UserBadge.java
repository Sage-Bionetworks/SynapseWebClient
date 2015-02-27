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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadge implements UserBadgeView.Presenter, SynapseWidgetPresenter, WidgetRendererPresenter {
	
	private UserBadgeView view;
	SynapseClientAsync synapseClient;
	private Integer maxNameLength;
	UserProfile profile;
	ClickHandler customClickHandler;
	GlobalApplicationState globalApplicationState;
	SynapseJSNIUtils synapseJSNIUtils;
	boolean isShowCompany;
	String description;
	
	@Inject
	public UserBadge(UserBadgeView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.synapseJSNIUtils = synapseJSNIUtils;
		view.setPresenter(this);
		view.setSize(BadgeSize.SMALL);
		isShowCompany = false;
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
		if (profile != null && profile.getPic() != null && profile.getPic().getPreviewId() != null && profile.getPic().getPreviewId().length() > 0) {
			String url = DisplayUtils.createUserProfileAttachmentUrl(synapseJSNIUtils.getBaseProfileAttachmentUrl(), profile.getOwnerId(), profile.getPic().getPreviewId(), null);
			view.showCustomUserPicture(url);
		} else {
			view.showAnonymousUserPicture();
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
			
			synapseClient.getUserProfile(principalId, new AsyncCallback<UserProfile>() {			
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
			parseProfile(profileString, adapterFactory, callback);
		} else {
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
	}
	
	public static void parseProfile(String profileString, AdapterFactory adapterFactory, AsyncCallback<UserProfile> callback) {
		try {
			UserProfile profile = new UserProfile(adapterFactory.createNew(profileString));
			callback.onSuccess(profile);
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}

	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
