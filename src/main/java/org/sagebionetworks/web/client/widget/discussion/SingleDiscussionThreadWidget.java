package org.sagebionetworks.web.client.widget.discussion;

import java.util.Set;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.discussion.modal.EditDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.discussion.modal.NewReplyModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
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

	private static final DiscussionReplyOrder DEFAULT_ORDER = DiscussionReplyOrder.CREATED_ON;
	private static final Boolean DEFAULT_ASCENDING = true;
	public static final Long LIMIT = 20L;
	private static final String DELETE_CONFIRM_MESSAGE = "Are you sure you want to delete this thread?";
	private static final DiscussionFilter DEFAULT_FILTER = DiscussionFilter.EXCLUDE_DELETED;
	private static final String DELETE_SUCCESS_TITLE = "Thread deleted";
	private static final String DELETE_SUCCESS_MESSAGE = "A thread has been deleted.";
	public static final String REPLY = "reply";
	public static final String REPLIES = "replies";
	public static final String CREATED_ON_PREFIX = "posted ";
	public static final String NO_INDENTATION_WIDTH = "100%";
	public static final String INDENTATION_WIDTH = "98%";
	SingleDiscussionThreadWidgetView view;
	NewReplyModal newReplyModal;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClientAsync;
	PortalGinInjector ginInjector;
	SynapseJSNIUtils jsniUtils;
	UserBadge authorWidget;
	RequestBuilderWrapper requestBuilder;
	AuthenticationController authController;
	GlobalApplicationState globalApplicationState;
	EditDiscussionThreadModal editThreadModal;
	MarkdownWidget markdownWidget;
	GWTWrapper gwtWrapper;
	SubscribeButtonWidget subscribeButtonWidget;
	private CallbackP<String> threadIdClickedCallback; 
	
	private Long offset;
	private DiscussionReplyOrder order;
	private Boolean ascending;
	private String threadId;
	private String messageKey;
	private Boolean isCurrentUserModerator;
	private String title;
	private Callback deleteCallback;
	private String projectId;
	private Callback refreshCallback;
	private Set<Long> moderatorIds;
	private Callback invokeCheckForInViewAndLoadData;
	
	@Inject
	public SingleDiscussionThreadWidget(
			SingleDiscussionThreadWidgetView view,
			NewReplyModal newReplyModal,
			SynapseAlert synAlert,
			UserBadge authorWidget,
			DiscussionForumClientAsync discussionForumClientAsync,
			PortalGinInjector ginInjector,
			SynapseJSNIUtils jsniUtils,
			RequestBuilderWrapper requestBuilder,
			AuthenticationController authController,
			GlobalApplicationState globalApplicationState,
			EditDiscussionThreadModal editThreadModal,
			MarkdownWidget markdownWidget,
			GWTWrapper gwtWrapper,
			SubscribeButtonWidget subscribeButtonWidget
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.jsniUtils = jsniUtils;
		this.newReplyModal = newReplyModal;
		this.synAlert = synAlert;
		this.authorWidget = authorWidget;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.requestBuilder = requestBuilder;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		this.editThreadModal = editThreadModal;
		this.markdownWidget = markdownWidget;
		this.gwtWrapper = gwtWrapper;
		this.subscribeButtonWidget = subscribeButtonWidget;
		
		view.setPresenter(this);
		view.setNewReplyModal(newReplyModal.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setAuthor(authorWidget.asWidget());
		view.setEditThreadModal(editThreadModal.asWidget());
		view.setMarkdownWidget(markdownWidget.asWidget());
		view.setSubscribeButtonWidget(subscribeButtonWidget.asWidget());
		
		subscribeButtonWidget.showIconOnly();
		refreshCallback = new Callback() {
			@Override
			public void invoke() {
				reconfigureThread();
			}
		};
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle, Boolean isCurrentUserModerator, Set<Long> moderatorIds, Callback deleteCallback) {
		this.title = bundle.getTitle();
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.threadId = bundle.getId();
		this.messageKey = bundle.getMessageKey();
		this.deleteCallback = deleteCallback;
		this.projectId = bundle.getProjectId();
		this.moderatorIds = moderatorIds;
		configureView(bundle);
		boolean isAuthorModerator = moderatorIds.contains(Long.parseLong(bundle.getCreatedBy()));
		view.setIsAuthorModerator(isAuthorModerator);
		
		authorWidget.configure(bundle.getCreatedBy());
		newReplyModal.configure(bundle.getId(), new Callback(){

			@Override
			public void invoke() {
				reconfigureThread();
			}
		});
		invokeCheckForInViewAndLoadData = new Callback() {
			@Override
			public void invoke() {
				checkForInViewAndLoadData();
			}
		};

		configureMessage();
		if (!bundle.getId().equals(globalApplicationState.getSynapseProperty(ForumWidget.DEFAULT_THREAD_ID_KEY))) {
			configureReplies();
			watchReplyCount();
		}

	}

	public void checkForInViewAndLoadData() {
		if (!view.isLoadMoreAttached()) {
			//Done, view has been detached and widget was never in the viewport
			return;
		} else if (view.isLoadMoreInViewport() && view.getLoadMoreVisibility()) {
			//try to load data!
			loadMore();
		} else {
			//wait for a few seconds and see if we should load data
			gwtWrapper.scheduleExecution(invokeCheckForInViewAndLoadData, DisplayConstants.DELAY_UNTIL_IN_VIEW);
		}
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
		view.setTitle(title);
		Long numberOfReplies = bundle.getNumberOfReplies();
		view.setCreatedOn(CREATED_ON_PREFIX+jsniUtils.getRelativeTime(bundle.getCreatedOn()));
		view.setEditedVisible(bundle.getIsEdited());
		view.setDeleteIconVisible(isCurrentUserModerator);

		Boolean isPinned = bundle.getIsPinned();
		if (isPinned == null) {
			isPinned = false;
		}
		view.setUnpinIconVisible(isCurrentUserModerator && isPinned);
		view.setPinIconVisible(isCurrentUserModerator && !isPinned);
		view.setEditIconVisible(bundle.getCreatedBy().equals(authController.getCurrentUserPrincipalId()));
		view.setThreadLink(TopicUtils.buildThreadLink(projectId, threadId));

		if (numberOfReplies == 0) {
			view.setButtonContainerWidth(NO_INDENTATION_WIDTH);
		} else {
			view.setButtonContainerWidth(INDENTATION_WIDTH);
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

	public void reconfigureThread() {
		synAlert.clear();
		discussionForumClientAsync.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				configure(result, isCurrentUserModerator, moderatorIds, deleteCallback);
			}
		});
	}

	public void configureMessage() {
		synAlert.clear();
		markdownWidget.clear();
		view.setLoadingMessageVisible(true);
		subscribeButtonWidget.configure(SubscriptionObjectType.THREAD, threadId);
		discussionForumClientAsync.getThreadUrl(messageKey, new AsyncCallback<String>(){

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
						markdownWidget.configure(message);
						configureEditThreadModal(message);
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

	private void configureEditThreadModal(String message) {
		editThreadModal.configure(threadId, title, message, new Callback(){

			@Override
			public void invoke() {
				reconfigureThread();
			}
		});
	}

	public void configureReplies() {
		view.clearReplies();
		offset = 0L;
		if (order == null) {
			order = DEFAULT_ORDER;
		}
		if (ascending == null) {
			ascending = DEFAULT_ASCENDING;
		}
		loadMore();
	}

	@Override
	public void onClickNewReply() {
		if (!authController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else {
			newReplyModal.show();
		}
	}

	@Override
	public void loadMore() {
		synAlert.clear();
		view.setLoadMoreVisibility(true);
		discussionForumClientAsync.getRepliesForThread(threadId, LIMIT, offset,
				order, ascending, DEFAULT_FILTER,
				new AsyncCallback<PaginatedResults<DiscussionReplyBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						view.setLoadMoreVisibility(false);
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(
							PaginatedResults<DiscussionReplyBundle> result) {
						offset += LIMIT;
						if (!result.getResults().isEmpty()) {
							for (DiscussionReplyBundle bundle : result.getResults()) {
								ReplyWidget replyWidget = ginInjector.createReplyWidget();
								replyWidget.configure(bundle, isCurrentUserModerator, moderatorIds, new Callback(){
									@Override
									public void invoke() {
										reconfigureThread();
									}
								});
								view.addReply(replyWidget.asWidget());
							}
						}
						view.setLoadMoreVisibility(offset < result.getTotalNumberOfResults());
						if (offset < result.getTotalNumberOfResults()) {
							gwtWrapper.scheduleExecution(invokeCheckForInViewAndLoadData, DisplayConstants.DELAY_UNTIL_IN_VIEW);
						}
					}
		});
	}

	@Override
	public void onClickDeleteThread() {
		view.showDeleteConfirm(DELETE_CONFIRM_MESSAGE, new AlertCallback(){

			@Override
			public void callback() {
				deleteThread();
			}
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
				if (deleteCallback != null) {
					deleteCallback.invoke();
				}
			}
		});
	}

	@Override
	public void onClickEditThread() {
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
			}
		});
	}
	
	public void setReplyButtonVisible(boolean visible) {
		view.setReplyButtonVisible(visible);
	}
	
	public void setCommandsVisible(boolean visible) {
		view.setCommandsVisible(visible);
	}
}
