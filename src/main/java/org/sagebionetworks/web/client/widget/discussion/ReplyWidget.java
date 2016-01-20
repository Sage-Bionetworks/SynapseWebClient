package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.MessageURL;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidget implements ReplyWidgetView.Presenter{

	ReplyWidgetView view;
	GWTWrapper gwtWrapper;
	UserBadge authorWidget;
	RequestBuilderWrapper requestBuilder;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClientAsync;
	private String replyId;

	@Inject
	public ReplyWidget(
			ReplyWidgetView view,
			UserBadge authorWidget,
			GWTWrapper gwtWrapper,
			SynapseAlert synAlert,
			DiscussionForumClientAsync discussionForumClientAsync,
			RequestBuilderWrapper requestBuilder
			) {
		this.view = view;
		this.authorWidget = authorWidget;
		this.gwtWrapper = gwtWrapper;
		this.synAlert = synAlert;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.requestBuilder = requestBuilder;
		view.setPresenter(this);
		view.setAuthor(authorWidget.asWidget());
		view.setAlert(synAlert.asWidget());
	}

	public void configure(DiscussionReplyBundle bundle) {
		view.clear();
		this.replyId = bundle.getId();
		authorWidget.configure(bundle.getCreatedBy());
		view.setCreatedOn(gwtWrapper.getFormattedDateString(bundle.getCreatedOn()));
		configureMessage();
	}

	public void configureMessage() {
		synAlert.clear();
		discussionForumClientAsync.getReplyUrl(replyId, new AsyncCallback<MessageURL>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(MessageURL result) {
				getMessage(result);
			}
		});
	}

	private void getMessage(MessageURL result) {
		requestBuilder.configure(RequestBuilder.GET, result.getMessageUrl());
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
						onError(null, new IllegalArgumentException("Unable to retrieve message for reply " + replyId));
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
}
