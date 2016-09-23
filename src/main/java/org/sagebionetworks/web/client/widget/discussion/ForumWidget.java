package org.sagebionetworks.web.client.widget.discussion;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidget;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ForumWidget implements ForumWidgetView.Presenter{

	//used to tell the discussion forum to show a single thread
	public final static String THREAD_ID_KEY = "threadId";
	public final static String REPLY_ID_KEY = "replyId";
	ForumWidgetView view;

	NewDiscussionThreadModal newThreadModal;
	DiscussionThreadListWidget threadListWidget;
	StuAlert synAlert;
	DiscussionForumClientAsync discussionForumClient;
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	SingleDiscussionThreadWidget singleThreadWidget;
	DiscussionThreadListWidget deletedThreadListWidget;

	String forumId;
	String entityId;
	Boolean isCurrentUserModerator;
	CallbackP<ParameterizedToken> paramChangeCallback;
	Callback urlChangeCallback;
	CallbackP<Boolean> emptyListCallback;
	Boolean isSingleThread = false;
	SubscribeButtonWidget subscribeToForumButton;
	Set<Long> moderatorIds;
	ParameterizedToken params;
	
	// From portal.properties, what thread should we show if no threads are available?
	public static final String DEFAULT_THREAD_ID_KEY = "org.sagebionetworks.portal.default_thread_id";
	public static DiscussionThreadBundle defaultThreadBundle;
	public SingleDiscussionThreadWidget defaultThreadWidget;

	@Inject
	public ForumWidget(
			final ForumWidgetView view,
			StuAlert synAlert,
			DiscussionForumClientAsync discussionForumClient,
			DiscussionThreadListWidget threadListWidget,
			DiscussionThreadListWidget deletedThreadListWidget,
			NewDiscussionThreadModal newThreadModal,
			AuthenticationController authController,
			GlobalApplicationState globalApplicationState,
			SingleDiscussionThreadWidget singleThreadWidget,
			SubscribeButtonWidget subscribeToForumButton,
			SingleDiscussionThreadWidget defaultThreadWidget
			) {
		this.view = view;
		this.synAlert = synAlert;
		this.threadListWidget = threadListWidget;
		this.newThreadModal = newThreadModal;
		this.discussionForumClient = discussionForumClient;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		this.singleThreadWidget = singleThreadWidget;
		this.subscribeToForumButton = subscribeToForumButton;
		this.defaultThreadWidget = defaultThreadWidget;
		this.deletedThreadListWidget = deletedThreadListWidget;
		view.setPresenter(this);
		view.setThreadList(threadListWidget.asWidget());
		view.setNewThreadModal(newThreadModal.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setSingleThread(singleThreadWidget.asWidget());
		view.setSubscribeButton(subscribeToForumButton.asWidget());
		view.setDefaultThreadWidget(defaultThreadWidget.asWidget());
		view.setDeletedThreadList(deletedThreadListWidget.asWidget());
		String defaultThreadId = globalApplicationState.getSynapseProperty(DEFAULT_THREAD_ID_KEY);
		initDefaultThread(defaultThreadId);
		emptyListCallback = new CallbackP<Boolean>(){
			@Override
			public void invoke(Boolean param) {
				if (!isSingleThread) {
					view.setDefaultThreadWidgetVisible(!param);
					view.setThreadListUIVisible(param);
					view.setDeletedThreadListVisible(false);
				}
			}
		};
		Callback refreshThreadsCallback = new Callback() {
			@Override
			public void invoke() {
				refreshThreads();
			}
		};
		subscribeToForumButton.setOnSubscribeCallback(refreshThreadsCallback);
		subscribeToForumButton.setOnUnsubscribeCallback(refreshThreadsCallback);
		
		threadListWidget.setThreadIdClickedCallback(new CallbackP<DiscussionThreadBundle>() {
			@Override
			public void invoke(DiscussionThreadBundle bundle) {
				String replyId = null;
				showThread(bundle.getId(), replyId);
				urlChangeCallback.invoke();
			}
		});
		deletedThreadListWidget.setThreadIdClickedCallback(new CallbackP<DiscussionThreadBundle>() {
			@Override
			public void invoke(DiscussionThreadBundle bundle) {
				String replyId = null;
				showThread(bundle.getId(), replyId);
				urlChangeCallback.invoke();
			}
		});
		singleThreadWidget.setReplyIdCallback(new CallbackP<String>() {
			@Override
			public void invoke(String replyId) {
				updatePlaceToReply(replyId);
				urlChangeCallback.invoke();
			}
		});
	}
	
	public void initDefaultThread(String defaultThreadId) {
		if (defaultThreadBundle == null) {
			//get default thread bundle
			discussionForumClient.getThread(defaultThreadId, new AsyncCallback<DiscussionThreadBundle>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
				public void onSuccess(DiscussionThreadBundle threadBundle) {
					defaultThreadBundle = createDefaultThread(threadBundle);
					initDefaultThreadWidget();
				};
			});
		} else {
			//configure using default thread bundle
			initDefaultThreadWidget();
		}
	}
	
	public DiscussionThreadBundle createDefaultThread(DiscussionThreadBundle stuThread) {
		DiscussionThreadBundle defaultThreadBundle = new DiscussionThreadBundle();
		defaultThreadBundle.setProjectId(stuThread.getProjectId());
		defaultThreadBundle.setTitle(stuThread.getTitle());
		defaultThreadBundle.setMessageKey(stuThread.getMessageKey());
		defaultThreadBundle.setCreatedBy(stuThread.getCreatedBy());
		defaultThreadBundle.setId(stuThread.getId());
		defaultThreadBundle.setActiveAuthors(new ArrayList<String>());
		defaultThreadBundle.setIsEdited(false);
		defaultThreadBundle.setIsPinned(false);
		defaultThreadBundle.setNumberOfReplies(0L);
		defaultThreadBundle.setNumberOfViews(1L);
		defaultThreadBundle.setIsDeleted(false);
		return defaultThreadBundle;
	}
	
	public void resetDefaultThreadDates() {
		Date now = new Date();
		defaultThreadBundle.setCreatedOn(now);
		defaultThreadBundle.setModifiedOn(now);
		defaultThreadBundle.setLastActivity(now);
	}
	
	public void initDefaultThreadWidget() {
		Set<Long> moderatorIds = new HashSet<Long>();
		Callback deleteCallback = null;
		boolean isCurrentUserModerator = false;
		resetDefaultThreadDates();
		String replyId = null;
		defaultThreadWidget.configure(defaultThreadBundle, replyId, isCurrentUserModerator, moderatorIds, deleteCallback);
		// show reminder on thread id click
		defaultThreadWidget.setThreadIdClickedCallback(new CallbackP<String>() {
			@Override
			public void invoke(String param) {
				view.showNewThreadTooltip();
			}
		});
		defaultThreadWidget.setReplyListVisible(false);
		defaultThreadWidget.setReplyTextBoxVisible(false);
		defaultThreadWidget.setCommandsVisible(false);
	}
	
	public void updatePlaceToSingleThread(String threadId) {
		params.clear();
		params.put(THREAD_ID_KEY, threadId);
		paramChangeCallback.invoke(params);
	}
	
	public void updatePlaceToReply(String replyId) {
		if (replyId != null) {
			params.put(REPLY_ID_KEY, replyId);	
		} else {
			params.remove(REPLY_ID_KEY);
		}
		
		paramChangeCallback.invoke(params);
	}
	
	public void updatePlaceToForum() {
		params.clear();
		paramChangeCallback.invoke(params);
	}
	
	public void configure(String entityId, ParameterizedToken params,
			Boolean isCurrentUserModerator,
			Set<Long> moderatorIds,
			CallbackP<ParameterizedToken> paramChangeCallback, Callback urlChangeCallback) {
		this.entityId = entityId;
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.moderatorIds = moderatorIds;
		this.paramChangeCallback = paramChangeCallback;
		this.urlChangeCallback = urlChangeCallback;
		this.params = params;
		initView();
		//are we just showing a single thread, or the full list?
		if (params.containsKey(THREAD_ID_KEY)) {
			String threadId = params.get(THREAD_ID_KEY);
			showThread(threadId, params.get(REPLY_ID_KEY));
		} else {
			showForum();
		}
	}

	public void initView() {
		view.setMainContainerVisible(false);
		view.setSingleThreadUIVisible(false);
		view.setThreadListUIVisible(false);
		view.setNewThreadButtonVisible(false);
		view.setShowAllThreadsButtonVisible(false);
		view.setDefaultThreadWidgetVisible(false);
		view.setDeletedThreadListVisible(false);
		view.setDeletedThreadButtonVisible(false);
	}

	public void showThread(final String threadId, final String replyId) {
		isSingleThread = true;
		synAlert.clear();
		subscribeToForumButton.clear();
		updatePlaceToSingleThread(threadId);
		discussionForumClient.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				singleThreadWidget.configure(result, replyId, isCurrentUserModerator, moderatorIds, new Callback(){
					@Override
					public void invoke() {
						showForum();
						urlChangeCallback.invoke();
					}
				});
				view.setSingleThreadUIVisible(true);
				view.setShowAllThreadsButtonVisible(true);
				view.setMainContainerVisible(true);
			}
		});
	}

	public void showForum() {
		isSingleThread = false;
		synAlert.clear();
		subscribeToForumButton.clear();
		threadListWidget.clear();
		updatePlaceToForum();
		discussionForumClient.getForumByProjectId(entityId, new AsyncCallback<Forum>(){
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(final Forum forum) {
				forumId = forum.getId();
				subscribeToForumButton.configure(SubscriptionObjectType.FORUM, forumId);
				newThreadModal.configure(forumId, new Callback(){
					@Override
					public void invoke() {
						threadListWidget.configure(forumId, isCurrentUserModerator,
								moderatorIds, emptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
					}
				});
				threadListWidget.configure(forumId, isCurrentUserModerator,
						moderatorIds, emptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
				view.setThreadListUIVisible(true);
				view.setNewThreadButtonVisible(true);
				view.setDeletedThreadButtonVisible(isCurrentUserModerator);
				view.setMainContainerVisible(true);
			}
		});
	}

	@Override
	public void onClickShowAllThreads() {
		showForum();
		urlChangeCallback.invoke();
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

	private void refreshThreads() {
		threadListWidget.configure(forumId, isCurrentUserModerator, moderatorIds,
				emptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
	}

	@Override
	public Widget asWidget(){
		return view.asWidget();
	}

	@Override
	public void onClickDeletedThreadButton() {
		if (view.isDeletedThreadListVisible()) {
			view.setDeletedThreadListVisible(false);
			view.setDeletedThreadButtonIcon(IconType.TOGGLE_RIGHT);
		} else {
			view.setDeletedThreadListVisible(true);
			view.setDeletedThreadButtonIcon(IconType.TOGGLE_DOWN);
			deletedThreadListWidget.configure(forumId, isCurrentUserModerator,
					moderatorIds, null, DiscussionFilter.DELETED_ONLY);
		}
	}
}
