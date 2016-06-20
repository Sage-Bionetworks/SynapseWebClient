package org.sagebionetworks.web.client.widget.discussion;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
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
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidget;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ForumWidget implements ForumWidgetView.Presenter{

	public final static Boolean SHOW_THREAD_DETAILS_FOR_THREAD_LIST = false;
	public final static Boolean SHOW_THREAD_DETAILS_FOR_SINGLE_THREAD = true;
	public final static Boolean SHOW_REPLY_DETAILS_FOR_THREAD_LIST = false;
	public final static Boolean SHOW_REPLY_DETAILS_FOR_SINGLE_THREAD = true;

	//used to tell the discussion forum to show a single thread
	public final static String THREAD_ID_KEY = "threadId";
	ForumWidgetView view;

	NewDiscussionThreadModal newThreadModal;
	DiscussionThreadListWidget threadListWidget;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClient;
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	SingleDiscussionThreadWidget singleThreadWidget;

	String forumId;
	String entityId;
	Boolean isCurrentUserModerator;
	CallbackP<ParameterizedToken> paramChangeCallback;
	Callback urlChangeCallback;
	CallbackP<Boolean> emptyListCallback;
	Boolean isSingleThread;
	SubscribeButtonWidget subscribeToForumButton;
	Set<Long> moderatorIds;

	// From portal.properties, what thread should we show if no threads are available?
	public static final String DEFAULT_THREAD_ID_KEY = "org.sagebionetworks.portal.default_thread_id";
	public static DiscussionThreadBundle defaultThreadBundle;
	public SingleDiscussionThreadWidget defaultThreadWidget;

	@Inject
	public ForumWidget(
			final ForumWidgetView view,
			SynapseAlert synAlert,
			DiscussionForumClientAsync discussionForumClient,
			DiscussionThreadListWidget threadListWidget,
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
		view.setPresenter(this);
		view.setThreadList(threadListWidget.asWidget());
		view.setNewThreadModal(newThreadModal.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setSingleThread(singleThreadWidget.asWidget());
		view.setSubscribeButton(subscribeToForumButton.asWidget());
		view.setDefaultThreadWidget(defaultThreadWidget.asWidget());
		String defaultThreadId = globalApplicationState.getSynapseProperty(DEFAULT_THREAD_ID_KEY);
		initDefaultThread(defaultThreadId);
		emptyListCallback = new CallbackP<Boolean>(){
			@Override
			public void invoke(Boolean param) {
				view.setDefaultThreadWidgetVisible(!param);
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
		
		threadListWidget.setThreadIdClickedCallback(new CallbackP<String>() {
			@Override
			public void invoke(String threadId) {
				showThread(threadId);
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
		boolean showThreadDetails = true;
		boolean showReplyDetails = false;
		boolean isCurrentUserModerator = false;
		resetDefaultThreadDates();
		defaultThreadWidget.configure(defaultThreadBundle, isCurrentUserModerator, moderatorIds, deleteCallback, showThreadDetails, showReplyDetails);
		// show reminder on thread id click
		defaultThreadWidget.setThreadIdClickedCallback(new CallbackP<String>() {
			@Override
			public void invoke(String param) {
				view.showNewThreadTooltip();
			}
		});
		defaultThreadWidget.setReplyButtonVisible(false);
		defaultThreadWidget.setCommandsVisible(false);
	}
	
	public void updatePlaceToSingleThread(String threadId) {
		ParameterizedToken params = new ParameterizedToken("");
		params.put(THREAD_ID_KEY, threadId);
		paramChangeCallback.invoke(params);
	}
	
	public void updatePlaceToForum() {
		ParameterizedToken params = new ParameterizedToken("");
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
		//are we just showing a single thread, or the full list?
		if (params.containsKey(THREAD_ID_KEY)) {
			String threadId = params.get(THREAD_ID_KEY);
			showThread(threadId);
		} else {
			showForum();
		}
	}

	public void showThread(final String threadId) {
		isSingleThread = true;
		synAlert.clear();
		subscribeToForumButton.clear();
		updatePlaceToSingleThread(threadId);
		view.setSingleThreadUIVisible(true);
		view.setThreadListUIVisible(false);
		view.setNewThreadButtonVisible(false);
		view.setShowAllThreadsButtonVisible(true);
		view.setDefaultThreadWidgetVisible(false);
		discussionForumClient.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>(){

			@Override
			public void onFailure(Throwable caught) {
				view.setThreadHeaderVisible(false);
				view.setSingleThreadUIVisible(false);
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				view.setThreadHeaderVisible(true);
				singleThreadWidget.configure(result, isCurrentUserModerator, moderatorIds, new Callback(){
					@Override
					public void invoke() {
						showForum();
						urlChangeCallback.invoke();
					}
				}, SHOW_THREAD_DETAILS_FOR_SINGLE_THREAD, SHOW_REPLY_DETAILS_FOR_SINGLE_THREAD);
			}
		});
	}

	public void showForum() {
		isSingleThread = false;
		synAlert.clear();
		subscribeToForumButton.clear();
		threadListWidget.clear();
		updatePlaceToForum();
		view.setSingleThreadUIVisible(false);
		view.setThreadListUIVisible(true);
		view.setNewThreadButtonVisible(true);
		view.setShowAllThreadsButtonVisible(false);
		view.setDefaultThreadWidgetVisible(false);
		discussionForumClient.getForumByProjectId(entityId, new AsyncCallback<Forum>(){
			@Override
			public void onFailure(Throwable caught) {
				view.setThreadHeaderVisible(false);
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(final Forum forum) {
				view.setThreadHeaderVisible(true);
				forumId = forum.getId();
				subscribeToForumButton.configure(SubscriptionObjectType.FORUM, forumId);
				newThreadModal.configure(forumId, new Callback(){
					@Override
					public void invoke() {
						threadListWidget.configure(forumId, isCurrentUserModerator, moderatorIds, emptyListCallback);
					}
				});
				threadListWidget.configure(forumId, isCurrentUserModerator, moderatorIds, emptyListCallback);
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
		threadListWidget.configure(forumId, isCurrentUserModerator, moderatorIds, emptyListCallback);
	}

	@Override
	public Widget asWidget(){
		return view.asWidget();
	}

	@Override
	public void sortByReplies() {
		if (!isSingleThread) {
			threadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_REPLIES);
		}
	}

	@Override
	public void sortByViews() {
		if (!isSingleThread) {
			threadListWidget.sortBy(DiscussionThreadOrder.NUMBER_OF_VIEWS);
		}
	}

	@Override
	public void sortByActivity() {
		if (!isSingleThread) {
			threadListWidget.sortBy(DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY);
		}
	}
}
