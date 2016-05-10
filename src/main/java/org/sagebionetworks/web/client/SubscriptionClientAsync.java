package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionRequest;
import org.sagebionetworks.repo.model.subscription.Topic;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("subscriptionclient")
public interface SubscriptionClientAsync{

	void getAllSubscriptions(SubscriptionObjectType objectType, Long limit, Long offset, AsyncCallback<SubscriptionPagedResults> callback);

	void subscribe(Topic toSubscribe, AsyncCallback<Subscription> callback);

	void unsubscribe(Long subscriptionId, AsyncCallback<Void> callback);

	void unsubscribeAll(AsyncCallback<Void> callback);

	void getSubscription(Long subscriptionId,
			AsyncCallback<Subscription> callback);

	void listSubscription(SubscriptionRequest request,
			AsyncCallback<SubscriptionPagedResults> callback);
}
