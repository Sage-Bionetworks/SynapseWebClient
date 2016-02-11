package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ForumWidget implements ForumWidgetView.Presenter{

	public final static Boolean DEFAULT_MODERATOR_MODE = false;

	//used to tell the discussion forum to show a single thread
	public final static String THREAD_ID_KEY = "threadId";
	ForumWidgetView view;

	NewDiscussionThreadModal newThreadModal;
	DiscussionThreadListWidget threadListWidget;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClient;
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	DiscussionThreadWidget singleThreadWidget;

	String forumId;
	String entityId;
	Boolean isCurrentUserModerator;
	Callback showAllThreadsCallback;

	@Inject
	public ForumWidget(
			ForumWidgetView view,
			SynapseAlert synAlert,
			DiscussionForumClientAsync discussionForumClient,
			DiscussionThreadListWidget threadListWidget,
			NewDiscussionThreadModal newThreadModal,
			AuthenticationController authController,
			GlobalApplicationState globalApplicationState,
			DiscussionThreadWidget singleThreadWidget
			) {
		this.view = view;
		this.synAlert = synAlert;
		this.threadListWidget = threadListWidget;
		this.newThreadModal = newThreadModal;
		this.discussionForumClient = discussionForumClient;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		this.singleThreadWidget = singleThreadWidget;
		view.setPresenter(this);
		view.setThreadList(threadListWidget.asWidget());
		view.setNewThreadModal(newThreadModal.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setSingleThread(singleThreadWidget.asWidget());
	}

	public void configure(String entityId, ParameterizedToken params,
			Boolean isCurrentUserModerator, Callback showAllThreadsCallback) {
		this.entityId = entityId;
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.showAllThreadsCallback = showAllThreadsCallback;
		synAlert.clear();
		//are we just showing a single thread, or the full list?
		if (params.containsKey(THREAD_ID_KEY)) {
			String threadId = params.get(THREAD_ID_KEY);
			showThread(threadId);
		} else {
			showForum();
		}
	}

	public void showThread(String threadId) {
		view.setSingleThreadUIVisible(true);
		view.setThreadListUIVisible(false);
		configureSingleThread(threadId);
	}

	private void configureSingleThread(final String threadId) {
		discussionForumClient.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				singleThreadWidget.configure(result, isCurrentUserModerator, new Callback(){

					@Override
					public void invoke() {
						configureSingleThread(threadId);
					}
				});
				if (singleThreadWidget.isThreadCollapsed()) {
					singleThreadWidget.toggleThread();
				}
			}
		});
	}

	public void showForum() {
		view.setSingleThreadUIVisible(false);
		view.setThreadListUIVisible(true);
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
						threadListWidget.configure(forumId, DEFAULT_MODERATOR_MODE);
					}
				});
				threadListWidget.configure(forumId, DEFAULT_MODERATOR_MODE);
			}
		});
		view.setModeratorModeContainerVisibility(isCurrentUserModerator);
	}

	@Override
	public void onClickShowAllThreads() {
		synAlert.clear();
		if (showAllThreadsCallback != null) {
			showAllThreadsCallback.invoke();
		}
		showForum();
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

	@Override
	public Widget asWidget(){
		return view.asWidget();
	}
}
