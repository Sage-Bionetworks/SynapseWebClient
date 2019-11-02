package org.sagebionetworks.web.client.widget.discussion;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ForumWidget implements ForumWidgetView.Presenter {
	public final static Long MODERATOR_LIMIT = 20L;

	// used to tell the discussion forum to show a single thread
	public final static String THREAD_ID_KEY = "threadId";
	public final static String REPLY_ID_KEY = "replyId";
	ForumWidgetView view;

	NewDiscussionThreadModal newThreadModal;
	DiscussionThreadListWidget threadListWidget;
	StuAlert stuAlert;
	DiscussionForumClientAsync discussionForumClient;
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	SingleDiscussionThreadWidget singleThreadWidget;
	DiscussionThreadListWidget deletedThreadListWidget;
	SynapseJavascriptClient jsClient;
	String forumId, threadId;
	String entityId;
	Boolean isCurrentUserModerator;
	CallbackP<ParameterizedToken> paramChangeCallback;
	Callback urlChangeCallback;
	CallbackP<Boolean> emptyListCallback;
	Boolean isSingleThread = false;
	SubscribeButtonWidget subscribeToForumButton;
	Set<String> moderatorIds = new HashSet<String>();
	ParameterizedToken params;
	Topic forumTopic = new Topic();
	ActionMenuWidget actionMenu;

	// From portal.properties, what thread should we show if no threads are available?
	public static final String DEFAULT_THREAD_ID_KEY = "org.sagebionetworks.portal.default_thread_id";
	public static DiscussionThreadBundle defaultThreadBundle;
	public SingleDiscussionThreadWidget defaultThreadWidget;
	public SubscribersWidget forumSubscribersWidget;
	public DiscussionThreadBundle currentThreadBundle;
	boolean isForumConfigured;

	@Inject
	public ForumWidget(final ForumWidgetView view, StuAlert stuAlert, DiscussionForumClientAsync discussionForumClient, DiscussionThreadListWidget threadListWidget, DiscussionThreadListWidget deletedThreadListWidget, NewDiscussionThreadModal newThreadModal, AuthenticationController authController, GlobalApplicationState globalApplicationState, SingleDiscussionThreadWidget singleThreadWidget, SubscribeButtonWidget subscribeToForumButton, SingleDiscussionThreadWidget defaultThreadWidget, SubscribersWidget forumSubscribersWidget, SynapseJavascriptClient jsClient, SynapseProperties synapseProperties) {
		this.view = view;
		this.stuAlert = stuAlert;
		this.threadListWidget = threadListWidget;
		this.newThreadModal = newThreadModal;
		this.discussionForumClient = discussionForumClient;
		fixServiceEntryPoint(discussionForumClient);
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		this.singleThreadWidget = singleThreadWidget;
		this.subscribeToForumButton = subscribeToForumButton;
		this.defaultThreadWidget = defaultThreadWidget;
		this.deletedThreadListWidget = deletedThreadListWidget;
		this.forumSubscribersWidget = forumSubscribersWidget;
		this.jsClient = jsClient;
		view.setPresenter(this);
		view.setThreadList(threadListWidget.asWidget());
		view.setNewThreadModal(newThreadModal.asWidget());
		view.setAlert(stuAlert.asWidget());
		view.setSingleThread(singleThreadWidget.asWidget());
		view.setSubscribeButton(subscribeToForumButton.asWidget());
		view.setDefaultThreadWidget(defaultThreadWidget.asWidget());
		view.setDeletedThreadList(deletedThreadListWidget.asWidget());
		view.setSubscribersWidget(forumSubscribersWidget.asWidget());
		String defaultThreadId = synapseProperties.getSynapseProperty(DEFAULT_THREAD_ID_KEY);
		forumTopic.setObjectType(SubscriptionObjectType.FORUM);
		initDefaultThread(defaultThreadId);
		emptyListCallback = new CallbackP<Boolean>() {
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
				ForumWidget.this.forumSubscribersWidget.configure(forumTopic);
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
			// get default thread bundle
			jsClient.getThread(defaultThreadId, new AsyncCallback<DiscussionThreadBundle>() {
				@Override
				public void onFailure(Throwable caught) {
					stuAlert.handleException(caught);
				}

				public void onSuccess(DiscussionThreadBundle threadBundle) {
					defaultThreadBundle = createDefaultThread(threadBundle);
					initDefaultThreadWidget();
				};
			});
		} else {
			// configure using default thread bundle
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
		Set<String> moderatorIds = new HashSet<String>();
		Callback deleteCallback = null;
		boolean isCurrentUserModerator = false;
		resetDefaultThreadDates();
		String replyId = null;
		defaultThreadWidget.configure(defaultThreadBundle, replyId, isCurrentUserModerator, moderatorIds, null, deleteCallback);
		defaultThreadWidget.setReplyListVisible(false);
		defaultThreadWidget.setNewReplyContainerVisible(false);
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

	public void configure(String entityId, final ParameterizedToken params, Boolean isCurrentUserModerator, ActionMenuWidget actionMenu, CallbackP<ParameterizedToken> paramChangeCallback, Callback urlChangeCallback) {
		isForumConfigured = false;
		this.entityId = entityId;
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.paramChangeCallback = paramChangeCallback;
		this.urlChangeCallback = urlChangeCallback;
		this.params = params;
		this.threadId = null;
		this.actionMenu = actionMenu;
		moderatorIds.clear();
		resetView();
		// get Forum and its moderators
		loadForum(entityId, new Callback() {
			@Override
			public void invoke() {
				// are we just showing a single thread, or the full list?
				if (params.containsKey(THREAD_ID_KEY)) {
					showThread(params.get(THREAD_ID_KEY), params.get(REPLY_ID_KEY));
				} else {
					showForum();
				}
			}
		});
		configureActionMenu();
	}

	public void loadForum(String entityId, final Callback callback) {
		stuAlert.clear();
		jsClient.getForumByProjectId(entityId, new AsyncCallback<Forum>() {
			@Override
			public void onFailure(Throwable caught) {
				stuAlert.handleException(caught);
			}

			@Override
			public void onSuccess(final Forum forum) {
				forumId = forum.getId();
				loadModerators(forumId, 0L, callback);
			}
		});
	}

	public void loadModerators(final String forumId, final Long offset, final Callback callback) {
		stuAlert.clear();
		jsClient.getModerators(forumId, MODERATOR_LIMIT, offset, new AsyncCallback<PaginatedIds>() {

			@Override
			public void onFailure(Throwable caught) {
				stuAlert.handleException(caught);
			}

			@Override
			public void onSuccess(PaginatedIds result) {
				moderatorIds.addAll(result.getResults());
				if (result.getTotalNumberOfResults() > offset + MODERATOR_LIMIT) {
					loadModerators(forumId, offset + MODERATOR_LIMIT, callback);
				} else {
					if (callback != null) {
						callback.invoke();
					}
				}
			}
		});
	}

	public void resetView() {
		view.setMainContainerVisible(false);
		view.setSingleThreadUIVisible(false);
		view.setNewThreadButtonVisible(false);
		view.setThreadListUIVisible(false);
		view.setShowAllThreadsButtonVisible(false);
		view.setSortRepliesButtonVisible(false);
		view.setDefaultThreadWidgetVisible(false);
		view.setDeletedThreadListVisible(false);
		view.setSubscribersWidgetVisible(false);
	}

	public void showThread(String threadId, final String replyId) {
		resetView();
		this.threadId = threadId;
		isSingleThread = true;
		stuAlert.clear();
		subscribeToForumButton.clear();
		updatePlaceToSingleThread(threadId);
		jsClient.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>() {

			@Override
			public void onFailure(Throwable caught) {
				stuAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				currentThreadBundle = result;
				singleThreadWidget.configure(result, replyId, isCurrentUserModerator, moderatorIds, actionMenu, new Callback() {
					@Override
					public void invoke() {
						isForumConfigured = false;
						ForumWidget.this.threadId = null;
						onClickShowAllThreads();
					}
				});

				view.setSingleThreadUIVisible(true);
				view.setShowAllThreadsButtonVisible(true);
				view.setSortRepliesButtonVisible(true);
				view.setMainContainerVisible(true);
				updateActionMenuCommands();
			}
		});
	}

	public void showForum() {
		resetView();
		isSingleThread = false;
		stuAlert.clear();
		subscribeToForumButton.clear();
		updatePlaceToForum();
		subscribeToForumButton.configure(SubscriptionObjectType.FORUM, forumId, actionMenu);
		newThreadModal.configure(forumId, new Callback() {
			@Override
			public void invoke() {
				threadListWidget.configure(forumId, isCurrentUserModerator, moderatorIds, emptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
			}
		});
		view.setThreadListUIVisible(true);
		view.setNewThreadButtonVisible(true);
		view.setMainContainerVisible(true);
		view.setSubscribersWidgetVisible(true);
		if (!isForumConfigured) {
			isForumConfigured = true;
			threadListWidget.clear();
			threadListWidget.configure(forumId, isCurrentUserModerator, moderatorIds, emptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
			forumTopic.setObjectId(forumId);
			forumSubscribersWidget.configure(forumTopic);
		}
		updateActionMenuCommands();
	}

	public void configureActionMenu() {
		if (actionMenu != null) {
			// add listener for forum commands
			actionMenu.setActionListener(Action.CREATE_THREAD, new ActionMenuWidget.ActionListener() {
				@Override
				public void onAction(Action action) {
					onClickNewThread();
				}
			});
			actionMenu.setActionListener(Action.SHOW_DELETED_THREADS, new ActionMenuWidget.ActionListener() {
				@Override
				public void onAction(Action action) {
					onClickDeletedThreadCommand();
				}
			});
		}
	}

	public void updateActionMenuCommands() {
		if (actionMenu != null) {
			// show thread commands if single thread, show forum commands if forum.
			actionMenu.setActionVisible(Action.FOLLOW, true);
			actionMenu.setActionVisible(Action.CREATE_THREAD, !isSingleThread);
			actionMenu.setActionVisible(Action.SHOW_DELETED_THREADS, !isSingleThread && isCurrentUserModerator);
			actionMenu.setActionVisible(Action.EDIT_THREAD, isSingleThread && currentThreadBundle.getCreatedBy().equals(authController.getCurrentUserPrincipalId()));
			actionMenu.setActionVisible(Action.PIN_THREAD, isSingleThread && isCurrentUserModerator);
			if (!isSingleThread) {
				actionMenu.setActionVisible(Action.RESTORE_THREAD, false);
				actionMenu.setActionVisible(Action.DELETE_THREAD, false);
			}
		}
		updateActionMenuDeletedThreadsCommand();
	}

	@Override
	public void onClickShowAllThreads() {
		showForum();
		urlChangeCallback.invoke();
		// jump to single thread that we just showed (if set)
		if (threadId != null) {
			threadListWidget.scrollToThread(threadId);
		}
	}

	public void onClickNewThread() {
		if (!authController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else {
			newThreadModal.show();
		}
	}

	private void refreshThreads() {
		threadListWidget.configure(forumId, isCurrentUserModerator, moderatorIds, emptyListCallback, DiscussionFilter.EXCLUDE_DELETED);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void onClickDeletedThreadCommand() {
		if (view.isDeletedThreadListVisible()) {
			view.setDeletedThreadListVisible(false);
		} else {
			view.setDeletedThreadListVisible(true);
			deletedThreadListWidget.configure(forumId, isCurrentUserModerator, moderatorIds, null, DiscussionFilter.DELETED_ONLY);
		}
		updateActionMenuDeletedThreadsCommand();
	}

	private void updateActionMenuDeletedThreadsCommand() {
		if (actionMenu != null) {
			String commandName = view.isDeletedThreadListVisible() ? "Hide Deleted Threads" : "Show Deleted Threads";
			actionMenu.setActionText(Action.SHOW_DELETED_THREADS, commandName);
		}
	}

	@Override
	public void onSortReplies(boolean ascending) {
		// no longer looking at a single reply if setting sort
		updatePlaceToReply(null);
		urlChangeCallback.invoke();
		singleThreadWidget.setSortDirection(ascending);
	}
}
