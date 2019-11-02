package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.SubscriptionPlace;
import org.sagebionetworks.web.client.view.SubscriptionView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.subscription.TopicWidget;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SubscriptionPresenter extends AbstractActivity implements SubscriptionView.Presenter, Presenter<SubscriptionPlace> {
	public static final String MISSING_PARAMS_MESSAGE = "Missing subscription and topic information.";
	private SubscriptionPlace place;
	private SubscriptionView view;
	private SynapseJavascriptClient jsClient;
	private SynapseAlert synAlert;
	private TopicWidget topicWidget;
	private GlobalApplicationState globalAppState;
	private SubscriptionObjectType objectType;
	private String objectId;

	@Inject
	public SubscriptionPresenter(SubscriptionView view, SynapseJavascriptClient jsClient, SynapseAlert synAlert, GlobalApplicationState globalAppState, TopicWidget topicWidget) {
		this.view = view;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.globalAppState = globalAppState;
		this.topicWidget = topicWidget;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		view.setTopicWidget(topicWidget.asWidget());
		topicWidget.addStyleNames("font-size-20");
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(SubscriptionPlace place) {
		this.place = place;
		synAlert.clear();
		String subscriptionIdParam = place.getParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM);
		String objectIdParam = place.getParam(SubscriptionPlace.OBJECT_ID_PARAM);
		String objectTypeParam = place.getParam(SubscriptionPlace.OBJECT_TYPE_PARAM);

		if (subscriptionIdParam != null) {
			// assume subscribed. look for subscription...
			view.selectSubscribedButton();
			jsClient.getSubscription(subscriptionIdParam, new AsyncCallback<Subscription>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}

				public void onSuccess(Subscription result) {
					// configure the topic renderer
					// note, the topic widget knows how to render different types. For Forum, for example, it will get
					// the project ID from a servlet call.
					objectType = result.getObjectType();
					objectId = result.getObjectId();
					topicWidget.configure(objectType, objectId);
				};
			});


		} else if (objectIdParam != null && objectTypeParam != null) {
			// not subscribed, but have enough info to subscribe
			// configure the topic renderer
			view.selectUnsubscribedButton();
			objectType = SubscriptionObjectType.valueOf(objectTypeParam.toUpperCase());
			objectId = objectIdParam;
			topicWidget.configure(objectType, objectId);
		} else {
			synAlert.showError(MISSING_PARAMS_MESSAGE);
		}
	}

	/**
	 * For testing
	 * 
	 * @return
	 */
	public SubscriptionPlace getPlace() {
		return place;
	}

	@Override
	public void onSubscribe() {
		// subscribe based on object type and id
		Topic newTopic = new Topic();
		newTopic.setObjectId(objectId);
		newTopic.setObjectType(objectType);
		jsClient.subscribe(newTopic, new AsyncCallback<Subscription>() {
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			};

			public void onSuccess(Subscription subscription) {
				// on success:
				place.clearParams();
				place.putParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM, subscription.getSubscriptionId());
				globalAppState.pushCurrentPlace(place);
				setPlace(place);
				view.showInfo("You are now following this topic.");
			};
		});
	}

	@Override
	public void onUnsubscribe() {
		synAlert.clear();
		String subscriptionIdParam = place.getParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM);
		jsClient.unsubscribe(subscriptionIdParam, new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			};

			public void onSuccess(Void v) {
				place.clearParams();
				place.putParam(SubscriptionPlace.OBJECT_ID_PARAM, objectId);
				place.putParam(SubscriptionPlace.OBJECT_TYPE_PARAM, objectType.name());
				globalAppState.pushCurrentPlace(place);
				setPlace(place);
				view.showInfo("You are no longer following this topic.");
			};
		});
	}

	@Override
	public String mayStop() {
		view.clear();
		return null;
	}

}
