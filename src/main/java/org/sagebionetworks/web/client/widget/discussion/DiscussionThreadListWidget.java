package org.sagebionetworks.web.client.widget.discussion;

import java.util.Set;

import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.refresh.DiscussionThreadCountAlert;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListWidget implements DiscussionThreadListWidgetView.Presenter{

	public static final Long LIMIT = 10L;
	public static final DiscussionThreadOrder DEFAULT_ORDER = DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY;
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
	private CallbackP<String> threadIdClickedCallback;
	Set<Long> moderatorIds;
	private DiscussionFilter filter;
	private LoadMoreWidgetContainer threadsContainer;
	@Inject
	public DiscussionThreadListWidget(
			DiscussionThreadListWidgetView view,
			PortalGinInjector ginInjector,
			DiscussionForumClientAsync discussionForumClientAsync,
			SynapseAlert synAlert,
			LoadMoreWidgetContainer loadMoreWidgetContainer
			) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.synAlert = synAlert;
		this.threadsContainer = loadMoreWidgetContainer;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		order = DEFAULT_ORDER;
		ascending = DEFAULT_ASCENDING;
		loadMoreWidgetContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
		view.setLoadMoreWidgetContainer(loadMoreWidgetContainer);
	}

	public void configure(String forumId, Boolean isCurrentUserModerator,
			Set<Long> moderatorIds, CallbackP<Boolean> emptyListCallback,
			DiscussionFilter filter) {
		clear();
		this.isCurrentUserModerator = isCurrentUserModerator;
		this.emptyListCallback = emptyListCallback;
		this.moderatorIds = moderatorIds;
		offset = 0L;
		this.forumId = forumId;
		if (filter != null) {
			this.filter = filter;
		} else {
			this.filter = DEFAULT_FILTER;
		}
		loadMore();
		DiscussionThreadCountAlert threadCountAlert = ginInjector.getDiscussionThreadCountAlert();
		view.setThreadCountAlert(threadCountAlert.asWidget());
		threadCountAlert.configure(forumId);
	}

	public void clear() {
		threadsContainer.clear();
	}
	
	public void setThreadIdClickedCallback(CallbackP<String> threadIdClickedCallback) {
		this.threadIdClickedCallback = threadIdClickedCallback;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void loadMore() {
		synAlert.clear();
		discussionForumClientAsync.getThreadsForForum(forumId, LIMIT, offset,
				order, ascending, filter, new AsyncCallback<PaginatedResults<DiscussionThreadBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						threadsContainer.setIsMore(false);
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(PaginatedResults<DiscussionThreadBundle> result) {
						for(DiscussionThreadBundle bundle: result.getResults()) {
							DiscussionThreadListItemWidget thread = ginInjector.createThreadListItemWidget();
							thread.configure(bundle);
							if (threadIdClickedCallback != null) {
								thread.setThreadIdClickedCallback(threadIdClickedCallback);
							}
							threadsContainer.add(thread.asWidget());
						}
						
						offset += LIMIT;
						long numberOfThreads = result.getTotalNumberOfResults();
						threadsContainer.setIsMore(offset < numberOfThreads);
						
						if (emptyListCallback != null) {
							emptyListCallback.invoke(numberOfThreads > 0);
						};
						view.setThreadHeaderVisible(numberOfThreads > 0);
						view.setNoThreadsFoundVisible(numberOfThreads == 0);
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
		configure(forumId, isCurrentUserModerator, moderatorIds, emptyListCallback, filter);
	}
}
