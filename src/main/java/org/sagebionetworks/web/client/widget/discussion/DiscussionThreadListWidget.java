package org.sagebionetworks.web.client.widget.discussion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.discussion.DiscussionFilter;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadOrder;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.refresh.DiscussionThreadCountAlert;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadListWidget implements DiscussionThreadListWidgetView.Presenter{

	public static final Long LIMIT = 20L;
	public static final DiscussionThreadOrder DEFAULT_ORDER = DiscussionThreadOrder.PINNED_AND_LAST_ACTIVITY;
	public static final Boolean DEFAULT_ASCENDING = false;
	public static final DiscussionFilter DEFAULT_FILTER = DiscussionFilter.EXCLUDE_DELETED;
	DiscussionThreadListWidgetView view;
	PortalGinInjector ginInjector;
	SynapseJSNIUtils jsniUtils;
	SynapseAlert synAlert;
	SynapseJavascriptClient jsClient;
	private Long offset;
	private DiscussionThreadOrder order;
	private Boolean ascending;
	private String forumId;
	private CallbackP<Boolean> emptyListCallback;
	private CallbackP<DiscussionThreadBundle> threadIdClickedCallback;
	Set<String> moderatorIds;
	private DiscussionFilter filter;
	private String entityId;
	private LoadMoreWidgetContainer loadMoreWidgetContainer;
	private Map<String, DiscussionThreadListItemWidget> threadId2Widget = new HashMap<String, DiscussionThreadListItemWidget>();
	
	@Inject
	public DiscussionThreadListWidget(
			DiscussionThreadListWidgetView view,
			PortalGinInjector ginInjector,
			SynapseAlert synAlert,
			LoadMoreWidgetContainer loadMoreWidgetContainer,
			SynapseJSNIUtils jsniUtils,
			SynapseJavascriptClient jsClient
			) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.synAlert = synAlert;
		this.jsniUtils = jsniUtils;
		this.jsClient = jsClient;
		view.setPresenter(this);
		view.setAlert(synAlert.asWidget());
		this.loadMoreWidgetContainer = loadMoreWidgetContainer;
		view.setThreadsContainer(loadMoreWidgetContainer);
	}

	public void configure(String forumId, Boolean isCurrentUserModerator,
			Set<String> moderatorIds, CallbackP<Boolean> emptyListCallback,
			DiscussionFilter filter) {
		order = DEFAULT_ORDER;
		ascending = DEFAULT_ASCENDING;
//		this.isCurrentUserModerator = isCurrentUserModerator;
		this.emptyListCallback = emptyListCallback;
		this.moderatorIds = moderatorIds;
		this.entityId = null;
		this.forumId = forumId;
		if (filter != null) {
			this.filter = filter;
		} else {
			this.filter = DEFAULT_FILTER;
		}
		loadMoreWidgetContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMoreThreadsForForum();
			}
		});
		
		DiscussionThreadCountAlert threadCountAlert = ginInjector.getDiscussionThreadCountAlert();
		view.setThreadCountAlert(threadCountAlert.asWidget());
		threadCountAlert.configure(forumId);
		loadInitialForumResults();
	}
	
	private void loadInitialForumResults() {
		clear();
		offset = 0L;
		loadMoreThreadsForForum();
	}

	public void configure(String entityId, CallbackP<Boolean> emptyListCallback,
			DiscussionFilter filter) {
		order = DEFAULT_ORDER;
		ascending = DEFAULT_ASCENDING;
		this.emptyListCallback = emptyListCallback;
		this.entityId = entityId;
		this.forumId = null;
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
		loadInitialEntityResults();
	}

	private void loadInitialEntityResults() {
		clear();
		offset = 0L;
		loadMoreThreadsForEntity();
	}

	public void loadMoreThreadsForEntity() {
		synAlert.clear();
		jsClient.getThreadsForEntity(entityId, LIMIT, offset, order, ascending, filter, getLoadMoreCallback());
	}

	public AsyncCallback<List<DiscussionThreadBundle>> getLoadMoreCallback() {
		return new AsyncCallback<List<DiscussionThreadBundle>>(){

					@Override
					public void onFailure(Throwable caught) {
						loadMoreWidgetContainer.setIsMore(false);
						synAlert.handleException(caught);
					}

					@Override
					public void onSuccess(List<DiscussionThreadBundle> results) {
						boolean isEmpty = results.isEmpty() && offset == 0; //no threads
						
						for(DiscussionThreadBundle bundle: results) {
							DiscussionThreadListItemWidget thread = ginInjector.createThreadListItemWidget();
							thread.configure(bundle);
							if (threadIdClickedCallback != null) {
								thread.setThreadIdClickedCallback(threadIdClickedCallback);
							}
							threadId2Widget.put(bundle.getId(), thread);
							loadMoreWidgetContainer.add(thread.asWidget());
						}
						
						offset += LIMIT;
						loadMoreWidgetContainer.setIsMore(results.size() == LIMIT);
						
						if (emptyListCallback != null) {
							emptyListCallback.invoke(!isEmpty);
						};
						view.setThreadHeaderVisible(!isEmpty);
						view.setNoThreadsFoundVisible(isEmpty);
					}
		};
	}

	public void clear() {
		view.clearSort();
		loadMoreWidgetContainer.clear();
		threadId2Widget.clear();
	}
	
	public void setThreadIdClickedCallback(CallbackP<DiscussionThreadBundle> threadIdClickedCallback) {
		this.threadIdClickedCallback = threadIdClickedCallback;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void loadMoreThreadsForForum() {
		synAlert.clear();
		jsClient.getThreadsForForum(forumId, LIMIT, offset,
				order, ascending, filter, getLoadMoreCallback());
	}

	public void sortBy(DiscussionThreadOrder newOrder) {
		if (order == newOrder) {
			ascending = !ascending;
		} else {
			order = newOrder;
			ascending = DEFAULT_ASCENDING;
		}
		if (entityId != null) {
			loadInitialEntityResults();
		} else {
			loadInitialForumResults();
		}
		view.setSorted(order, ascending);
	}
	
	public void scrollToThread(String threadId) {
		if (threadId2Widget.containsKey(threadId)) {
			//update thread data and scroll into view
			final DiscussionThreadListItemWidget threadListItemWidget = threadId2Widget.get(threadId);
			jsClient.getThread(threadId, new AsyncCallback<DiscussionThreadBundle>() {
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
