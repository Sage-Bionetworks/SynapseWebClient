package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
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
	public static final String DELETED_REPLY_DEFAULT_MESSAGE = "This reply has been deleted.";
	ReplyWidgetView view;
	SynapseJSNIUtils jsniUtils;
	UserBadge authorWidget;
	RequestBuilderWrapper requestBuilder;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClientAsync;
	AuthenticationController authController;
	private String replyId;
	private String messageKey;
	private Boolean isCurrentUserModerator;

	@Inject
	public ReplyWidget(
			ReplyWidgetView view,
			UserBadge authorWidget,
			SynapseJSNIUtils jsniUtils,
			SynapseAlert synAlert,
			RequestBuilderWrapper requestBuilder,
			DiscussionForumClientAsync discussionForumClientAsync,
			AuthenticationController authController
			) {
		this.view = view;
		this.authorWidget = authorWidget;
		this.jsniUtils = jsniUtils;
		this.synAlert = synAlert;
		this.requestBuilder = requestBuilder;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.authController = authController;
		view.setPresenter(this);
		view.setAuthor(authorWidget.asWidget());
		view.setAlert(synAlert.asWidget());
	}

	public void configure(DiscussionReplyBundle bundle, Boolean isCurrentUserModerator) {
		view.clear();
		this.replyId = bundle.getId();
		this.messageKey = bundle.getMessageKey();
		authorWidget.configure(bundle.getCreatedBy());
		view.setCreatedOn(jsniUtils.getRelativeTime(bundle.getCreatedOn()));
		this.isCurrentUserModerator = isCurrentUserModerator;
		if (bundle.getIsDeleted()) {
			view.setDeleteButtonVisibility(false);
			view.setMessage(DELETED_REPLY_DEFAULT_MESSAGE);
			view.setEditIconVisible(false);
		} else {
			view.setDeleteButtonVisibility(isCurrentUserModerator);
			view.setEditIconVisible(bundle.getCreatedBy().equals(authController.getCurrentUserPrincipalId()));
			configureMessage();
		}
	}

	public void configureMessage() {
		synAlert.clear();
		String url = DiscussionMessageURLUtil.buildMessageUrl(messageKey, WebConstants.REPLY_TYPE);
		requestBuilder.configure(RequestBuilder.GET, url);
		requestBuilder.setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(final Request request, final Throwable e) {
					synAlert.handleException(e);
				}
				public void onResponseReceived(final Request request, final Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						view.setMessage(response.getText());
					} else {
						onError(null, new IllegalArgumentException("Unable to retrieve message for reply " + replyId + ". Reason: " + response.getStatusText()));
					}
				}
			});
		} catch (final Exception e) {
			synAlert.handleException(e);
		}
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
				reconfigure();
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
				configure(result, isCurrentUserModerator);
			}
		});
	}

	@Override
	public void onClickEditReply() {
		// TODO Auto-generated method stub
		
	}
}
