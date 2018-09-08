package org.sagebionetworks.web.client.widget.discussion;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.Set;

import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.discussion.modal.EditDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.refresh.ReplyCountAlert;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SingleDiscussionThreadWidget implements SingleDiscussionThreadWidgetView.Presenter{

	public static final String PIN_THREAD_ACTION_TEXT = "Pin Thread";
	public static final String UNPIN_THREAD_ACTION_TEXT = "Unpin Thread";
	private static final DiscussionReplyOrder DEFAULT_ORDER = DiscussionReplyOrder.CREATED_ON;
	public static final Boolean DEFAULT_ASCENDING = true;
	public static final Long LIMIT = 30L;
	private static final DiscussionFilter DEFAULT_FILTER = DiscussionFilter.EXCLUDE_DELETED;

	private static final String CONFIRM_RESTORE_DIALOG_TITLE = "Confirm Restoration";
	private static final String DELETE_CONFIRM_MESSAGE = "Are you sure you want to delete this thread?";
	private static final String DELETE_SUCCESS_TITLE = "Thread deleted";
	private static final String DELETE_SUCCESS_MESSAGE = "A thread has been deleted.";
	private static final String RESTORE_SUCCESS_TITLE = "Thread restored";
	private static final String RESTORE_SUCCESS_MESSAGE = "A thread has been restored.";
	public static final String CREATED_ON_PREFIX = "posted ";
	public static final String RESTORE_CONFIRM_MESSAGE = "Are you sure you want to restore this thread?";
	SingleDiscussionThreadWidgetView view;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClientAsync;
	SynapseJavascriptClient jsClient;
	PortalGinInjector ginInjector;
	DateTimeUtils dateTimeUtils;
	UserBadge authorWidget;
	RequestBuilderWrapper requestBuilder;
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	MarkdownWidget markdownWidget;
	LoadMoreWidgetContainer repliesContainer;
	SubscribeButtonWidget subscribeButtonWidget;
	NewReplyWidget newReplyWidget;
	NewReplyWidget secondNewReplyWidget;
	private CallbackP<String> threadIdClickedCallback, replyIdCallback;
	
	private Long offset;
	private DiscussionReplyOrder order;
	private Boolean ascending;
	private String threadId, replyId;
	private String messageKey;
	private Boolean isCurrentUserModerator;
	private String title;
	private Callback deleteOrRestoreCallback;
	private String projectId;
	private Callback refreshCallback;
	private Set<String> moderatorIds;
	private boolean isThreadDeleted;
	private SubscribersWidget threadSubscribersWidget;
	Topic threadTopic = new Topic();
	private ActionMenuWidget actionMenu;
	ActionMenuWidget.ActionListener editActionListener, unpinActionListener, pinActionListener, deleteActionListener, restoreActionListener;
	Boolean isPinned;
	PopupUtilsView popupUtils;
	ClientCache clientCache;
	String message;
	
	@Inject
	public SingleDiscussionThreadWidget(
			SingleDiscussionThreadWidgetView view,
			SynapseAlert synAlert,
			UserBadge authorWidget,
			DiscussionForumClientAsync discussionForumClientAsync,
			PortalGinInjector ginInjector,
			DateTimeUtils dateTimeUtils,
			RequestBuilderWrapper requestBuilder,
			AuthenticationController authController,
			GlobalApplicationState globalApplicationState,
			MarkdownWidget markdownWidget,
			LoadMoreWidgetContainer loadMoreWidgetContainer,
			SubscribeButtonWidget subscribeButtonWidget,
			NewReplyWidget newReplyWidget,
			NewReplyWidget secondNewReplyWidget,
			SubscribersWidget threadSubscribersWidget,
			SynapseJavascriptClient jsClient,
			PopupUtilsView popupUtils,
			ClientCache clientCache
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.dateTimeUtils = dateTimeUtils;
		this.synAlert = synAlert;
		this.authorWidget = authorWidget;
		this.discussionForumClientAsync = discussionForumClientAsync;
		fixServiceEntryPoint(discussionForumClientAsync);
		this.requestBuilder = requestBuilder;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		
		this.markdownWidget = markdownWidget;
		this.repliesContainer = loadMoreWidgetContainer;
		this.subscribeButtonWidget = subscribeButtonWidget;
		this.newReplyWidget = newReplyWidget;
		this.secondNewReplyWidget = secondNewReplyWidget;
		this.threadSubscribersWidget = threadSubscribersWidget;
		this.jsClient = jsClient;
		this.popupUtils = popupUtils;
		this.clientCache = clientCache;
		
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		view.setAuthor(authorWidget.asWidget());
		view.setMarkdownWidget(markdownWidget.asWidget());
		view.setSubscribeButtonWidget(subscribeButtonWidget.asWidget());
		view.setNewReplyContainer(newReplyWidget.asWidget());
		view.setSecondNewReplyContainer(secondNewReplyWidget.asWidget());
		view.setSubscribersWidget(threadSubscribersWidget.asWidget());
		Callback refreshSubscribersCallback = new Callback() {
			@Override
			public void invoke() {
				SingleDiscussionThreadWidget.this.threadSubscribersWidget.configure(threadTopic);
			}
		};
		
		subscribeButtonWidget.setOnUnsubscribeCallback(refreshSubscribersCallback);
		subscribeButtonWidget.setOnSubscribeCallback(refreshSubscribersCallback);
		loadMoreWidgetContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
		view.setRepliesContainer(loadMoreWidgetContainer);
		subscribeButtonWidget.showIconOnly();
		refreshCallback = new Callback() {
			@Override
			public void invoke() {
				reconfigureThread();
			}
		};
		threadTopic.setObjectType(SubscriptionObjectType.THREAD);
		editActionListener = action -> {
			onClickEditThread();
		};
		pinActionListener = action -> {
			onClickPinThread();
		};
		unpinActionListener = action -> {
			onClickUnpinThread();
		};
		
		deleteActionListener = action -> {
			onClickDeleteThread();
		};
		
		restoreActionListener = action -> {
			onClickRestore();
		};
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle, String replyId, Boolean isCurrentUserModerator, Set<String> moderatorIds, ActionMenuWidget actionMenu, Callback deleteOrRestoreCallback) {
		this.title = bundle.getTitle();
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.threadId = bundle.getId();
		this.messageKey = bundle.getMessageKey();
		this.deleteOrRestoreCallback = deleteOrRestoreCallback;
		this.projectId = bundle.getProjectId();
		this.moderatorIds = moderatorIds;
		this.isThreadDeleted = bundle.getIsDeleted();
		this.actionMenu = actionMenu;
		configureView(bundle);
		boolean isAuthorModerator = moderatorIds.contains(bundle.getCreatedBy());
		view.setIsAuthorModerator(isAuthorModerator);

		authorWidget.configure(bundle.getCreatedBy());
		configureMessage();
		if (!bundle.getId().equals(ginInjector.getSynapseProperties().getSynapseProperty(ForumWidget.DEFAULT_THREAD_ID_KEY))) {
			view.setSubscribersWidgetContainerVisible(true);
			if (replyId != null) {
				configureReply(replyId);
			} else {
				configureReplies();
			}
		} else {
			view.setSubscribersWidgetContainerVisible(false);
		}

		newReplyWidget.configure(threadId, getNewReplyCallback());
		secondNewReplyWidget.configure(threadId, getNewReplyCallback());
	}

	private Callback getNewReplyCallback() {
		return new Callback() {
			@Override
			public void invoke() {
				reconfigureThread();
			}
		};
	}

	/**
	 * After configuring this widget, call this method to pop up an alert when the thread etag changes upstream.
	 */
	public void watchReplyCount() {
		ReplyCountAlert refreshAlert = ginInjector.getReplyCountAlert();
		view.setRefreshAlert(refreshAlert.asWidget());
		refreshAlert.setRefreshCallback(refreshCallback);
		refreshAlert.configure(threadId);
	}

	private void configureView(DiscussionThreadBundle bundle) {
		view.clear();
		repliesContainer.clear();
		view.setTitle(title);
		view.setCreatedOn(CREATED_ON_PREFIX+dateTimeUtils.getRelativeTime(bundle.getCreatedOn()));
		view.setEditedLabelVisible(bundle.getIsEdited());
		boolean isDeleted = bundle.getIsDeleted();
		view.setDeletedThreadVisible(isDeleted);
		view.setReplyContainersVisible(!isDeleted);
		view.setCommandsVisible(!isDeleted);
		view.setRestoreIconVisible(isDeleted);
		threadTopic.setObjectId(bundle.getId());
		threadSubscribersWidget.configure(threadTopic);
		isPinned = bundle.getIsPinned();
		if (isPinned == null) {
			isPinned = false;
		}
		if (!isDeleted) {
			view.setDeleteIconVisible(isCurrentUserModerator);
			view.setUnpinIconVisible(isCurrentUserModerator && isPinned);
			view.setPinIconVisible(isCurrentUserModerator && !isPinned);
			view.setEditIconVisible(bundle.getCreatedBy().equals(authController.getCurrentUserPrincipalId()));
			if (actionMenu != null) {
				actionMenu.setActionVisible(Action.RESTORE_THREAD, false);
				actionMenu.setActionVisible(Action.DELETE_THREAD, isCurrentUserModerator);
			}
		} else {
			if (actionMenu != null) {
				actionMenu.setActionVisible(Action.RESTORE_THREAD, isCurrentUserModerator);
				actionMenu.setActionVisible(Action.DELETE_THREAD, false);
			}
		}
		configureActionMenu();
	}
	
	public void configureActionMenu() {
		if (actionMenu != null) {
			actionMenu.setActionListener(Action.EDIT_THREAD, editActionListener);
			actionMenu.setActionListener(Action.DELETE_THREAD, deleteActionListener);
			actionMenu.setActionListener(Action.RESTORE_THREAD, restoreActionListener);
			if (isPinned) {
				// thread is pinned
				actionMenu.setActionListener(Action.PIN_THREAD, unpinActionListener);
				actionMenu.setActionText(Action.PIN_THREAD, UNPIN_THREAD_ACTION_TEXT);
			} else {
				actionMenu.setActionListener(Action.PIN_THREAD, pinActionListener);
				actionMenu.setActionText(Action.PIN_THREAD, PIN_THREAD_ACTION_TEXT);
			}
		}
	}
	
	@Override
	public void onClickThread() {
		if (threadIdClickedCallback == null) {
			globalApplicationState.getPlaceChanger().goTo(TopicUtils.getThreadPlace(projectId, threadId));	
		} else {
			threadIdClickedCallback.invoke(threadId);
		}
	}
	
	public void setThreadIdClickedCallback(CallbackP<String> threadIdClickedCallback) {
		this.threadIdClickedCallback = threadIdClickedCallback;
	}
	
	public void setReplyIdCallback(CallbackP<String> replyIdCallback) {
		this.replyIdCallback = replyIdCallback;
	}

	public void reconfigureThread() {
		synAlert.clear();
		jsClient.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				configure(result, replyId, isCurrentUserModerator, moderatorIds, actionMenu, deleteOrRestoreCallback);
			}
		});
	}

	public void configureMessage() {
		synAlert.clear();
		markdownWidget.clear();
		subscribeButtonWidget.configure(SubscriptionObjectType.THREAD, threadId, actionMenu);
		//check cache for message
		if (clientCache.contains(messageKey + WebConstants.MESSAGE_SUFFIX)) {
			//cache hit
			setMessage(clientCache.get(messageKey + WebConstants.MESSAGE_SUFFIX));
		} else {
			//cache miss
			view.setLoadingMessageVisible(true);
			jsClient.getThreadUrl(messageKey, new AsyncCallback<String>(){

				@Override
				public void onFailure(Throwable caught) {
					view.setLoadingMessageVisible(false);
					synAlert.handleException(caught);
				}

				@Override
				public void onSuccess(String result) {
					getMessage(result);
				}
			});
		}
	}

	public void getMessage(String url) {
		requestBuilder.configure(RequestBuilder.GET, url);
		requestBuilder.setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {

				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						String message = response.getText();
						view.setLoadingMessageVisible(false);
						setMessage(message);
					} else {
						onError(null, new IllegalArgumentException("Unable to retrieve message for thread " + threadId + ". Reason: " + response.getStatusText()));
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					view.setLoadingMessageVisible(false);
					synAlert.handleException(exception);
				}
			});
		} catch (final Exception e) {
			view.setLoadingMessageVisible(false);
			synAlert.handleException(e);
		}
	}
	
	public void setMessage(String message) {
		this.message = message;
		markdownWidget.configure(message);
		clientCache.put(messageKey + WebConstants.MESSAGE_SUFFIX, message);
	}

	public void configureReplies() {
		repliesContainer.clear();
		view.setShowAllRepliesButtonVisible(false);
		offset = 0L;
		if (order == null) {
			order = DEFAULT_ORDER;
		}
		if (ascending == null) {
			ascending = DEFAULT_ASCENDING;
		}
		loadMore();
		watchReplyCount();
	}
	
	public void setSortDirection(Boolean ascending) {
		this.ascending = ascending;
		configureReplies();
	}
	
	@Override
	public void loadMore() {
		synAlert.clear();
		discussionForumClientAsync.getRepliesForThread(threadId, LIMIT, offset,
				order, ascending, DEFAULT_FILTER,
				new AsyncCallback<PaginatedResults<DiscussionReplyBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						repliesContainer.setIsMore(false);
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(
							PaginatedResults<DiscussionReplyBundle> result) {
						offset += LIMIT;
						if (!result.getResults().isEmpty()) {
							for (DiscussionReplyBundle bundle : result.getResults()) {
								ReplyWidget replyWidget = ginInjector.createReplyWidget();
								replyWidget.configure(bundle, isCurrentUserModerator, moderatorIds, refreshCallback, isThreadDeleted);
								repliesContainer.add(replyWidget.asWidget());
							}
						}
						repliesContainer.setIsMore(offset < result.getTotalNumberOfResults());
						view.setSecondNewReplyContainerVisible(result.getTotalNumberOfResults() > 0);
					}
		});
	}
	
	public void configureReply(String replyId) {
		synAlert.clear();
		repliesContainer.clear();
		repliesContainer.setIsMore(false);
		view.setShowAllRepliesButtonVisible(true);
		setReplyId(replyId);
		jsClient.getReply(replyId, new AsyncCallback<DiscussionReplyBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionReplyBundle bundle) {
				ReplyWidget replyWidget = ginInjector.createReplyWidget();
				replyWidget.configure(bundle, isCurrentUserModerator, moderatorIds, refreshCallback, isThreadDeleted);
				repliesContainer.add(replyWidget.asWidget());
			}
		});
	}

	@Override
	public void onClickShowAllReplies() {
		setReplyId(null);
		configureReplies();
	}
	
	private void setReplyId(String replyId) {
		this.replyId = replyId;
		if (replyIdCallback != null) {
			replyIdCallback.invoke(replyId);	
		}
	}
	
	@Override
	public void onClickDeleteThread() {
		popupUtils.showConfirmDelete(DELETE_CONFIRM_MESSAGE, () -> {
			deleteThread();
		});
	}

	public void deleteThread() {
		synAlert.clear();
		discussionForumClientAsync.markThreadAsDeleted(threadId, new AsyncCallback<Void>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(Void result) {
				view.showSuccess(DELETE_SUCCESS_TITLE, DELETE_SUCCESS_MESSAGE);
				if (deleteOrRestoreCallback != null) {
					deleteOrRestoreCallback.invoke();
				}
			}
		});
	}

	@Override
	public void onClickEditThread() {
		EditDiscussionThreadModal editThreadModal = ginInjector.getEditDiscussionThreadModal();
		view.setEditThreadModal(editThreadModal.asWidget());
		editThreadModal.configure(threadId, title, message, () -> {
			reconfigureThread();
		});
		editThreadModal.show();
	}

	public void onClickPinThread() {
		synAlert.clear();
		discussionForumClientAsync.pinThread(threadId, new AsyncCallback<Void>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(Void result) {
				view.setPinIconVisible(false);
				view.setUnpinIconVisible(true);
				isPinned = true;
				configureActionMenu();
			}
		});
	}

	public void onClickUnpinThread() {
		synAlert.clear();
		discussionForumClientAsync.unpinThread(threadId, new AsyncCallback<Void>(){
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(Void result) {
				view.setPinIconVisible(true);
				view.setUnpinIconVisible(false);
				isPinned = false;
				configureActionMenu();
			}
		});
	}
	
	public void setNewReplyContainerVisible(boolean visible) {
		view.setReplyContainersVisible(visible);;
	}
	
	public void setCommandsVisible(boolean visible) {
		view.setCommandsVisible(visible);
	}

	public void setReplyListVisible(boolean visible) {
		view.setReplyListContainerVisible(visible);
	}

	public void onClickRestore() {
		popupUtils.showConfirmDialog(CONFIRM_RESTORE_DIALOG_TITLE, RESTORE_CONFIRM_MESSAGE, new Callback() {
			@Override
			public void invoke() {
				restoreThread();
			}
		});
	}

	public void restoreThread() {
		synAlert.clear();
		discussionForumClientAsync.restoreThread(threadId, new AsyncCallback<Void>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(Void result) {
				view.showSuccess(RESTORE_SUCCESS_TITLE, RESTORE_SUCCESS_MESSAGE);
				if (deleteOrRestoreCallback != null) {
					deleteOrRestoreCallback.invoke();
				}
			}
		});
	}
}
