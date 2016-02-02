package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.discussion.modal.NewReplyModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.utils.Callback;
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
	private static final String DELETE_CONFIRM_MESSAGE = "Are you sure you want to delete this thread? Once a thread is deleted, you will not be able to undo this action.";
	DiscussionThreadWidgetView view;
	NewReplyModal newReplyModal;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClientAsync;
	PortalGinInjector ginInjector;
	SynapseJSNIUtils jsniUtils;
	UserBadge authorWidget;
	RequestBuilderWrapper requestBuilder;
	private Long offset;
	private DiscussionReplyOrder order;
	private Boolean ascending;
	private String threadId;
	private String messageKey;
	private Boolean isCurrentUserModerator;
	private Boolean isThreadDeleted;

	@Inject
	public DiscussionThreadWidget(
			DiscussionThreadWidgetView view,
			NewReplyModal newReplyModal,
			SynapseAlert synAlert,
			UserBadge authorWidget,
			DiscussionForumClientAsync discussionForumClientAsync,
			PortalGinInjector ginInjector,
			SynapseJSNIUtils jsniUtils,
			RequestBuilderWrapper requestBuilder
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.jsniUtils = jsniUtils;
		this.newReplyModal = newReplyModal;
		this.synAlert = synAlert;
		this.authorWidget = authorWidget;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.requestBuilder = requestBuilder;
		view.setPresenter(this);
		view.setNewReplyModal(newReplyModal.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setAuthor(authorWidget.asWidget());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle, Boolean isCurrentUserModerator) {
		view.clear();
		view.setTitle(bundle.getTitle());
		for (String userId : bundle.getActiveAuthors()){
			UserBadge user = ginInjector.getUserBadgeWidget();
			user.configure(userId);
			user.setSize(BadgeSize.SMALL_PICTURE_ONLY);
			view.addActiveAuthor(user.asWidget());
		}
		view.setNumberOfReplies(bundle.getNumberOfReplies().toString());
		view.setNumberOfViews(bundle.getNumberOfViews().toString());
		view.setLastActivity(jsniUtils.getRelativeTime(bundle.getLastActivity()));
		this.isThreadDeleted = bundle.getIsDeleted();
		if (isThreadDeleted) {
			view.setTitleAsDeleted();
			view.setThreadDownIconVisible(false);
			view.setThreadUpIconVisible(false);
			view.disableToggle();
		} else {
			authorWidget.configure(bundle.getCreatedBy());
			view.setCreatedOn(jsniUtils.getRelativeTime(bundle.getCreatedOn()));
			view.setShowRepliesVisibility(bundle.getNumberOfReplies() > 0);
			threadId = bundle.getId();
			messageKey = bundle.getMessageKey();
			newReplyModal.configure(bundle.getId(), new Callback(){
	
				@Override
				public void invoke() {
					reconfigure();
					configureReplies();
				}
			});
			this.isCurrentUserModerator = isCurrentUserModerator;
			view.setDeleteButtonVisible(isCurrentUserModerator);
		}
	}

	private void reconfigure() {
		synAlert.clear();
		discussionForumClientAsync.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>(){

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(DiscussionThreadBundle result) {
				configure(result, isCurrentUserModerator);
			}
		});
	}

	@Override
	public void toggleThread() {
		if (isThreadDeleted) {
			return;
		}
		if (view.isThreadCollapsed()) {
			// expand
			view.setThreadDownIconVisible(false);
			view.setThreadUpIconVisible(true);
			configureMessage();
		} else {
			// collapse
			view.setThreadDownIconVisible(true);
			view.setThreadUpIconVisible(false);
		}
		view.toggleThread();
	}

	public void configureMessage() {
		synAlert.clear();
		String url = DiscussionMessageURLUtil.buildMessageUrl(messageKey, WebConstants.THREAD_TYPE);
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
						onError(null, new IllegalArgumentException("Unable to retrieve message for thread " + threadId + ". Reason: " + response.getStatusText()));
					}
				}
			});
		} catch (final Exception e) {
			synAlert.handleException(e);
		}
	}

	@Override
	public void toggleReplies() {
		if (view.isReplyCollapsed()) {
			// expand
			view.setReplyDownIconVisible(false);
			view.setReplyUpIconVisible(true);
			configureReplies();
		} else {
			// collapse
			view.setReplyDownIconVisible(true);
			view.setReplyUpIconVisible(false);
		}
		view.toggleReplies();
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
		newReplyModal.show();
	}

	@Override
	public void loadMore() {
		synAlert.clear();
		view.setLoadingVisible(true);
		discussionForumClientAsync.getRepliesForThread(threadId, LIMIT, offset, order, ascending,
				new AsyncCallback<PaginatedResults<DiscussionReplyBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						view.setLoadingVisible(false);
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(
							PaginatedResults<DiscussionReplyBundle> result) {
						view.setShowRepliesVisibility(true);
						offset += LIMIT;
						for (DiscussionReplyBundle bundle : result.getResults()) {
							ReplyWidget replyWidget = ginInjector.createReplyWidget();
							replyWidget.configure(bundle, isCurrentUserModerator);
							view.addReply(replyWidget.asWidget());
						}
						view.setLoadingVisible(false);
						view.setNumberOfReplies(""+result.getTotalNumberOfResults());
						view.setLoadMoreButtonVisibility(offset < result.getTotalNumberOfResults());
						view.showReplyDetails();
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
				reset();
				reconfigure();
			}
		});
	}


	public void reset() {
		view.clear();
		if (!view.isThreadCollapsed()) {
			view.toggleThread();
		}
	}
}
