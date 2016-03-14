package org.sagebionetworks.web.unitclient.presenter;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SubscriptionClientAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.place.SubscriptionPlace;
import org.sagebionetworks.web.client.presenter.SubscriptionPresenter;
import org.sagebionetworks.web.client.place.SynapseForumPlace;
import org.sagebionetworks.web.client.presenter.SynapseForumPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.SubscriptionView;
import org.sagebionetworks.web.client.view.SynapseForumView;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.subscription.TopicWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SubscriptionPresenterTest {
	@Mock
	SubscriptionView mockView;
	@Mock
	SubscriptionClientAsync mockSubscriptionClient;
	
	@Mock
	SubscriptionPlace mockPlace;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	TopicWidget mockTopicWidget;
	SubscriptionPresenter presenter;
	@Mock
	Subscription mockSubscription;
	private static final SubscriptionObjectType TEST_OBJECT_TYPE = SubscriptionObjectType.FORUM;
	private static final String TEST_OBJECT_ID = "3";
	private static final String TEST_SUBSCRIPTION_ID = "8837";
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		presenter = new SubscriptionPresenter(mockView, mockSubscriptionClient, mockSynAlert,
				mockGlobalApplicationState, mockTopicWidget);
		when(mockPlace.toToken()).thenReturn("fake token");
		when(mockSubscription.getSubscriptionId()).thenReturn(TEST_SUBSCRIPTION_ID);
		when(mockSubscription.getObjectId()).thenReturn(TEST_OBJECT_ID);
		when(mockSubscription.getObjectType()).thenReturn(TEST_OBJECT_TYPE);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(presenter);
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockView).setTopicWidget(any(Widget.class));
	}

	@Test
	public void testSubscribed() {
		//load the place with a subscription id
		AsyncMockStubber.callSuccessWith(mockSubscription).when(mockSubscriptionClient)
			.getSubscription(anyLong(), any(AsyncCallback.class));
		Long subscriptionId = 31416L;//happy pi day!
		when(mockPlace.getParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM)).thenReturn(subscriptionId.toString());
		presenter.setPlace(mockPlace);
		verify(mockSynAlert).clear();
		verify(mockView).selectSubscribedButton();
		verify(mockTopicWidget).configure(TEST_OBJECT_TYPE, TEST_OBJECT_ID);
		verify(mockSubscriptionClient).getSubscription(eq(subscriptionId), any(AsyncCallback.class));
		
		//now test unsubscribe action
		AsyncMockStubber.callSuccessWith(null).when(mockSubscriptionClient)
			.unsubscribe(anyLong(), any(AsyncCallback.class));
		presenter.onUnsubscribe();
		verify(mockPlace).clearParams();
		verify(mockPlace).putParam(SubscriptionPlace.OBJECT_ID_PARAM, TEST_OBJECT_ID);
		verify(mockPlace).putParam(SubscriptionPlace.OBJECT_TYPE_PARAM, TEST_OBJECT_TYPE.name());
		verify(mockGlobalApplicationState).pushCurrentPlace(mockPlace);
	}
	
	@Test
	public void testSubscribedFailure() {
		//load the place with a subscription id
		Exception ex = new Exception("error occurred");
		AsyncMockStubber.callFailureWith(ex).when(mockSubscriptionClient)
			.getSubscription(anyLong(), any(AsyncCallback.class));
		Long subscriptionId = 31416L;//happy pi day!
		when(mockPlace.getParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM)).thenReturn(subscriptionId.toString());
		presenter.setPlace(mockPlace);
		verify(mockSynAlert).clear();
		verify(mockView).selectSubscribedButton();
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testUnsubscribed() {
		//load the place with an object type and id
		when(mockPlace.getParam(SubscriptionPlace.OBJECT_ID_PARAM)).thenReturn(TEST_OBJECT_ID);
		when(mockPlace.getParam(SubscriptionPlace.OBJECT_TYPE_PARAM)).thenReturn(TEST_OBJECT_TYPE.name());
		presenter.setPlace(mockPlace);
		verify(mockSynAlert).clear();
		verify(mockView).selectUnsubscribedButton();
		verify(mockTopicWidget).configure(TEST_OBJECT_TYPE, TEST_OBJECT_ID);
		
		//now test subscribe action
		AsyncMockStubber.callSuccessWith(mockSubscription).when(mockSubscriptionClient)
			.subscribe(any(Topic.class), any(AsyncCallback.class));
		presenter.onSubscribe();
		verify(mockPlace).clearParams();
		verify(mockPlace).putParam(SubscriptionPlace.SUBSCRIPTION_ID_FILTER_PARAM, TEST_SUBSCRIPTION_ID);
		verify(mockGlobalApplicationState).pushCurrentPlace(mockPlace);
	}
	
	@Test
	public void testNoParams() {
		//load the place with a subscription id
		presenter.setPlace(mockPlace);
		verify(mockSynAlert).clear();
		verify(mockSynAlert).showError(SubscriptionPresenter.MISSING_PARAMS_MESSAGE);
	}
}
