package org.sagebionetworks.web.client.widget.discussion;

import static org.sagebionetworks.web.client.DisplayConstants.BUTTON_DELETE;
import static org.sagebionetworks.web.client.DisplayConstants.DANGER_BUTTON_STYLE;
import static org.sagebionetworks.web.client.DisplayConstants.BUTTON_RESTORE;
import static org.sagebionetworks.web.client.DisplayConstants.PRIMARY_BUTTON_STYLE;

import java.util.Set;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.sagebionetworks.repo.model.discussion.CreateDiscussionReply;
import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.validation.ValidationResult;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.discussion.modal.EditDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
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
	public static final Long LIMIT = 5L;
	private static final DiscussionFilter DEFAULT_FILTER = DiscussionFilter.EXCLUDE_DELETED;

	private static final String CONFIRM_DELETE_DIALOG_TITLE = "Confirm Deletion";
	private static final String CONFIRM_RESTORE_DIALOG_TITLE = "Confirm Restoration";
	private static final String DELETE_CONFIRM_MESSAGE = "Are you sure you want to delete this thread?";
	private static final String DELETE_SUCCESS_TITLE = "Thread deleted";
	private static final String DELETE_SUCCESS_MESSAGE = "A thread has been deleted.";
	private static final String RESTORE_SUCCESS_TITLE = "Thread restored";
	private static final String RESTORE_SUCCESS_MESSAGE = "A thread has been restored.";
	private static final String NEW_REPLY_SUCCESS_TITLE = "Reply created";
	private static final String NEW_REPLY_SUCCESS_MESSAGE = "A new reply has been created.";
	public static final String CREATED_ON_PREFIX = "posted ";
	public static final String DEFAULT_MARKDOWN = "";
	public static final String RESTORE_CONFIRM_MESSAGE = "Are you sure you want to restore this thread?";
	SingleDiscussionThreadWidgetView view;
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
	LoadMoreWidgetContainer repliesContainer;
	SubscribeButtonWidget subscribeButtonWidget;
	MarkdownEditorWidget markdownEditor;
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
	
	@Inject
	public SingleDiscussionThreadWidget(
			SingleDiscussionThreadWidgetView view,
			MarkdownEditorWidget markdownEditor,
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
			LoadMoreWidgetContainer loadMoreWidgetContainer,
			SubscribeButtonWidget subscribeButtonWidget
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.jsniUtils = jsniUtils;
		this.markdownEditor = markdownEditor;
		markdownEditor.hideUploadRelatedCommands();
		markdownEditor.showExternalImageButton();
		this.synAlert = synAlert;
		this.authorWidget = authorWidget;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.requestBuilder = requestBuilder;
		this.authController = authController;
		this.globalApplicationState = globalApplicationState;
		this.editThreadModal = editThreadModal;
		this.markdownWidget = markdownWidget;
		this.repliesContainer = loadMoreWidgetContainer;
		this.subscribeButtonWidget = subscribeButtonWidget;
		
		view.setPresenter(this);
		view.setMarkdownEditorWidget(markdownEditor.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setAuthor(authorWidget.asWidget());
		view.setEditThreadModal(editThreadModal.asWidget());
		view.setMarkdownWidget(markdownWidget.asWidget());
		view.setSubscribeButtonWidget(subscribeButtonWidget.asWidget());
		
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
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle, String replyId, Boolean isCurrentUserModerator, Set<String> moderatorIds, Callback deleteOrRestoreCallback) {
		this.title = bundle.getTitle();
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.threadId = bundle.getId();
		this.messageKey = bundle.getMessageKey();
		this.deleteOrRestoreCallback = deleteOrRestoreCallback;
		this.projectId = bundle.getProjectId();
		this.moderatorIds = moderatorIds;
		this.isThreadDeleted = bundle.getIsDeleted();
		configureView(bundle);
		boolean isAuthorModerator = moderatorIds.contains(bundle.getCreatedBy());
		view.setIsAuthorModerator(isAuthorModerator);

		authorWidget.configure(bundle.getCreatedBy());
		configureMessage();
		if (!bundle.getId().equals(globalApplicationState.getSynapseProperty(ForumWidget.DEFAULT_THREAD_ID_KEY))) {
			if (replyId != null) {
				configureReply(replyId);
			} else {
				configureReplies();
			}
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
		repliesContainer.clear();
		view.setTitle(title);
		view.setCreatedOn(CREATED_ON_PREFIX+jsniUtils.getRelativeTime(bundle.getCreatedOn()));
		view.setEditedLabelVisible(bundle.getIsEdited());
		boolean isDeleted = bundle.getIsDeleted();
		view.setDeletedThreadVisible(isDeleted);
		view.setReplyContainerVisible(!isDeleted);
		view.setCommandsVisible(!isDeleted);
		view.setRestoreIconVisible(isDeleted);
		if (!isDeleted) {
			view.setDeleteIconVisible(isCurrentUserModerator);

			Boolean isPinned = bundle.getIsPinned();
			if (isPinned == null) {
				isPinned = false;
			}
			view.setUnpinIconVisible(isCurrentUserModerator && isPinned);
			view.setPinIconVisible(isCurrentUserModerator && !isPinned);
			view.setEditIconVisible(bundle.getCreatedBy().equals(authController.getCurrentUserPrincipalId()));
			view.setThreadLink(TopicUtils.buildThreadLink(projectId, threadId));
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
		discussionForumClientAsync.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				configure(result, replyId, isCurrentUserModerator, moderatorIds, deleteOrRestoreCallback);
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

	@Override
	public void onClickNewReply() {
		if (!authController.isLoggedIn()) {
			view.showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
			globalApplicationState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		} else {
			view.setReplyTextBoxVisible(false);
			markdownEditor.configure(DEFAULT_MARKDOWN);
			view.setNewReplyContainerVisible(true);
			markdownEditor.setMarkdownFocus();
		}
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
					}
		});
	}
	
	public void configureReply(String replyId) {
		synAlert.clear();
		repliesContainer.clear();
		repliesContainer.setIsMore(false);
		view.setShowAllRepliesButtonVisible(true);
		setReplyId(replyId);
		discussionForumClientAsync.getReply(replyId, new AsyncCallback<DiscussionReplyBundle>() {
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
		view.showConfirm(DELETE_CONFIRM_MESSAGE, CONFIRM_DELETE_DIALOG_TITLE, BUTTON_DELETE, DANGER_BUTTON_STYLE, new AlertCallback(){

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
				if (deleteOrRestoreCallback != null) {
					deleteOrRestoreCallback.invoke();
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
	
	public void setReplyTextBoxVisible(boolean visible) {
		view.setReplyTextBoxVisible(visible);
	}
	
	public void setCommandsVisible(boolean visible) {
		view.setCommandsVisible(visible);
	}

	public void onClickCancel() {
		view.resetButton();
		view.setReplyTextBoxVisible(true);
		view.setNewReplyContainerVisible(false);
	}

	public void onClickSave() {
		synAlert.clear();
		String messageMarkdown = markdownEditor.getMarkdown();
		ValidationResult result = new ValidationResult();
		result.requiredField("Message", messageMarkdown);
		if (!result.isValid()) {
			synAlert.showError(result.getErrorMessage());
			return;
		}
		view.showSaving();
		CreateDiscussionReply toCreate = new CreateDiscussionReply();
		toCreate.setThreadId(threadId);
		toCreate.setMessageMarkdown(messageMarkdown);
		discussionForumClientAsync.createReply(toCreate, new AsyncCallback<DiscussionReplyBundle>(){
			@Override
			public void onFailure(Throwable caught) {
				view.resetButton();
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionReplyBundle result) {
				view.showSuccess(NEW_REPLY_SUCCESS_TITLE, NEW_REPLY_SUCCESS_MESSAGE);
				reconfigureThread();
				onClickCancel();
			}
		});
	}

	public void setReplyListVisible(boolean visible) {
		view.setReplyListContainerVisible(visible);
	}

	public void onClickRestore() {
		view.showConfirm(RESTORE_CONFIRM_MESSAGE, CONFIRM_RESTORE_DIALOG_TITLE, BUTTON_RESTORE, PRIMARY_BUTTON_STYLE, new AlertCallback(){

			@Override
			public void callback() {
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
