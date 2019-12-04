package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.inject.Inject;

public class DiscussionTab implements DiscussionTabView.Presenter {
	private final static Long PROJECT_VERSION_NUMBER = null;
	Tab tab;
	DiscussionTabView view;
	// use this token to navigate between threads within the discussion tab
	ParameterizedToken params;
	ForumWidget forumWidget;
	String entityName, entityId;
	GlobalApplicationState globalAppState;
	SynapseProperties synapseProperties;
	PortalGinInjector ginInjector;
	@Inject
	public DiscussionTab(Tab tab, PortalGinInjector ginInjector) {
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure("Discussion", "Engage your collaborators in project specific Discussions.", WebConstants.DOCS_URL + "discussion.html", EntityArea.DISCUSSION);
	}

	public void lazyInject() {
		if (view == null) {
			this.view = ginInjector.getDiscussionTabView();
			this.forumWidget = ginInjector.getForumWidget();
			this.globalAppState = ginInjector.getGlobalApplicationState();
			this.synapseProperties = ginInjector.getSynapseProperties();
			view.setPresenter(this);
			view.setForum(forumWidget.asWidget());
			tab.setContent(view.asWidget());
		}
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(final String entityId, String entityName, EntityBundle projectBundle, String areaToken, Boolean isCurrentUserModerator) {
		lazyInject();
		this.entityId = entityId;
		this.entityName = entityName;
		this.params = new ParameterizedToken(areaToken);
		checkForSynapseForum();
		CallbackP<ParameterizedToken> updateParamsCallback = token -> {
			updatePlace(new Synapse(entityId, PROJECT_VERSION_NUMBER, EntityArea.DISCUSSION, token.toString()));
		};
		Callback updateURLCallback = new Callback() {
			@Override
			public void invoke() {
				tab.showTab();
			}
		};
		tab.configureEntityActionController(projectBundle, true, null);
		forumWidget.configure(entityId, params, isCurrentUserModerator, tab.getEntityActionMenu(), updateParamsCallback, updateURLCallback);
		// SWC-3994: initialize tab Place to set the initial area token
		updateParamsCallback.invoke(params);
	}

	public void updateActionMenuCommands() {
		forumWidget.updateActionMenuCommands();
	}

	public void checkForSynapseForum() {
		String forumSynapseId = synapseProperties.getSynapseProperty(WebConstants.FORUM_SYNAPSE_ID_PROPERTY);
		if (forumSynapseId.equals(entityId)) {
			SynapseForumPlace forumPlace = new SynapseForumPlace(ParameterizedToken.DEFAULT_TOKEN);
			forumPlace.setParameterizedToken(params);
			globalAppState.getPlaceChanger().goTo(forumPlace);
		}
	}

	/**
	 * Based on the current area parameters, update the address bar (push the url in to the browser
	 * history).
	 */
	public void updatePlace(Synapse newPlace) {
		tab.setEntityNameAndPlace(entityName, newPlace);
	}

	public Tab asTab() {
		return tab;
	}

	public String getCurrentAreaToken() {
		return params.toString();
	}
}
