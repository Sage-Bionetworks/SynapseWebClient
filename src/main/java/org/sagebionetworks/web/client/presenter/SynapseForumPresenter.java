package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.SynapseForumView;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SynapseForumPresenter extends AbstractActivity implements Presenter<SynapseForumPlace> {
	public static final Boolean DEFAULT_IS_MODERATOR = false;
	private SynapseForumPlace place;
	SynapseForumView view;

	GlobalApplicationState globalApplicationState;
	ForumWidget forumWidget;
	SynapseProperties synapseProperties;

	@Inject
	public SynapseForumPresenter(SynapseForumView view, GlobalApplicationState globalApplicationState, ForumWidget forumWidget, SynapseProperties synapseProperties) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.forumWidget = forumWidget;
		this.synapseProperties = synapseProperties;
		view.setForumWidget(forumWidget.asWidget());
	}


	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view);
		showForum(synapseProperties.getSynapseProperty(WebConstants.FORUM_SYNAPSE_ID_PROPERTY));
	}

	public void showForum(String entityId) {
		CallbackP<ParameterizedToken> paramChangeCallback = new CallbackP<ParameterizedToken>() {
			@Override
			public void invoke(ParameterizedToken token) {
				// handle token changes
				place.setParameterizedToken(token);
			}
		};
		Callback urlChangeCallback = new Callback() {
			@Override
			public void invoke() {
				// push the new place up to the url (with params that may have been updated)
				globalApplicationState.pushCurrentPlace(place);
			}
		};
		ActionMenuWidget actionMenu = null;
		forumWidget.configure(entityId, place.getParameterizedToken(), DEFAULT_IS_MODERATOR, actionMenu, paramChangeCallback, urlChangeCallback);
	}

	@Override
	public void setPlace(SynapseForumPlace place) {
		this.place = place;
	}

	public String getCurrentAreaToken() {
		return place.getParameterizedToken().toString();
	}
}
