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
import org.sagebionetworks.web.client.widget.subscription.SubscriptionListWidgetView;
import org.sagebionetworks.web.client.widget.subscription.SubscriptionListWidget;
import org.sagebionetworks.web.client.widget.subscription.TopicRowWidget;
import org.sagebionetworks.web.client.widget.subscription.TopicWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

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
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new SubscriptionListWidget(mockView, mockSubscriptionClient, mockPortalGinInjector, mockSynAlert, mockAuthenticationController);
		AsyncMockStubber.callSuccessWith(mockSubscriptionPagedResults).when(mockSubscriptionClient)
			.getAllSubscriptions(any(SubscriptionObjectType.class), anyLong(), anyLong(), any(AsyncCallback.class));
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockPortalGinInjector.getTopicRowWidget()).thenReturn(mockTopicRowWidget);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(any(Widget.class));
	}

	@Test
	public void testConfigureAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.configure();
		verify(mockView).clearFilter();
		verify(mockView).clearSubscriptions();
		verify(mockView).setNoItemsMessageVisible(true);
		verify(mockSubscriptionClient, never())
			.getAllSubscriptions(any(SubscriptionObjectType.class), anyLong(), anyLong(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigure() {
		//simulate returning a full result, and then less than a full page result.
		Long expectedOffset = 0L;
		SubscriptionObjectType expectedFilter = null;
		List<Subscription> results = new ArrayList<Subscription>();
		for (int i = 0; i < SubscriptionListWidget.LIMIT; i++) {
			results.add(new Subscription());
		}
		when(mockSubscriptionPagedResults.getResults()).thenReturn(results);
		widget.configure();
		verify(mockView).clearFilter();
		verify(mockView).clearSubscriptions();
		verify(mockView).setNoItemsMessageVisible(true);
		verify(mockSubscriptionClient)
			.getAllSubscriptions(eq(expectedFilter), eq(SubscriptionListWidget.LIMIT), eq(expectedOffset), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		
		//full result set.  show more button, and create a TopicRowWidget for each subscription
		verify(mockView).setNoItemsMessageVisible(false);
		verify(mockView).setMoreButtonVisible(true);
		verify(mockTopicRowWidget, times(SubscriptionListWidget.LIMIT.intValue())).configure(any(Subscription.class));
		verify(mockView, times(SubscriptionListWidget.LIMIT.intValue())).addNewSubscription(any(Widget.class));
		
		//now try to get more, but return zero results
		expectedOffset += SubscriptionListWidget.LIMIT;
		results.clear();
		reset(mockView);
		reset(mockTopicRowWidget);
		widget.onMore();
		verify(mockSubscriptionClient)
			.getAllSubscriptions(eq(expectedFilter), eq(SubscriptionListWidget.LIMIT), eq(expectedOffset), any(AsyncCallback.class));
	
		verify(mockView).setMoreButtonVisible(false);
		verify(mockTopicRowWidget, never()).configure(any(Subscription.class));
		verify(mockView, never()).addNewSubscription(any(Widget.class));
	}
	
	@Test
	public void testOnFilter() {
		//simulate returning a full result, and then less than a full page result.
		Long expectedOffset = 0L;
		SubscriptionObjectType expectedFilter = SubscriptionObjectType.FORUM;
		when(mockSubscriptionPagedResults.getResults()).thenReturn(new ArrayList<Subscription>());
		widget.onFilter(expectedFilter);
		verify(mockView).clearSubscriptions();
		verify(mockView).setNoItemsMessageVisible(true);
		verify(mockSubscriptionClient)
			.getAllSubscriptions(eq(expectedFilter), eq(SubscriptionListWidget.LIMIT), eq(expectedOffset), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		
		//empty set, should never hide the message
		verify(mockView, never()).setNoItemsMessageVisible(false);
		verify(mockView).setMoreButtonVisible(false);
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
