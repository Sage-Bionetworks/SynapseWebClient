package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;

import com.google.inject.Inject;

public class DiscussionTab implements DiscussionTabView.Presenter{
	private final static Long PROJECT_VERSION_NUMBER = null;
	Tab tab;
	DiscussionTabView view;
	CookieProvider cookies;
	//use this token to navigate between threads within the discussion tab
	ParameterizedToken params;
	ForumWidget forumWidget;
	String entityName, entityId;

	@Inject
	public DiscussionTab(
			DiscussionTabView view,
			Tab tab,
			CookieProvider cookies,
			ForumWidget forumWidget
			) {
		this.view = view;
		this.tab = tab;
		this.cookies = cookies;
		this.forumWidget = forumWidget;
		// Necessary for "beta" badge.  Remove when bringing out of beta.
		view.updateWidth(tab);
		tab.configure("Discussion " + DisplayConstants.BETA_BADGE_HTML, view.asWidget());
		view.setPresenter(this);
		view.setForum(forumWidget.asWidget());
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(String entityId, String entityName, String areaToken, Boolean isCurrentUserModerator) {
		this.entityId = entityId;
		this.entityName = entityName;
		this.params = new ParameterizedToken(areaToken);
		forumWidget.configure(entityId, params, isCurrentUserModerator, DisplayUtils.isInTestWebsite(cookies), new Callback(){
			@Override
			public void invoke() {
				params.clear();
				updatePlace();
				tab.showTab();
			}
		});
		updatePlace();
		tab.setTabListItemVisible(DisplayUtils.isInTestWebsite(cookies));
	}

	/**
	 * Based on the current area parameters, update the address bar (push the url in to the browser history).
	 */
	public void updatePlace(){
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, PROJECT_VERSION_NUMBER, EntityArea.DISCUSSION, getCurrentAreaToken()));
	}

	public Tab asTab(){
		return tab;
	}

	public String getCurrentAreaToken() {
		return params.toString();
	}
}
