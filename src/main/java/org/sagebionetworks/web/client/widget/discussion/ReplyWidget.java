package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
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
	private String replyId;
	private String messageKey;
	private Boolean isCurrentUserModerator;
	private Callback deleteReplyCallback;

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
			MarkdownWidget markdownWidget
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
		view.setPresenter(this);
		view.setAuthor(authorWidget.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setEditReplyModal(editReplyModal.asWidget());
		view.setMarkdownWidget(markdownWidget.asWidget());
	}

	public void configure(DiscussionReplyBundle bundle, Boolean isCurrentUserModerator, Callback deleteReplyCallback) {
		view.clear();
		markdownWidget.clear();
		this.replyId = bundle.getId();
		this.messageKey = bundle.getMessageKey();
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.deleteReplyCallback = deleteReplyCallback;
		authorWidget.configure(bundle.getCreatedBy());
		view.setCreatedOn(jsniUtils.getRelativeTime(bundle.getCreatedOn()));
		if (bundle.getIsDeleted()) {
			view.setDeleteIconVisibility(false);
			view.setMessageVisible(false);
			view.setDeletedMessageVisible(true);
			view.setEditIconVisible(false);
		} else {
			view.setMessageVisible(true);
			view.setDeletedMessageVisible(false);
			view.setEditedVisible(bundle.getIsEdited());
			view.setDeleteIconVisibility(isCurrentUserModerator);
			view.setEditIconVisible(bundle.getCreatedBy().equals(authController.getCurrentUserPrincipalId()));
			configureMessage();
		}
	}

	public void configureMessage() {
		synAlert.clear();
		view.setLoadingMessageVisible(true);
		String url = DiscussionMessageURLUtil.buildMessageUrl(messageKey, WebConstants.REPLY_TYPE);
		requestBuilder.configure(RequestBuilder.GET, url);
		requestBuilder.setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(final Request request, final Throwable e) {
					view.setLoadingMessageVisible(false);
					synAlert.handleException(e);
				}
				public void onResponseReceived(final Request request, final Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						String message = response.getText();
						view.setLoadingMessageVisible(false);
						markdownWidget.configure(message);
						configureEditReplyModal(message);
					} else {
						onError(null, new IllegalArgumentException("Unable to retrieve message for reply " + replyId + ". Reason: " + response.getStatusText()));
					}
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
		view.showDeleteConfirm(DELETE_CONFIRM_MESSAGE, new AlertCallback(){

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
				configure(result, isCurrentUserModerator, deleteReplyCallback);
			}
		});
	}

	@Override
	public void onClickEditReply() {
		editReplyModal.show();
	}
}
