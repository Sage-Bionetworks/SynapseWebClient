package org.sagebionetworks.web.client.widget.discussion;

import java.util.List;
import org.sagebionetworks.repo.model.subscription.SubscriberPagedResults;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubscribersWidget implements SubscribersWidgetView.Presenter, IsWidget {

	SubscribersWidgetView view;
	SynapseAlert synAlert;
	Topic topic;
	LoadMoreWidgetContainer loadMoreWidgetContainer;
	String nextPageToken;
	PortalGinInjector ginInjector;
	SynapseJavascriptClient jsClient;

	@Inject
	public SubscribersWidget(SubscribersWidgetView view, PortalGinInjector ginInjector, SynapseAlert synAlert, LoadMoreWidgetContainer loadMoreWidgetContainer, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.synAlert = synAlert;
		this.loadMoreWidgetContainer = loadMoreWidgetContainer;
		this.jsClient = jsClient;
		view.setPresenter(this);
		view.setSynapseAlert(synAlert.asWidget());
		view.setUserListContainer(loadMoreWidgetContainer.asWidget());
	}

	public void configure(Topic topic) {
		this.topic = topic;
		synAlert.clear();
		// get the count
		view.setSubscribersLinkVisible(false);
		jsClient.getSubscribersCount(topic, new AsyncCallback<Long>() {
			private void countIsUnavailable() {
				view.clearSubscriberCount();
				view.setSubscribersLinkVisible(true);
			}

			@Override
			public void onFailure(Throwable caught) {
				// unable to get the count, ignore
				countIsUnavailable();
			}

			@Override
			public void onSuccess(Long count) {
				if (count != null) {
					view.setSubscriberCount(count);
					view.setSubscribersLinkVisible(count > 0);
				} else {
					countIsUnavailable();
				}
			}
		});
	}

	@Override
	public void onClickSubscribersLink() {
		// show the dialog and start getting the subscribers
		loadMoreWidgetContainer.clear();
		nextPageToken = null;
		loadMoreWidgetContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMoreSubscribers();
			}
		});
		loadMoreSubscribers();
		view.showDialog();
	}

	public void loadMoreSubscribers() {
		synAlert.clear();
		jsClient.getSubscribers(topic, nextPageToken, new AsyncCallback<SubscriberPagedResults>() {
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
					UserBadge userBadge = ginInjector.getUserBadgeWidget();
					userBadge.configure(userId);
					loadMoreWidgetContainer.add(userBadge.asWidget());
				}
				loadMoreWidgetContainer.setIsMore(nextPageToken != null);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/**
	 * for testing purposes only
	 * 
	 * @return
	 */
	public String getNextPageToken() {
		return nextPageToken;
	}

}
