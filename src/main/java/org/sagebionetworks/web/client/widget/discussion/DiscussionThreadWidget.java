package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.sagebionetworks.repo.model.ObjectType;
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
import org.sagebionetworks.web.client.widget.refresh.RefreshAlert;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidget;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
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

public class DiscussionThreadWidget implements DiscussionThreadWidgetView.Presenter{

	private static final DiscussionReplyOrder DEFAULT_ORDER = DiscussionReplyOrder.CREATED_ON;
	private static final Boolean DEFAULT_ASCENDING = true;
	public static final Long LIMIT = 20L;
	private static final String DELETE_CONFIRM_MESSAGE = "Are you sure you want to delete this thread?";
	private static final DiscussionFilter DEFAULT_FILTER = DiscussionFilter.EXCLUDE_DELETED;
	private static final String DELETE_SUCCESS_TITLE = "Thread deleted";
	private static final String DELETE_SUCCESS_MESSAGE = "A thread has been deleted.";
	public static final String REPLY = "reply";
	public static final String REPLIES = "replies";
	DiscussionThreadWidgetView view;
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
	UserBadge authorIconWidget;
	GWTWrapper gwtWrapper;
	SubscribeButtonWidget subscribeButtonWidget;
	RefreshAlert refreshAlert;
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

	@Inject
	public DiscussionThreadWidget(
			DiscussionThreadWidgetView view,
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
			UserBadge authorIconWidget,
			GWTWrapper gwtWrapper,
			SubscribeButtonWidget subscribeButtonWidget,
			RefreshAlert refreshAlert
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
		this.authorIconWidget = authorIconWidget;
		this.gwtWrapper = gwtWrapper;
		this.subscribeButtonWidget = subscribeButtonWidget;
		this.refreshAlert = refreshAlert;
		
		view.setPresenter(this);
		view.setNewReplyModal(newReplyModal.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setAuthor(authorWidget.asWidget());
		view.setEditThreadModal(editThreadModal.asWidget());
		view.setMarkdownWidget(markdownWidget.asWidget());
		view.setThreadAuthor(authorIconWidget.asWidget());
		view.setSubscribeButtonWidget(subscribeButtonWidget.asWidget());
		view.setRefreshAlert(refreshAlert.asWidget());
		subscribeButtonWidget.showIconOnly();
		refreshAlert.setRefreshCallback(new Callback() {
			@Override
			public void invoke() {
				reconfigureThread();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle, Boolean isCurrentUserModerator, Callback deleteCallback, boolean showThreadDetails, boolean showReplyDetails) {
		this.title = bundle.getTitle();
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.threadId = bundle.getId();
		this.messageKey = bundle.getMessageKey();
		this.deleteCallback = deleteCallback;
		this.projectId = bundle.getProjectId();
		refreshAlert.clear();
		configureView(bundle, showThreadDetails, showReplyDetails);
		authorWidget.configure(bundle.getCreatedBy());
		newReplyModal.configure(bundle.getId(), new Callback(){

			@Override
			public void invoke() {
				reconfigureThread();
			}
		});
	}
	
	/**
	 * After configuring this widget, call this method to pop up an alert when the thread etag changes upstream.
	 */
	public void watchEtag() {
		refreshAlert.configure(threadId, ObjectType.THREAD);
	}

	public void unwatchEtag() {
		refreshAlert.clear();
	}


	private void configureView(DiscussionThreadBundle bundle, boolean showThreadDetails, boolean showReplyDetails) {
		view.clear();
		view.setTitle(title);
		authorIconWidget.configure(bundle.getCreatedBy());
		authorIconWidget.setSize(BadgeSize.SMALL_PICTURE_ONLY);
		for (String userId : bundle.getActiveAuthors()){
			UserBadge user = ginInjector.getUserBadgeWidget();
			user.configure(userId);
			user.setSize(BadgeSize.SMALL_PICTURE_ONLY);
			view.addActiveAuthor(user.asWidget());
		}
		Long numberOfReplies = bundle.getNumberOfReplies();
		view.setNumberOfReplies(numberOfReplies.toString(), getDescriptiveReplyText(numberOfReplies));
		view.setNumberOfViews(bundle.getNumberOfViews().toString());
		view.setLastActivity(jsniUtils.getRelativeTime(bundle.getLastActivity()));
		view.setCreatedOn(jsniUtils.getRelativeTime(bundle.getCreatedOn()));
		view.setEditedVisible(bundle.getIsEdited());
		view.setDeleteIconVisible(isCurrentUserModerator);
		view.setEditIconVisible(bundle.getCreatedBy().equals(authController.getCurrentUserPrincipalId()));
		view.setThreadLink(TopicUtils.buildThreadLink(projectId, threadId));
		if (showThreadDetails) {
			showThreadDetails();
		} else {
			hideThreadDetails();
		}
		if (showReplyDetails) {
			showReplyDetails();
		} else {
			hideReplyDetails();
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

	private String getDescriptiveReplyText(Long numberOfReplies) {
		if (numberOfReplies == 1) {
			return REPLY;
		} else {
			return REPLIES;
		}
	}

	private void hideThreadDetails() {
		view.hideThreadDetails();
		unwatchEtag();
	}

	private void showThreadDetails() {
		configureMessage();
		view.showThreadDetails();
		watchEtag();
	}

	private void reconfigureThread() {
		synAlert.clear();
		discussionForumClientAsync.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				boolean showThreadDetails = true;
				boolean showReplyDetails = !view.isReplyCollapsed();
				configure(result, isCurrentUserModerator, deleteCallback, showThreadDetails, showReplyDetails);
			}
		});
	}

	public boolean isThreadCollapsed() {
		return view.isThreadCollapsed();
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

	private void hideReplyDetails() {
		view.setLoadMoreButtonVisibility(false);
		view.hideReplyDetails();
	}

	private void showReplyDetails() {
		configureReplies();
		view.showReplyDetails();
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
		view.setLoadingRepliesVisible(true);
		discussionForumClientAsync.getRepliesForThread(threadId, LIMIT, offset,
				order, ascending, DEFAULT_FILTER,
				new AsyncCallback<PaginatedResults<DiscussionReplyBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						view.setLoadingRepliesVisible(false);
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(
							PaginatedResults<DiscussionReplyBundle> result) {
						offset += LIMIT;
						if (result.getResults().isEmpty()) {
							view.hideReplyDetails();
						} else {
							for (DiscussionReplyBundle bundle : result.getResults()) {
								ReplyWidget replyWidget = ginInjector.createReplyWidget();
								replyWidget.configure(bundle, isCurrentUserModerator, new Callback(){
									@Override
									public void invoke() {
										reconfigureThread();
									}
								});
								view.addReply(replyWidget.asWidget());
							}
							view.setLoadingRepliesVisible(false);
							Long totalReplies = result.getTotalNumberOfResults();
							view.setNumberOfReplies(""+totalReplies, getDescriptiveReplyText(totalReplies));
							view.setLoadMoreButtonVisibility(offset < result.getTotalNumberOfResults());
							view.showReplyDetails();
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
}
