package org.sagebionetworks.web.client.widget.discussion;

import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.SHOW_REPLY_DETAILS_FOR_THREAD_LIST;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.SHOW_THREAD_DETAILS_FOR_THREAD_LIST;

import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListWidget implements DiscussionThreadListWidgetView.Presenter{

	public static final Long LIMIT = 10L;
	public static final DiscussionThreadOrder DEFAULT_ORDER = DiscussionThreadOrder.LAST_ACTIVITY;
	public static final Boolean DEFAULT_ASCENDING = false;
	public static final DiscussionFilter DEFAULT_FILTER = DiscussionFilter.EXCLUDE_DELETED;
	DiscussionThreadListWidgetView view;
	PortalGinInjector ginInjector;
	DiscussionForumClientAsync discussionForumClientAsync;
	SynapseAlert synAlert;
	private Long offset;
	private DiscussionThreadOrder order;
	private Boolean ascending;
	private String forumId;
	private Boolean isCurrentUserModerator;
	private CallbackP<Boolean> emptyListCallback;

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
		order = DEFAULT_ORDER;
		ascending = DEFAULT_ASCENDING;
	}

	public void configure(String forumId, Boolean isCurrentUserModerator, CallbackP<Boolean> emptyListCallback) {
		view.clear();
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.emptyListCallback = emptyListCallback;
		offset = 0L;
		this.forumId = forumId;
		loadMore();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void loadMore(){
		synAlert.clear();
		view.setLoadingVisible(true);
		discussionForumClientAsync.getThreadsForForum(forumId, LIMIT, offset,
				order, ascending, DEFAULT_FILTER,
				new AsyncCallback<PaginatedResults<DiscussionThreadBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						view.setLoadingVisible(false);
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(PaginatedResults<DiscussionThreadBundle> result) {
						for(DiscussionThreadBundle bundle: result.getResults()) {
							DiscussionThreadWidget thread = ginInjector.createThreadWidget();
							thread.configure(bundle, isCurrentUserModerator, new Callback(){

								@Override
								public void invoke() {
									configure(forumId, isCurrentUserModerator, emptyListCallback);
								}
							}, SHOW_THREAD_DETAILS_FOR_THREAD_LIST, SHOW_REPLY_DETAILS_FOR_THREAD_LIST);
							view.addThread(thread.asWidget());
						}
						offset += LIMIT;
						long numberOfThreads = result.getTotalNumberOfResults();
						view.setLoadingVisible(false);
						view.setLoadMoreButtonVisibility(offset < numberOfThreads);
						if (emptyListCallback != null) {
							emptyListCallback.invoke(numberOfThreads > 0);
						};
					}
		});
	}

	public void sortBy(DiscussionThreadOrder newOrder) {
		if (order == newOrder) {
			ascending = !ascending;
		} else {
			order = newOrder;
			ascending = DEFAULT_ASCENDING;
		}
		configure(forumId, isCurrentUserModerator, emptyListCallback);
	}
}
