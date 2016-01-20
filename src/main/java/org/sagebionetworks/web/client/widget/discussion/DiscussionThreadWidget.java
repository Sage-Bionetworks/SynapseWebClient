package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.discussion.modal.NewReplyModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadWidget implements DiscussionThreadWidgetView.Presenter{

	private static final DiscussionReplyOrder DEFAULT_ORDER = DiscussionReplyOrder.CREATED_ON;
	private static final Boolean DEFAULT_ASCENDING = false;
	public static final Long LIMIT = 20L;
	DiscussionThreadWidgetView view;
	NewReplyModal newReplyModal;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClientAsync;
	PortalGinInjector ginInjector;
	GWTWrapper gwtWrapper;
	UserBadge authorWidget;
	private Long offset;
	private DiscussionReplyOrder order;
	private Boolean ascending;
	private String threadId;

	@Inject
	public DiscussionThreadWidget(
			DiscussionThreadWidgetView view,
			NewReplyModal newReplyModal,
			SynapseAlert synAlert,
			UserBadge authorWidget,
			DiscussionForumClientAsync discussionForumClientAsync,
			PortalGinInjector ginInjector,
			GWTWrapper gwtWrapper
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.gwtWrapper = gwtWrapper;
		this.newReplyModal = newReplyModal;
		this.synAlert = synAlert;
		this.authorWidget = authorWidget;
		this.discussionForumClientAsync = discussionForumClientAsync;
		view.setPresenter(this);
		view.setNewReplyModal(newReplyModal.asWidget());
		view.setAlert(synAlert.asWidget());
		view.setAuthor(authorWidget.asWidget());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle) {
		view.clear();
		view.setTitle(bundle.getTitle());
		for (String userId : bundle.getActiveAuthors()){
			UserBadge user = ginInjector.getUserBadgeWidget();
			user.configure(userId);
			view.addActiveAuthor(user.asWidget());
		}
		view.setNumberOfReplies(bundle.getNumberOfReplies().toString());
		view.setNumberOfViews(bundle.getNumberOfViews().toString());
		view.setLastActivity(gwtWrapper.getFormattedDateString(bundle.getLastActivity()));
		authorWidget.configure(bundle.getCreatedBy());
		view.setCreatedOn(gwtWrapper.getFormattedDateString(bundle.getCreatedOn()));
		view.setShowRepliesVisibility(bundle.getNumberOfReplies() > 0);
		threadId = bundle.getId();
		newReplyModal.configure(bundle.getId(), new Callback(){

			@Override
			public void invoke() {
				reconfigure();
			}
		});
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
				configure(result);
			}
		});
		configureReplies();
	}

	@Override
	public void toggleThread() {
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

	private void configureMessage() {
		view.setMessage("thread message");
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
		synAlert.clear();
		view.clearReplies();
		offset = 0L;
		if (order == null) {
			order = DEFAULT_ORDER;
		}
		if (ascending == null) {
			ascending = DEFAULT_ASCENDING;
		}
		discussionForumClientAsync.getRepliesForThread(threadId, LIMIT, offset, order, ascending,
				new AsyncCallback<PaginatedResults<DiscussionReplyBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(
							PaginatedResults<DiscussionReplyBundle> result) {
						view.setShowRepliesVisibility(true);
						offset += LIMIT;
						for (DiscussionReplyBundle bundle : result.getResults()) {
							ReplyWidget replyWidget = ginInjector.createReplyWidget();
							replyWidget.configure(bundle);
							view.addReply(replyWidget.asWidget());
						}
						view.setNumberOfReplies(""+result.getTotalNumberOfResults());
						view.setLoadMoreButtonVisibility(offset < result.getTotalNumberOfResults());
						view.showReplyDetails();
					}
		});
	}

	@Override
	public void onClickNewReply() {
		newReplyModal.show();
	}
}
