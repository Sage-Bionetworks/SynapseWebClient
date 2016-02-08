package org.sagebionetworks.web.client.widget.discussion;

import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;


public class DeletedThreadListWidget extends DiscussionThreadListWidget{

	@Inject
	public DeletedThreadListWidget(
			DiscussionThreadListWidgetView view,
			PortalGinInjector ginInjector,
			DiscussionForumClientAsync discussionForumClientAsync,
			SynapseAlert synAlert
			) {
		super(view, ginInjector, discussionForumClientAsync, synAlert);
	}

	public void loadMore() {
		synAlert.clear();
		view.setLoadingVisible(true);
		discussionForumClientAsync.getDeletedThreadsForForum(forumId, LIMIT, offset, order, ascending,
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
									configure(forumId, isCurrentUserModerator);
								}
							});
							view.addThread(thread.asWidget());
						}
						offset += LIMIT;
						long numberOfThreads = result.getTotalNumberOfResults();
						view.setLoadingVisible(false);
						view.setLoadMoreButtonVisibility(offset < numberOfThreads);
						if (numberOfThreads > 0) {
							view.setEmptyUIVisible(false);
							view.setThreadHeaderVisible(true);
						} else {
							view.setEmptyUIVisible(true);
							view.setThreadHeaderVisible(false);
						}
					}
		});
	}
}
