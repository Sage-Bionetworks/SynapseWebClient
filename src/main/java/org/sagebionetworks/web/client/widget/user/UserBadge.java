package org.sagebionetworks.web.client.widget.user;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.Map;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserBadge implements UserBadgeView.Presenter, SynapseWidgetPresenter, WidgetRendererPresenter, IsWidget {
	
	public static final String DEFAULT_COLOR = "lightgrey";
	public static final String DEFAULT_LETTER = "S";
	private UserBadgeView view;
	SynapseClientAsync synapseClient;
	private Integer maxNameLength;
	UserProfile profile;
	GlobalApplicationState globalApplicationState;
	SynapseJSNIUtils synapseJSNIUtils;
	boolean isShowCompany;
	boolean useCachedImage;
	private ClientCache clientCache;
	public static final String[] COLORS = {"chocolate","black","firebrick","maroon","olive","limegreen","forestgreen","darkturquoise","teal","blue","navy","darkmagenta","purple", "stateblue","orangered","forestblue", "blueviolet"};
	private String principalId = null, username = null;
	UserProfileAsyncHandler userProfileAsyncHandler;
	private AdapterFactory adapterFactory;
	
	public static final ClickHandler DO_NOTHING_ON_CLICK = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			//do nothing, let event go to button only
		}
	};
	
	@Inject
	public UserBadge(UserBadgeView view, 
			SynapseClientAsync synapseClient, 
			GlobalApplicationState globalApplicationState,
			SynapseJSNIUtils synapseJSNIUtils,
			ClientCache clientCache,
			UserProfileAsyncHandler userProfileAsyncHandler,
			AdapterFactory adapterFactory) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.globalApplicationState = globalApplicationState;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.clientCache = clientCache;
		this.userProfileAsyncHandler = userProfileAsyncHandler;
		this.adapterFactory = adapterFactory;
		view.setPresenter(this);
		view.setSize(BadgeSize.DEFAULT);
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
		if (isShowCompany) {
			view.showDescription(profile.getCompany());
		}
		view.setUserId(profile.getOwnerId());
		view.setHref("#!Profile:" + profile.getOwnerId());
		useCachedImage = true;
		configurePicture();
	}
	
	public void setOpenNewWindow(boolean value) {
		if (value) {
			view.setOpenInNewWindow();
		}
	}
	
	public void setSize(BadgeSize size) {
		view.setSize(size);
	}

	public void configurePicture() {
		if (profile != null && profile.getProfilePicureFileHandleId() != null) {
			String url = synapseJSNIUtils.getFileHandleAssociationUrl(profile.getOwnerId(), FileHandleAssociateType.UserProfileAttachment, profile.getProfilePicureFileHandleId());
			if (!useCachedImage) {
				url += DisplayUtils.getParamForNoCaching();
			}

			view.showCustomUserPicture(url);
		} else {
			showDefaultPicture();
		}
	}
	
	public void showDefaultPicture() {
		view.setDefaultPictureLetter(getDefaultPictureLetter(profile));
		view.setDefaultPictureColor(getDefaultPictureColor(profile));
		view.showAnonymousUserPicture();
	}
	
	public String getDefaultPictureColor(UserProfile profile) {
		if (profile == null) {
			return DEFAULT_COLOR;
		}
		return getColor(profile.getUserName().hashCode());
	}
	
	public static String getColor(int hashcode) {
		int index = Math.abs(hashcode % COLORS.length);
		return COLORS[index];
	}
	
	public String getDefaultPictureLetter(UserProfile profile) {
		if (profile == null) {
			return DEFAULT_LETTER;
		}
		if (DisplayUtils.isDefined(profile.getFirstName())) {
			return (""+profile.getFirstName().charAt(0)).toUpperCase();
		} else {
			return (""+profile.getUserName().charAt(0)).toUpperCase();
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
		this.principalId = principalId;
		profile = null;
		username = null;
		
		//get user profile and configure
		view.clear();
		view.showLoading();
		if (principalId != null && principalId.trim().length() > 0) {
			loadBadge();			
		} else {
			view.showLoadError("Missing user ID");
		}
	}
	
	public void configure(String principalId, boolean isShowCompany) {
		this.isShowCompany = isShowCompany;
		configure(principalId);
	}

	public void loadBadge() {
		if (profile == null) {
			if (principalId != null) {
				UserProfile profile = getUserProfileFromCache(principalId, adapterFactory, clientCache);
				if (profile != null) {
					configure(profile);
				} else {
					userProfileAsyncHandler.getUserProfile(principalId, new AsyncCallback<UserProfile>() {			
						@Override
						public void onSuccess(UserProfile result) {
							cacheProfile(result);
							configure(result);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							view.showLoadError(principalId);
						}
					});
				}
			} else if (username != null) {
				// get the user profile from the username
				synapseClient.getUserProfileFromUsername(username, new AsyncCallback<UserProfile>() {
					@Override
					public void onFailure(Throwable caught) {
						view.showLoadError(caught.getMessage());
					}
					@Override
					public void onSuccess(UserProfile userProfile) {
						clientCache.put(username + WebConstants.USERNAME_SUFFIX, userProfile.getOwnerId());
						configure(userProfile);
					}
				});
			}
		}
	}
	
	/**
	 * When the username is clicked, call this clickhandler instead of the default behavior
	 * @param clickHandler
	 */
	public void setCustomClickHandler(ClickHandler clickHandler) {
		view.setCustomClickHandler(clickHandler);
	}	

	public static UserProfile getUserProfileFromCache(String principalId, AdapterFactory adapterFactory, ClientCache clientCache) {
		String profileString = clientCache.get(principalId + WebConstants.USER_PROFILE_SUFFIX);
		if (profileString != null) {
			try {
				return new UserProfile(adapterFactory.createNew(profileString));
			} catch (JSONObjectAdapterException e) {
				//if any problems occur, try to get the user profile from with a rpc
			}	
		}
		return null;
	}
	
	public void cacheProfile(UserProfile profile) {
		JSONObjectAdapter adapter = adapterFactory.createNew();
		try {
			profile.writeToJSONObject(adapter);
			clientCache.put(profile.getOwnerId() + WebConstants.USER_PROFILE_SUFFIX, adapter.toJSONString());
		} catch (JSONObjectAdapterException e) {
		}
	}
	
	public void clearState() {
		profile = null;
		isShowCompany = false;
		maxNameLength = null;
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setStyleNames(String style) {
		view.setStyleNames(style);
	}
	
	public void addUsernameLinkStyle(String style) {
		view.addUsernameLinkStyle(style);
	}

	public void setHeight(String height) {
		view.setHeight(height);
	}
	@Override
	public void onImageLoadError() {
		if (useCachedImage) {
			//try not caching the image on load
			useCachedImage = false;
			configurePicture();	
		}
	}
	
	public void setDoNothingOnClick() {
		view.doNothingOnClick();
	}
}
