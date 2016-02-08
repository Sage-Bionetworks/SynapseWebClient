package org.sagebionetworks.web.client.presenter;

import java.util.HashMap;

import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SynapseForumView;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SynapseForumPresenter extends AbstractActivity implements SynapseForumView.Presenter, Presenter<SynapseForumPlace> {
	private SynapseForumPlace place;
	SynapseForumView view;
	//use this token to navigate between threads within the discussion tab
	ParameterizedToken params;
	 
	NewDiscussionThreadModal newThreadModal;
	DiscussionThreadListWidget threadListWidget;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClient;
	SynapseClientAsync synapseClient;
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	private String forumId;
	
	MarkdownWidget wikiPage;
	WikiPageKey pageKey;
	Boolean isCurrentUserModerator;
	CookieProvider cookies;
	
	@Inject
	public SynapseForumPresenter(
			SynapseForumView view,
			SynapseAlert synAlert,
			DiscussionForumClientAsync discussionForumClient,
			DiscussionThreadListWidget threadListWidget,
			NewDiscussionThreadModal newThreadModal,
			AuthenticationController authController,
			GlobalApplicationState globalApplicationState,
			MarkdownWidget wikiPage,
			SynapseClientAsync synapseClient,
			CookieProvider cookies
			) {
		this.view = view;
		this.synAlert = synAlert;
		this.threadListWidget = threadListWidget;
		this.newThreadModal = newThreadModal;
		this.discussionForumClient = discussionForumClient;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		this.wikiPage = wikiPage;
		this.synapseClient = synapseClient;
		this.cookies = cookies;
		view.setPresenter(this);
		view.setThreadList(threadListWidget.asWidget());
		view.setNewThreadModal(newThreadModal.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setWikiWidget(wikiPage.asWidget());
		isCurrentUserModerator = DiscussionTab.DEFAULT_MODERATOR_MODE;
	}


	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view);
		//get the wiki page key and entity id from properties
		loadWikiHelpContent();
		showForum(globalApplicationState.getSynapseProperty(WebConstants.FORUM_SYNAPSE_ID_PROPERTY));
	}
	

	@Override
	public void setPlace(SynapseForumPlace place) {
		this.place = place;
		this.view.setPresenter(this);
//		String threadId = place.getParam(DiscussionTab.THREAD_ID_KEY);
		isCurrentUserModerator = DisplayUtils.isInTestWebsite(cookies);
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
	
	public void showForum(String entityId) {
		discussionForumClient.getForumMetadata(entityId, new AsyncCallback<Forum>(){
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(final Forum forum) {
				forumId = forum.getId();
				newThreadModal.configure(forumId, new Callback(){
					@Override
					public void invoke() {
						threadListWidget.configure(forumId, DiscussionTab.DEFAULT_MODERATOR_MODE);
					}
				});
				threadListWidget.configure(forumId, DiscussionTab.DEFAULT_MODERATOR_MODE);
			}
		});
		view.setModeratorModeContainerVisibility(isCurrentUserModerator);
	}
	
	@Override
	public void onClickNewThread() {
		if (!authController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else {
			newThreadModal.show();
		}
	}

	@Override
	public void onModeratorModeChange() {
		threadListWidget.configure(forumId, view.getModeratorMode());
		newThreadModal.configure(forumId, new Callback(){
			@Override
			public void invoke() {
				threadListWidget.configure(forumId, view.getModeratorMode());
			}
		});
	}
	
	public String getCurrentAreaToken() {
		return params.toString();
	}
}
