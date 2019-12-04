package org.sagebionetworks.web.unitclient.widget.discussion;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.subscription.SubscriberPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.discussion.SubscribersWidget;
import org.sagebionetworks.web.client.widget.discussion.SubscribersWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SubscribersWidgetTest {
	@Mock
	SubscribersWidgetView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreWidgetContainer;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Mock
	SubscriberPagedResults mockSubscriberPagedResults;
	@Mock
	Topic mockTopic;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;

	SubscribersWidget widget;
	public static final Long TEST_SUBSCRIBER_COUNT = 44L;
	public static final String TEST_OBJECT_ID = "98765";
	public static final String TEST_NEXT_PAGE_TOKEN = "456765456y";
	List<String> subscribers;


	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new SubscribersWidget(mockView, mockGinInjector, mockSynAlert, mockLoadMoreWidgetContainer, mockSynapseJavascriptClient);
		AsyncMockStubber.callSuccessWith(mockSubscriberPagedResults).when(mockSynapseJavascriptClient).getSubscribers(any(Topic.class), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(TEST_SUBSCRIBER_COUNT).when(mockSynapseJavascriptClient).getSubscribersCount(any(Topic.class), any(AsyncCallback.class));
		when(mockTopic.getObjectType()).thenReturn(SubscriptionObjectType.FORUM);
		when(mockTopic.getObjectId()).thenReturn(TEST_OBJECT_ID);
		subscribers = new ArrayList<String>();
		when(mockSubscriberPagedResults.getSubscribers()).thenReturn(subscribers);
		when(mockGinInjector.getUserBadgeWidget()).thenReturn(mockUserBadge);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockView).setUserListContainer(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		widget.configure(mockTopic);

		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setSubscribersLinkVisible(false);
		inOrder.verify(mockView).setSubscriberCount(TEST_SUBSCRIBER_COUNT);
		inOrder.verify(mockView).setSubscribersLinkVisible(true);
	}

	@Test
	public void testConfigureNullCount() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseJavascriptClient).getSubscribersCount(any(Topic.class), any(AsyncCallback.class));
		widget.configure(mockTopic);

		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setSubscribersLinkVisible(false);
		inOrder.verify(mockView).clearSubscriberCount();
		inOrder.verify(mockView).setSubscribersLinkVisible(true);

		verify(mockView, never()).setSubscriberCount(anyLong());
	}


	@Test
	public void testConfigureFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getSubscribersCount(any(Topic.class), any(AsyncCallback.class));

		widget.configure(mockTopic);

		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setSubscribersLinkVisible(false);
		inOrder.verify(mockView).clearSubscriberCount();
		inOrder.verify(mockView).setSubscribersLinkVisible(true);

		verify(mockView, never()).setSubscriberCount(anyLong());
	}

	@Test
	public void testOnSubscribersLinkWithMorePages() {
		when(mockSubscriberPagedResults.getNextPageToken()).thenReturn(TEST_NEXT_PAGE_TOKEN);

		String subscriberId = "888888";
		subscribers.add(subscriberId);

		widget.onClickSubscribersLink();
		verify(mockGinInjector).getUserBadgeWidget();
		verify(mockUserBadge).configure(subscriberId);
		verify(mockSynAlert).clear();
		verify(mockLoadMoreWidgetContainer).clear();
		verify(mockView).showDialog();
		verify(mockLoadMoreWidgetContainer).add(any(Widget.class));
		verify(mockLoadMoreWidgetContainer).setIsMore(true);
	}

	@Test
	public void testLoadMoreSubscribersLastPage() {
		when(mockSubscriberPagedResults.getNextPageToken()).thenReturn(null);

		widget.loadMoreSubscribers();
		verify(mockLoadMoreWidgetContainer, never()).clear();
		verify(mockGinInjector, never()).getUserBadgeWidget();
		verify(mockLoadMoreWidgetContainer).setIsMore(false);
	}

	@Test
	public void testLoadMoreSubscribersFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getSubscribers(any(Topic.class), anyString(), any(AsyncCallback.class));

		widget.loadMoreSubscribers();
		verify(mockSynAlert).handleException(ex);
		verify(mockLoadMoreWidgetContainer).setIsMore(false);
	}

}
