package org.sagebionetworks.web.client.widget.user;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.Map;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
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

public class UserBadge implements SynapseWidgetPresenter, WidgetRendererPresenter, IsWidget {

	private UserBadgeView view;
	SynapseClientAsync synapseClient;
	UserProfile profile;
	GlobalApplicationState globalApplicationState;
	SynapseJSNIUtils synapseJSNIUtils;
	boolean useCachedImage;
	private ClientCache clientCache;
	private String principalId = null, username = null;
	UserProfileAsyncHandler userProfileAsyncHandler;
	private AdapterFactory adapterFactory;

	public static final ClickHandler DO_NOTHING_ON_CLICK = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			// do nothing, let event go to button only
		}
	};

	@Inject
	public UserBadge(UserBadgeView view, SynapseClientAsync synapseClient, GlobalApplicationState globalApplicationState, SynapseJSNIUtils synapseJSNIUtils, ClientCache clientCache, UserProfileAsyncHandler userProfileAsyncHandler, AdapterFactory adapterFactory) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.globalApplicationState = globalApplicationState;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.clientCache = clientCache;
		this.userProfileAsyncHandler = userProfileAsyncHandler;
		this.adapterFactory = adapterFactory;
		view.setSize(BadgeSize.DEFAULT);
		clearState();
	}

	/**
	 * Simple configure, with user profile
	 * 
	 * @param profile
	 */
	public void configure(UserProfile profile) {
		this.profile = profile;
		useCachedImage = true;
		view.configure(profile);
	}

	public void setTextHidden(boolean isTextHidden) {
		view.setTextHidden(isTextHidden);
	}

	public void setTooltipHidden(boolean isTooltipHidden) {
		view.setTooltipHidden(isTooltipHidden);
	}

	public void setSize(BadgeSize size) {
		view.setSize(size);
	}

	/**
	 * Wiki configure
	 */
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		// get the user id from the descriptor, and pass to the other configure
		configure(widgetDescriptor.get(WidgetConstants.USERBADGE_WIDGET_ID_KEY));
	}

	/**
	 * Simple configure, without UserProfile
	 * 
	 * @param principalId
	 */
	public void configure(final String principalId) {
		this.principalId = principalId;
		profile = null;
		username = null;

		// get user profile and configure
		view.clear();
		view.showLoading();
		if (principalId != null && principalId.trim().length() > 0) {
			loadBadge();
		} else {
			view.showLoadError("Missing user ID");
		}
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
	 * 
	 * @param clickHandler
	 */
	public void setCustomClickHandler(ClickHandler clickHandler) {
		view.setCustomClickHandler(clickHandler);
	}

	public void setOpenInNewWindow() {
		view.setOpenInNewWindow();
	}

	public static UserProfile getUserProfileFromCache(String principalId, AdapterFactory adapterFactory, ClientCache clientCache) {
		String profileString = clientCache.get(principalId + WebConstants.USER_PROFILE_SUFFIX);
		if (profileString != null) {
			try {
				return new UserProfile(adapterFactory.createNew(profileString));
			} catch (JSONObjectAdapterException e) {
				// if any problems occur, try to get the user profile from with a rpc
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
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void addStyleNames(String style) {
		view.addStyleName(style);
	}

	public void setHeight(String height) {
		view.setHeight(height);
	}

	public void setDoNothingOnClick() {
		view.doNothingOnClick();
	}

	public void addContextCommand(String commandName, Callback callback) {
		view.addContextCommand(commandName, callback);
	}
}
