package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("subscriptionclient")	
public interface SubscriptionClient extends RemoteService {
	
	SubscriptionPagedResults getAllSubscriptions(SubscriptionObjectType objectType, Long limit, Long offset) throws RestServiceException;
	
	Subscription getSubscription(String subscriptionId) throws RestServiceException;
	
	Subscription subscribe(Topic toSubscribe) throws RestServiceException;
	
	void unsubscribe(Long subscriptionId) throws RestServiceException;
	
	void unsubscribeAll() throws RestServiceException;
}
