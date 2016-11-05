package org.sagebionetworks.web.client.widget.discussion;

import java.util.Set;

import org.gwtbootstrap3.extras.bootbox.client.callback.SimpleCallback;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.discussion.modal.EditReplyModal;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidget implements ReplyWidgetView.Presenter{

	public static final String REPLY_URL = "Reply URL:";
	private static final String DELETE_CONFIRM_MESSAGE = "Are you sure you want to delete this reply?";
	private static final String DELETE_SUCCESS_TITLE = "Reply deleted";
	private static final String DELETE_SUCCESS_MESSAGE = "A reply has been deleted.";
	ReplyWidgetView view;
	SynapseJSNIUtils jsniUtils;
	UserBadge authorWidget;
	RequestBuilderWrapper requestBuilder;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClientAsync;
	AuthenticationController authController;
	EditReplyModal editReplyModal;
	MarkdownWidget markdownWidget;
	GWTWrapper gwt;
	CopyTextModal copyTextModal;
	private String replyId, projectId, threadId;
	private String messageKey;
	private Boolean isCurrentUserModerator;
	private Callback deleteReplyCallback;
	private Set<String> moderatorIds;
	private String message;
	private boolean isThreadDeleted;
	
	@Inject
	public ReplyWidget(
			ReplyWidgetView view,
			UserBadge authorWidget,
			SynapseJSNIUtils jsniUtils,
			SynapseAlert synAlert,
			RequestBuilderWrapper requestBuilder,
			DiscussionForumClientAsync discussionForumClientAsync,
			AuthenticationController authController,
			EditReplyModal editReplyModal,
			MarkdownWidget markdownWidget,
			GWTWrapper gwt,
			CopyTextModal copyTextModal
			) {
		this.view = view;
		this.authorWidget = authorWidget;
		this.jsniUtils = jsniUtils;
		this.synAlert = synAlert;
		this.requestBuilder = requestBuilder;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.authController = authController;
		this.editReplyModal = editReplyModal;
		this.markdownWidget = markdownWidget;
		this.gwt = gwt;
		this.copyTextModal = copyTextModal;
		view.setPresenter(this);
		view.setAuthor(authorWidget.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setEditReplyModal(editReplyModal.asWidget());
		view.setMarkdownWidget(markdownWidget.asWidget());
		view.setCopyTextModal(copyTextModal.asWidget());
		
		copyTextModal.setTitle(REPLY_URL);
	}

	public void configure(DiscussionReplyBundle bundle, Boolean isCurrentUserModerator, Set<String> moderatorIds, Callback deleteReplyCallback, boolean isThreadDeleted) {
		view.clear();
		markdownWidget.clear();
		this.replyId = bundle.getId();
		this.projectId = bundle.getProjectId();
		this.threadId = bundle.getThreadId();
		this.messageKey = bundle.getMessageKey();
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.moderatorIds = moderatorIds;
		this.deleteReplyCallback = deleteReplyCallback;
		this.isThreadDeleted = isThreadDeleted;
		authorWidget.configure(bundle.getCreatedBy());
		view.setCreatedOn(SingleDiscussionThreadWidget.CREATED_ON_PREFIX+jsniUtils.getRelativeTime(bundle.getCreatedOn()));
		view.setMessageVisible(true);
		view.setEditedVisible(bundle.getIsEdited());
		boolean isAuthorModerator = moderatorIds.contains(bundle.getCreatedBy());
		view.setIsAuthorModerator(isAuthorModerator);
		view.setCommandsContainerVisible(!isThreadDeleted);
		if (!isThreadDeleted) {
			view.setDeleteIconVisibility(isCurrentUserModerator);
			view.setEditIconVisible(bundle.getCreatedBy().equals(authController.getCurrentUserPrincipalId()));
		}

		configureMessage();
	}

	public void configureMessage() {
		synAlert.clear();
		view.setLoadingMessageVisible(true);
		discussionForumClientAsync.getReplyUrl(messageKey, new AsyncCallback<String>(){

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
						message = response.getText();
						view.setLoadingMessageVisible(false);
						markdownWidget.configure(message);
					} else {
						onError(null, new IllegalArgumentException("Unable to retrieve message for reply " + replyId + ". Reason: " + response.getStatusText()));
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

	private void configureEditReplyModal(String message) {
		editReplyModal.configure(replyId, message, new Callback(){

			@Override
			public void invoke() {
				reconfigure();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onClickDeleteReply() {
		view.showDeleteConfirm(DELETE_CONFIRM_MESSAGE, new SimpleCallback(){

			@Override
			public void callback() {
				deleteReply();
			}
		});
	}

	public void deleteReply() {
		synAlert.clear();
		discussionForumClientAsync.markReplyAsDeleted(replyId, new AsyncCallback<Void>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(Void result) {
				view.showSuccess(DELETE_SUCCESS_TITLE, DELETE_SUCCESS_MESSAGE);
				if (deleteReplyCallback != null) {
					deleteReplyCallback.invoke();
				}
			}
		});
	}

	public void reconfigure() {
		synAlert.clear();
		discussionForumClientAsync.getReply(replyId, new AsyncCallback<DiscussionReplyBundle>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionReplyBundle result) {
				configure(result, isCurrentUserModerator, moderatorIds, deleteReplyCallback, isThreadDeleted);
			}
		});
	}

	@Override
	public void onClickEditReply() {
		configureEditReplyModal(message);
		editReplyModal.show();
	}
	
	@Override
	public void onClickReplyLink() {
		String url = gwt.getHostPageBaseURL() + TopicUtils.buildReplyLink(projectId, threadId, replyId).substring(1);
		copyTextModal.setText(url);
		copyTextModal.show();
	}
}
