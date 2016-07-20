package org.sagebionetworks.web.client.widget.discussion;

import java.util.Set;

import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
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
	GWTWrapper gwtWrapper;
	private Long offset;
	private DiscussionThreadOrder order;
	private Boolean ascending;
	private String forumId;
	private Boolean isCurrentUserModerator;
	private CallbackP<Boolean> emptyListCallback;
	private CallbackP<String> threadIdClickedCallback;
	Set<Long> moderatorIds;
	private Callback invokeCheckForInViewAndLoadData;
	private DiscussionFilter filter;
	
	@Inject
	public DiscussionThreadListWidget(
			DiscussionThreadListWidgetView view,
			PortalGinInjector ginInjector,
			DiscussionForumClientAsync discussionForumClientAsync,
			SynapseAlert synAlert,
			GWTWrapper gwtWrapper
			) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.discussionForumClientAsync = discussionForumClientAsync;
		this.synAlert = synAlert;
		this.gwtWrapper= gwtWrapper;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		order = DEFAULT_ORDER;
		ascending = DEFAULT_ASCENDING;
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
		invokeCheckForInViewAndLoadData = new Callback() {
			@Override
			public void invoke() {
				checkForInViewAndLoadData();
			}
		};
		loadMore();
		DiscussionThreadCountAlert threadCountAlert = ginInjector.getDiscussionThreadCountAlert();
		view.setThreadCountAlert(threadCountAlert.asWidget());
		threadCountAlert.configure(forumId);
	}

	public void checkForInViewAndLoadData() {
		if (!view.isLoadMoreAttached()) {
			//Done, view has been detached and widget was never in the viewport
			return;
		} else if (view.isLoadMoreInViewport() && view.getLoadMoreVisibility()) {
			//try to load data!
			loadMore();
		} else {
			//wait for a few seconds and see if we should load data
			gwtWrapper.scheduleExecution(invokeCheckForInViewAndLoadData, DisplayConstants.DELAY_UNTIL_IN_VIEW);
		}
	}

	public void clear() {
		view.clear();
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
		view.setLoadMoreVisibility(true);
		discussionForumClientAsync.getThreadsForForum(forumId, LIMIT, offset,
				order, ascending, filter, new AsyncCallback<PaginatedResults<DiscussionThreadBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						view.setLoadMoreVisibility(false);
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
							view.addThread(thread.asWidget());
						}
						
						offset += LIMIT;
						long numberOfThreads = result.getTotalNumberOfResults();
						view.setLoadMoreVisibility(offset < numberOfThreads);
						if (offset < numberOfThreads) {
							gwtWrapper.scheduleExecution(invokeCheckForInViewAndLoadData, DisplayConstants.DELAY_UNTIL_IN_VIEW);
						}
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
