package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.place.ACTPlace;
import org.sagebionetworks.web.client.place.SubscriptionPlace;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ACTView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SubscriptionPresenter extends AbstractActivity implements SubscriptionView.Presenter, Presenter<SubscriptionPlace> {
	private SubscriptionPlace place;
	private SubscriptionView view;
	private SubscriptionClientAsync subscriptionClient;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	private GlobalApplicationState globalAppState;
	private Long submitterIdFilter;
	
	@Inject
	public SubscriptionPresenter(SubscriptionView view,
			SubscriptionClientAsync subscriptionClient,
			SynapseAlert synAlert,
			UserGroupSuggestionProvider provider,
			PortalGinInjector ginInjector,
			GlobalApplicationState globalAppState,
			TopicWidget topicWidget
			) {
		this.view = view;
		this.subscriptionClient = subscriptionClient;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.globalAppState = globalAppState;
		
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
		String subscriptionIdParam = place.getParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM);
		String objectIdParam =  place.getParam(SubscriptionPlace.OBJECT_ID_PARAM);
		String objectTypeParam =  place.getParam(SubscriptionPlace.OBJECT_TYPE_PARAM);
		
		if (subscriptionIdParam != null) {
			//TODO: assume subscribed.  look for subscription...
			
			//configure the topic renderer
			topicWidget.configure(SubscriptionObjectType.valueOf(objectType, objectId);
			//note, the topic widget knows how to render different types.  For Forum, for example, it will get the project ID from a servlet call.
			
		} else if (objectIdParam != null && objectTypeParam != null){
			//TODO: not subscribed, but have enough info to subscribe
			
			clearPlaceParams();
			place.putParam(SubscriptionPlace.OBJECT_ID_PARAM, objectIdParam);
			place.putParam(SubscriptionPlace.OBJECT_TYPE_PARAM, objectTypeParam);
			globalAppState.pushCurrentPlace(place);
			//configure the topic renderer
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
				view.showInfo("Successfully followed the topic.");
			};
		});
	}
	
	@Override
	public void onUnfollow() {
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
				view.showInfo("Successfully unfollowed the topic");
			};
		});
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
}
