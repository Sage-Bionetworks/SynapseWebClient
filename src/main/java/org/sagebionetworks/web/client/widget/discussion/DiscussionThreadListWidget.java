package org.sagebionetworks.web.client.widget.discussion;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
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
	SynapseJSNIUtils jsniUtils;
	SynapseAlert synAlert;
	private Long offset;
	private DiscussionThreadOrder order;
	private Boolean ascending;
	private String forumId;
	private Boolean isCurrentUserModerator;
	private CallbackP<Boolean> emptyListCallback;
	private CallbackP<DiscussionThreadBundle> threadIdClickedCallback;
	Set<String> moderatorIds;
	private DiscussionFilter filter;
	private LoadMoreWidgetContainer threadsContainer;
	private String entityId;
	private LoadMoreWidgetContainer loadMoreWidgetContainer;
	private Map<String, DiscussionThreadListItemWidget> threadId2Widget = new HashMap<String, DiscussionThreadListItemWidget>();
	@Inject
	public DiscussionThreadListWidget(
			DiscussionThreadListWidgetView view,
			PortalGinInjector ginInjector,
			DiscussionForumClientAsync discussionForumClientAsync,
			SynapseAlert synAlert,
			LoadMoreWidgetContainer loadMoreWidgetContainer,
			SynapseJSNIUtils jsniUtils
			) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.synAlert = synAlert;
		this.threadsContainer = loadMoreWidgetContainer;
		this.jsniUtils = jsniUtils;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		order = DEFAULT_ORDER;
		ascending = DEFAULT_ASCENDING;
		this.loadMoreWidgetContainer = loadMoreWidgetContainer;
		view.setThreadsContainer(loadMoreWidgetContainer);
	}

	public void configure(String forumId, Boolean isCurrentUserModerator,
			Set<String> moderatorIds, CallbackP<Boolean> emptyListCallback,
			DiscussionFilter filter) {
		clear();
		threadId2Widget.clear();
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
		loadMoreWidgetContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
		loadMore();
		DiscussionThreadCountAlert threadCountAlert = ginInjector.getDiscussionThreadCountAlert();
		view.setThreadCountAlert(threadCountAlert.asWidget());
		threadCountAlert.configure(forumId);
	}

	public void configure(String entityId, CallbackP<Boolean> emptyListCallback,
			DiscussionFilter filter) {
		clear();
		this.emptyListCallback = emptyListCallback;
		offset = 0L;
		this.entityId = entityId;
		if (filter != null) {
			this.filter = filter;
		} else {
			this.filter = DEFAULT_FILTER;
		}
		loadMoreWidgetContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMoreThreadsForEntity();
			}
		});
		loadMoreThreadsForEntity();
	}

	public void loadMoreThreadsForEntity() {
		synAlert.clear();
		discussionForumClientAsync.getThreadsForEntity(entityId, LIMIT, offset, order, ascending, filter, getLoadMoreCallback());
	}

	public AsyncCallback<PaginatedResults<DiscussionThreadBundle>> getLoadMoreCallback() {
		return new AsyncCallback<PaginatedResults<DiscussionThreadBundle>>(){

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
							threadId2Widget.put(bundle.getId(), thread);
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
		};
	}

	public void clear() {
		threadsContainer.clear();
	}
	
	public void setThreadIdClickedCallback(CallbackP<DiscussionThreadBundle> threadIdClickedCallback) {
		this.threadIdClickedCallback = threadIdClickedCallback;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void loadMore() {
		synAlert.clear();
		discussionForumClientAsync.getThreadsForForum(forumId, LIMIT, offset,
				order, ascending, filter, getLoadMoreCallback());
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
	
	public void scrollToThread(String threadId) {
		if (threadId2Widget.containsKey(threadId)) {
			//update thread data and scroll into view
			final DiscussionThreadListItemWidget threadListItemWidget = threadId2Widget.get(threadId);
			discussionForumClientAsync.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>() {
				@Override
				public void onFailure(Throwable caught) {
					// unable to update thread data
					jsniUtils.consoleError(caught.getMessage());
				}
				@Override
				public void onSuccess(DiscussionThreadBundle bundle) {
					threadListItemWidget.configure(bundle);
				}
			});
			view.scrollIntoView(threadListItemWidget.asWidget());
		}
	}
}
