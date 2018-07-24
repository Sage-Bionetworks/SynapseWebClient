package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.subscription.SortByType;
import org.sagebionetworks.repo.model.subscription.SortDirection;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionRequest;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("subscriptionclient")	
public interface SubscriptionClient extends RemoteService {
	
	SubscriptionPagedResults getAllSubscriptions(SubscriptionObjectType objectType, Long limit, Long offset,  SortByType sortByType, SortDirection sortDirection) throws RestServiceException;
	
	Subscription getSubscription(Long subscriptionId) throws RestServiceException;
	
	Subscription subscribe(Topic toSubscribe) throws RestServiceException;
	
	void unsubscribe(Long subscriptionId) throws RestServiceException;
	
	SubscriptionPagedResults listSubscription(SubscriptionRequest request)
			throws RestServiceException;
}
