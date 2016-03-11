package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.place.SubscriptionPlace;
import org.sagebionetworks.web.client.view.SubscriptionView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.subscription.TopicWidget;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SubscriptionPresenter extends AbstractActivity implements SubscriptionView.Presenter, Presenter<SubscriptionPlace> {
	private SubscriptionPlace place;
	private SubscriptionView view;
	private SubscriptionClientAsync subscriptionClient;
	private SynapseAlert synAlert;
	private TopicWidget topicWidget;
	private GlobalApplicationState globalAppState;
	
	@Inject
	public SubscriptionPresenter(SubscriptionView view,
			SubscriptionClientAsync subscriptionClient,
			SynapseAlert synAlert,
			UserGroupSuggestionProvider provider,
			GlobalApplicationState globalAppState,
			TopicWidget topicWidget
			) {
		this.view = view;
		this.subscriptionClient = subscriptionClient;
		this.synAlert = synAlert;
		this.globalAppState = globalAppState;
		this.topicWidget = topicWidget;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		view.setTopicWidget(topicWidget.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}
	
	@Override
	public void setPlace(SubscriptionPlace place) {
		this.place = place;
		this.view.setPresenter(this);
		synAlert.clear();
		String subscriptionIdParam = place.getParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM);
		String objectIdParam =  place.getParam(SubscriptionPlace.OBJECT_ID_PARAM);
		String objectTypeParam =  place.getParam(SubscriptionPlace.OBJECT_TYPE_PARAM);
		
		if (subscriptionIdParam != null) {
			//assume subscribed.  look for subscription...
			view.selectFollow();
			subscriptionClient.getSubscription(Long.parseLong(subscriptionIdParam), new AsyncCallback<Subscription>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
				public void onSuccess(Subscription result) {
					//configure the topic renderer
					//note, the topic widget knows how to render different types.  For Forum, for example, it will get the project ID from a servlet call.
					topicWidget.configure(result.getObjectType(), result.getObjectId());
				};
			});
			
			
		} else if (objectIdParam != null && objectTypeParam != null){
			//not subscribed, but have enough info to subscribe
			//configure the topic renderer
			view.selectUnfollow();
			topicWidget.configure(SubscriptionObjectType.valueOf(objectTypeParam), objectIdParam);
			
		} else {
			synAlert.showError("Missing subscription and topic information.");
		}
	}
	/**
	 * For testing
	 * @return
	 */
	public SubscriptionPlace getPlace() {
		return place;
	}
	
	private void clearPlaceParams() {
		place.removeParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM);
		place.removeParam(SubscriptionPlace.OBJECT_ID_PARAM);
		place.removeParam(SubscriptionPlace.OBJECT_TYPE_PARAM);
	}
	
	@Override
	public void onFollow() {
		String objectIdParam =  place.getParam(SubscriptionPlace.OBJECT_ID_PARAM);
		String objectTypeParam =  place.getParam(SubscriptionPlace.OBJECT_TYPE_PARAM);
		
		// subscribe based on object type and id
		Topic newTopic = new Topic();
		newTopic.setObjectId(objectIdParam);
		newTopic.setObjectType(SubscriptionObjectType.valueOf(objectTypeParam));
		subscriptionClient.subscribe(newTopic, new AsyncCallback<Subscription>() {
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			};
			public void onSuccess(Subscription subscription) {
				//on success:
				clearPlaceParams();
				place.putParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM, subscription.getSubscriptionId());
				globalAppState.pushCurrentPlace(place);
				setPlace(place);
				view.showInfo("You are now following this topic.", "");
			};
		});
	}
	
	@Override
	public void onUnfollow() {
		synAlert.clear();
		String subscriptionIdParam = place.getParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM);
		final Long subscriptionId = Long.parseLong(subscriptionIdParam);
		//unsubscribe does not have the object id/type, get it first.
		subscriptionClient.getSubscription(subscriptionId, new AsyncCallback<Subscription>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			public void onSuccess(Subscription result) {
				String objectId = result.getObjectId();
				SubscriptionObjectType objectType = result.getObjectType();
				unsubscribe(subscriptionId, objectId, objectType);
			};
		});
		
	}
	
	private void unsubscribe(Long subscriptionId, final String objectId, final SubscriptionObjectType objectType) {
		synAlert.clear();
		subscriptionClient.unsubscribe(subscriptionId, new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			};
			public void onSuccess(Void v) {
				clearPlaceParams();
				place.putParam(SubscriptionPlace.OBJECT_ID_PARAM, objectId);
				place.putParam(SubscriptionPlace.OBJECT_TYPE_PARAM, objectType.name());
				globalAppState.pushCurrentPlace(place);
				setPlace(place);
				view.showInfo("You are no longer following this topic.", "");
			};
		});
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
}
