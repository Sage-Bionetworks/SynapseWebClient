package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.repo.model.EntityBundle.*;

import java.util.HashMap;
import java.util.Set;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.SynapseForumView;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.users.AclUtils;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SynapseForumPresenter extends AbstractActivity implements SynapseForumView.Presenter, Presenter<SynapseForumPlace> {
	public static final Boolean DEFAULT_IS_MODERATOR = false;
	private SynapseForumPlace place;
	SynapseForumView view;

	SynapseAlert synAlert;
	SynapseClientAsync synapseClient;
	GlobalApplicationState globalApplicationState;
	
	MarkdownWidget wikiPage;
	WikiPageKey pageKey;
	CookieProvider cookies;
	ForumWidget forumWidget;

	@Inject
	public SynapseForumPresenter(
			SynapseForumView view,
			SynapseAlert synAlert,
			GlobalApplicationState globalApplicationState,
			MarkdownWidget wikiPage,
			SynapseClientAsync synapseClient,
			CookieProvider cookies,
			ForumWidget forumWidget
			) {
		this.view = view;
		this.synAlert = synAlert;
		this.globalApplicationState = globalApplicationState;
		this.wikiPage = wikiPage;
		this.synapseClient = synapseClient;
		this.cookies = cookies;
		this.forumWidget = forumWidget;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setWikiWidget(wikiPage.asWidget());
		view.setForumWidget(forumWidget.asWidget());
	}


	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view);
		//get the wiki page key and entity id from properties
		loadWikiHelpContent();
		showForum(globalApplicationState.getSynapseProperty(WebConstants.FORUM_SYNAPSE_ID_PROPERTY));
	}

	public void showForum(final String entityId) {
		// get the moderator ids, and then show the forum
		AsyncCallback<AccessControlList> callback = new AsyncCallback<AccessControlList>() {
			@Override
			public void onSuccess(AccessControlList acl) {
				Set<Long> moderatorIds = AclUtils.getPrincipalIds(acl, ACCESS_TYPE.MODERATE);
				showForum(entityId, moderatorIds);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}	
		};
		synapseClient.getEntityBenefactorAcl(entityId, callback);
	}

	public void showForum(String entityId, Set<Long> moderatorIds) {
		CallbackP<ParameterizedToken> paramChangeCallback = new CallbackP<ParameterizedToken>(){
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
		forumWidget.configure(entityId, place.getParameterizedToken(), DEFAULT_IS_MODERATOR, moderatorIds, paramChangeCallback, urlChangeCallback);
	}
	
	@Override
	public void setPlace(SynapseForumPlace place) {
		this.place = place;
		this.view.setPresenter(this);
	}
	
	public void loadWikiHelpContent() {
		if (pageKey == null) {
			//get the wiki page key, then load the content
			synapseClient.getPageNameToWikiKeyMap(new AsyncCallback<HashMap<String,WikiPageKey>>() {
				@Override
				public void onSuccess(HashMap<String,WikiPageKey> result) {
					pageKey = result.get(WebConstants.FORUM);
					loadWikiHelpContent(pageKey);
				};
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
			});
		} else {
			loadWikiHelpContent(pageKey);
		}
	}
	
	public void loadWikiHelpContent(WikiPageKey key) {
		boolean isIgnoreLoadingFailure = false;
		wikiPage.loadMarkdownFromWikiPage(key, isIgnoreLoadingFailure);
	}

	public String getCurrentAreaToken() {
		return place.getParameterizedToken().toString();
	}
}
