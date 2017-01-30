package org.sagebionetworks.web.client.widget.discussion;

import java.util.List;

import org.sagebionetworks.repo.model.subscription.SubscriberCount;
import org.sagebionetworks.repo.model.subscription.SubscriberPagedResults;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeList;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FollowersWidget implements FollowersWidgetView.Presenter, IsWidget {

	UserBadgeList userBadgeList;
	FollowersWidgetView view;
	SynapseAlert synAlert;
	DiscussionForumClientAsync discussionForumClientAsync;
	Topic topic;
	LoadMoreWidgetContainer loadMoreWidgetContainer;
	String nextPageToken;
	@Inject
	public FollowersWidget(
			FollowersWidgetView view,
			UserBadgeList userBadgeList,
			SynapseAlert synAlert,
			LoadMoreWidgetContainer loadMoreWidgetContainer,
			DiscussionForumClientAsync discussionForumClientAsync
			) {
		this.view = view;
		this.userBadgeList = userBadgeList;
		this.synAlert = synAlert;
		this.loadMoreWidgetContainer = loadMoreWidgetContainer;
		this.discussionForumClientAsync = discussionForumClientAsync;
		view.setPresenter(this);
		view.setSynapseAlert(synAlert.asWidget());
		view.setUserListContainer(loadMoreWidgetContainer.asWidget());
	}

	public void configure(Topic topic) {
		this.topic = topic;
		synAlert.clear();
		// get the count
		view.clearFollowerCount();
		view.setFollowersLinkVisible(false);
		discussionForumClientAsync.getSubscribersCount(topic, new AsyncCallback<Long>(){
			@Override
			public void onFailure(Throwable caught) {
				// unable to get the count, ignore
				view.setFollowersLinkVisible(true);
			}

			@Override
			public void onSuccess(Long count) {
				if (count != null) {
					view.setFollowerCount(count);
					view.setFollowersLinkVisible(count > 0);
				} else {
					view.setFollowersLinkVisible(true);	
				}
			}
		});
	}

	@Override
	public void onClickFollowersLink() {
		// show the dialog and start getting the subscribers
		userBadgeList.clear();
		nextPageToken = null;
		loadMoreWidgetContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMoreFollowers();
			}
		});
		loadMoreFollowers();
		view.showDialog();
	}
	
	public void loadMoreFollowers() {
		discussionForumClientAsync.getSubscribers(topic, nextPageToken, new AsyncCallback<SubscriberPagedResults>(){
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				loadMoreWidgetContainer.setIsMore(false);
			}

			@Override
			public void onSuccess(SubscriberPagedResults results) {
				nextPageToken = results.getNextPageToken();
				List<String> subscribers = results.getSubscribers();
				for (String userId : subscribers) {
					userBadgeList.addUserBadge(userId);
				}
				loadMoreWidgetContainer.setIsMore(nextPageToken != null);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
