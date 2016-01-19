package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionReplyBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionReplyOrder;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.discussion.modal.NewReplyModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
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
	private Long offset;
	private DiscussionReplyOrder order;
	private Boolean ascending;
	private String threadId;

	@Inject
	public DiscussionThreadWidget(
			DiscussionThreadWidgetView view,
			NewReplyModal newReplyModal,
			SynapseAlert synAlert,
			DiscussionForumClientAsync discussionForumClientAsync,
			PortalGinInjector ginInjector,
			GWTWrapper gwtWrapper
			) {
		this.ginInjector = ginInjector;
		this.view = view;
		this.gwtWrapper = gwtWrapper;
		this.newReplyModal = newReplyModal;
		this.synAlert = synAlert;
		this.discussionForumClientAsync = discussionForumClientAsync;
		view.setPresenter(this);
		view.setNewReplyModal(newReplyModal.asWidget());
		view.setAlert(synAlert.asWidget());
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(DiscussionThreadBundle bundle) {
		view.clear();
		view.setTitle(bundle.getTitle());
		view.setActiveUsers(bundle.getActiveAuthors().toString());
		view.setNumberOfReplies(bundle.getNumberOfReplies().toString());
		view.setNumberOfViews(bundle.getNumberOfViews().toString());
		view.setLastActivity(gwtWrapper.getFormattedDateString(bundle.getLastActivity()));
		view.setAuthor(bundle.getCreatedBy());
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
		view.toggleThread();
	}

	@Override
	public void toggleReplies() {
		configureReplies();
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
