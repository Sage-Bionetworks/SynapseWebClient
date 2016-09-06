package org.sagebionetworks.web.client.widget.entity.tabs;

import java.util.Set;

import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.inject.Inject;

public class DiscussionTab implements DiscussionTabView.Presenter{
	private final static Long PROJECT_VERSION_NUMBER = null;
	Tab tab;
	DiscussionTabView view;
	//use this token to navigate between threads within the discussion tab
	ParameterizedToken params;
	ForumWidget forumWidget;
	String entityName, entityId;
	
	@Inject
	public DiscussionTab(
			DiscussionTabView view,
			Tab tab,
			ForumWidget forumWidget
			) {
		this.view = view;
		this.tab = tab;
		this.forumWidget = forumWidget;
		// Necessary for "beta" badge.  Remove when bringing out of beta.
		view.updateWidth(tab);
		tab.configure("Discussion", view.asWidget(), "Engage your collaborators in project specific Discussions.", WebConstants.DOCS_URL + "discussion.html");
		view.setPresenter(this);
		view.setForum(forumWidget.asWidget());
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(final String entityId, String entityName, String areaToken, Boolean isCurrentUserModerator, Set<Long> moderatorIds) {
		this.entityId = entityId;
		this.entityName = entityName;
		this.params = new ParameterizedToken(areaToken);
		CallbackP<ParameterizedToken> updateParamsCallback = new CallbackP<ParameterizedToken>(){
			@Override
			public void invoke(ParameterizedToken token) {
				updatePlace(new Synapse(entityId, PROJECT_VERSION_NUMBER, EntityArea.DISCUSSION, token.toString()));
			}
		};
		Callback updateURLCallback = new Callback() {
			@Override
			public void invoke() {
				tab.showTab();
			}
		};
		forumWidget.configure(entityId, params, isCurrentUserModerator, moderatorIds, updateParamsCallback, updateURLCallback);
	}

	/**
	 * Based on the current area parameters, update the address bar (push the url in to the browser history).
	 */
	public void updatePlace(Synapse newPlace){
		tab.setEntityNameAndPlace(entityName, newPlace);
	}

	public Tab asTab(){
		return tab;
	}

	public String getCurrentAreaToken() {
		return params.toString();
	}
}
