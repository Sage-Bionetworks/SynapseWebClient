package org.sagebionetworks.web.unitclient.widget.subscription;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.TopicUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.subscription.SubscriptionListWidgetView;
import org.sagebionetworks.web.client.widget.subscription.SubscriptionListWidget;
import org.sagebionetworks.web.client.widget.subscription.TopicRowWidget;
import org.sagebionetworks.web.client.widget.subscription.TopicWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.springframework.mock.web.MockPageContext;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;


public class SubscriptionListWidgetTest {
	SubscriptionListWidget widget;
	
	@Mock
	SubscriptionListWidgetView mockView; 
	@Mock
	SubscriptionClientAsync mockSubscriptionClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	SubscriptionPagedResults mockSubscriptionPagedResults;
	@Mock
	TopicRowWidget mockTopicRowWidget;
	@Mock
	DetailedPaginationWidget mockDetailedPaginationWidget;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new SubscriptionListWidget(mockView, mockSubscriptionClient, mockPortalGinInjector, mockSynAlert, mockAuthenticationController, mockDetailedPaginationWidget);
		AsyncMockStubber.callSuccessWith(mockSubscriptionPagedResults).when(mockSubscriptionClient)
			.getAllSubscriptions(any(SubscriptionObjectType.class), anyLong(), anyLong(), any(AsyncCallback.class));
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockPortalGinInjector.getTopicRowWidget()).thenReturn(mockTopicRowWidget);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockView).setPagination(any(Widget.class));
	}

	@Test
	public void testConfigureAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.configure();
		verify(mockView).clearFilter();
		verify(mockView).clearSubscriptions();
		verify(mockSubscriptionClient, never())
			.getAllSubscriptions(any(SubscriptionObjectType.class), anyLong(), anyLong(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigure() {
		//simulate returning a full result, and then less than a full page result.
		Long expectedOffset = 0L;
		Long expectedTotalCount = SubscriptionListWidget.LIMIT*2;
		//default FORUM type
		SubscriptionObjectType expectedFilter = SubscriptionObjectType.FORUM;
		List<Subscription> results = new ArrayList<Subscription>();
		for (int i = 0; i < SubscriptionListWidget.LIMIT; i++) {
			results.add(new Subscription());
		}
		when(mockSubscriptionPagedResults.getResults()).thenReturn(results);
		when(mockSubscriptionPagedResults.getTotalNumberOfResults()).thenReturn(expectedTotalCount);
		widget.configure();
		verify(mockView).clearFilter();
		verify(mockView, times(2)).clearSubscriptions();
		verify(mockView, times(2)).setNoItemsMessageVisible(false);
		verify(mockView).setLoadingVisible(true);
		verify(mockSubscriptionClient)
			.getAllSubscriptions(eq(expectedFilter), eq(SubscriptionListWidget.LIMIT), eq(expectedOffset), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		
		//full result set.  create a TopicRowWidget for each subscription
		verify(mockDetailedPaginationWidget).configure(SubscriptionListWidget.LIMIT, expectedOffset, expectedTotalCount, widget);
		verify(mockTopicRowWidget, times(SubscriptionListWidget.LIMIT.intValue())).configure(any(Subscription.class));
		verify(mockView, times(SubscriptionListWidget.LIMIT.intValue())).addNewSubscription(any(Widget.class));
		
		//now try to get more, but return zero results
		expectedOffset += SubscriptionListWidget.LIMIT;
		results.clear();
		reset(mockView);
		reset(mockTopicRowWidget);
		//simulate the pagination widget going to the second page
		widget.onPageChange(expectedOffset);
		verify(mockSubscriptionClient)
			.getAllSubscriptions(eq(expectedFilter), eq(SubscriptionListWidget.LIMIT), eq(expectedOffset), any(AsyncCallback.class));
	
		verify(mockTopicRowWidget, never()).configure(any(Subscription.class));
		verify(mockView, never()).addNewSubscription(any(Widget.class));
	}
	
	@Test
	public void testOnFilter() {
		//simulate returning a full result, and then less than a full page result.
		Long expectedOffset = 0L;
		Long expectedTotalCount = 0L;
		SubscriptionObjectType expectedFilter = null;
		when(mockSubscriptionPagedResults.getResults()).thenReturn(new ArrayList<Subscription>());
		when(mockSubscriptionPagedResults.getTotalNumberOfResults()).thenReturn(expectedTotalCount);
		widget.onFilter(expectedFilter);
		verify(mockView).clearSubscriptions();
		verify(mockView).setNoItemsMessageVisible(false);
		verify(mockView).setNoItemsMessageVisible(true);
		verify(mockSubscriptionClient)
			.getAllSubscriptions(eq(expectedFilter), eq(SubscriptionListWidget.LIMIT), eq(expectedOffset), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		
		//empty set
		verify(mockDetailedPaginationWidget).configure(SubscriptionListWidget.LIMIT, expectedOffset, expectedTotalCount, widget);
		verify(mockTopicRowWidget, never()).configure(any(Subscription.class));
		verify(mockView, never()).addNewSubscription(any(Widget.class));
	}
	
	@Test
	public void testGetAllSubscriptionsError() {
		Exception ex = new Exception("nope");
		AsyncMockStubber.callFailureWith(ex).when(mockSubscriptionClient)
			.getAllSubscriptions(any(SubscriptionObjectType.class), anyLong(), anyLong(), any(AsyncCallback.class));
		widget.configure();
		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(ex);;
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
