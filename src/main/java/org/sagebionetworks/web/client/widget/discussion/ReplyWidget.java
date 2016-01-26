package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidget implements ReplyWidgetView.Presenter{

	ReplyWidgetView view;
	SynapseJSNIUtils jsniUtils;
	UserBadge authorWidget;
	RequestBuilderWrapper requestBuilder;
	SynapseAlert synAlert;
	private String replyId;
	private String messageKey;

	@Inject
	public ReplyWidget(
			ReplyWidgetView view,
			UserBadge authorWidget,
			SynapseJSNIUtils jsniUtils,
			SynapseAlert synAlert,
			RequestBuilderWrapper requestBuilder
			) {
		this.view = view;
		this.authorWidget = authorWidget;
		this.jsniUtils = jsniUtils;
		this.synAlert = synAlert;
		this.requestBuilder = requestBuilder;
		view.setPresenter(this);
		view.setAuthor(authorWidget.asWidget());
		view.setAlert(synAlert.asWidget());
	}

	public void configure(DiscussionReplyBundle bundle) {
		view.clear();
		this.replyId = bundle.getId();
		this.messageKey = bundle.getMessageKey();
		authorWidget.configure(bundle.getCreatedBy());
		view.setCreatedOn(jsniUtils.getRelativeTime(bundle.getCreatedOn()));
		configureMessage();
	}

	public void configureMessage() {
		synAlert.clear();
		String url = DiscussionMessageURLUtil.buildMessageUrl(messageKey, WebConstants.REPLY_TYPE);
		requestBuilder.configure(RequestBuilder.GET, url);
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
}
