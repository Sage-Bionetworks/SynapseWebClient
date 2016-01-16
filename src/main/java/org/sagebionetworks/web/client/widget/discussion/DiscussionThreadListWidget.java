package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListWidget implements DiscussionThreadListWidgetView.Presenter{

	public static final Long LIMIT = 10L;
	public static final DiscussionThreadOrder DEFAULT_ORDER = DiscussionThreadOrder.LAST_ACTIVITY;
	public static final Boolean DEFAULT_ASCENDING = false;
	DiscussionThreadListWidgetView view;
	PortalGinInjector ginInjector;
	DiscussionForumClientAsync discussionForumClientAsync;
	SynapseAlert synAlert;
	private Long offset;
	private DiscussionThreadOrder order;
	private Boolean ascending;

	@Inject
	public DiscussionThreadListWidget(
			DiscussionThreadListWidgetView view,
			PortalGinInjector ginInjector,
			DiscussionForumClientAsync discussionForumClientAsync,
			SynapseAlert synAlert
			) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.synAlert = synAlert;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
	}

	public void configure(String forumId) {
		view.clear();
		offset = 0L;
		if (order == null) {
			order = DEFAULT_ORDER;
		}
		if (ascending == null) {
			ascending = DEFAULT_ASCENDING;
		}
		discussionForumClientAsync.getThreadsForForum(forumId, LIMIT, offset, order, ascending,
				new AsyncCallback<PaginatedResults<DiscussionThreadBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						synAlert.clear();
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(PaginatedResults<DiscussionThreadBundle> result) {
						for(DiscussionThreadBundle bundle: result.getResults()) {
							DiscussionThreadWidget thread = ginInjector.createThreadWidget();
							thread.configure(bundle);
							view.addThread(thread.asWidget());
						}
						offset += LIMIT;
						view.setLoadMoreButtonVisibility(offset < result.getTotalNumberOfResults());
					}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void loadMore() {
		// TODO Auto-generated method stub
		
	}
}
