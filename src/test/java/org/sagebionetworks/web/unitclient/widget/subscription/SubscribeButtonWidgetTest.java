package org.sagebionetworks.web.unitclient.widget.subscription;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.model.subscription.SubscriptionRequest;
import org.sagebionetworks.repo.model.subscription.Topic;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidget;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SubscribeButtonWidgetTest {
	@Mock
	SubscribeButtonWidgetView mockView;
	@Mock
	SynapseJavascriptClient mockJsClient;

	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	SubscriptionPagedResults mockSubscriptionPagedResults;

	@Mock
	Callback mockSubscribeCallback;
	@Mock
	Callback mockUnsubscribeCallback;

	SubscribeButtonWidget widget;
	@Mock
	Subscription mockSubscription;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Captor
	ArgumentCaptor<ActionMenuWidget.ActionListener> actionListenerCaptor;
	private static final String TEST_OBJECT_ID = "3";
	private static final String TEST_SUBSCRIPTION_ID = "8837";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		widget = new SubscribeButtonWidget(mockView, mockJsClient, mockSynAlert, mockAuthenticationController, mockGlobalApplicationState);
		when(mockSubscription.getSubscriptionId()).thenReturn(TEST_SUBSCRIPTION_ID);
		when(mockSubscription.getObjectId()).thenReturn(TEST_OBJECT_ID);
		when(mockSubscription.getObjectType()).thenReturn(SubscriptionObjectType.FORUM);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);

		AsyncMockStubber.callSuccessWith(mockSubscriptionPagedResults).when(mockJsClient).listSubscription(any(SubscriptionRequest.class), any(AsyncCallback.class));

		// by default, return a single Subscription
		when(mockSubscriptionPagedResults.getTotalNumberOfResults()).thenReturn(1L);
		when(mockSubscriptionPagedResults.getResults()).thenReturn(Collections.singletonList(mockSubscription));
		widget.setOnSubscribeCallback(mockSubscribeCallback);
		widget.setOnUnsubscribeCallback(mockUnsubscribeCallback);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(any(Widget.class));
		assertFalse(widget.isIconOnly());
	}

	@Test
	public void testFollowButton() {
		widget.showFollowButton();
		verify(mockView).showFollowButton();
	}

	@Test
	public void testUnfollowButton() {
		widget.showUnfollowButton();
		verify(mockView).showUnfollowButton();
	}

	@Test
	public void testFollowIcon() {
		widget.showIconOnly();
		assertTrue(widget.isIconOnly());

		widget.showFollowButton();
		verify(mockView).showFollowIcon();
	}

	@Test
	public void testUnfollowIcon() {
		widget.showIconOnly();
		widget.showUnfollowButton();
		verify(mockView).showUnfollowIcon();
	}

	@Test
	public void testClear() {
		widget.clear();
		verify(mockView).clear();
	}

	@Test
	public void testAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.configure(SubscriptionObjectType.THREAD, "123", mockActionMenuWidget);
		// show the follow button. on click, send to the login place
		verify(mockView).showFollowButton();
		verify(mockActionMenuWidget).setActionListener(eq(Action.FOLLOW), actionListenerCaptor.capture());

		// simulate click
		actionListenerCaptor.getValue().onAction(Action.FOLLOW);
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_LOGIN_REQUIRED);
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
	}

	@Test
	public void testConfigureSubscribed() {
		widget.configure(SubscriptionObjectType.THREAD, "123", mockActionMenuWidget);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockView).showUnfollowButton();
		verify(mockActionMenuWidget).setActionListener(eq(Action.FOLLOW), any(ActionMenuWidget.ActionListener.class));
		verify(mockActionMenuWidget).setActionText(Action.FOLLOW, "Unfollow Thread");
	}

	@Test
	public void testConfigureUnsubscribed() {
		when(mockSubscriptionPagedResults.getTotalNumberOfResults()).thenReturn(0L);
		widget.configure(SubscriptionObjectType.FORUM, "123", mockActionMenuWidget);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockView).showFollowButton();
		verify(mockActionMenuWidget).setActionListener(eq(Action.FOLLOW), any(ActionMenuWidget.ActionListener.class));
		verify(mockActionMenuWidget).setActionText(Action.FOLLOW, "Follow Forum");
	}

	@Test
	public void testConfigureFailure() {
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).listSubscription(any(SubscriptionRequest.class), any(AsyncCallback.class));
		widget.configure(SubscriptionObjectType.THREAD, "123", mockActionMenuWidget);
		verify(mockView).clear();
		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testSubscribe() {
		AsyncMockStubber.callSuccessWith(mockSubscription).when(mockJsClient).subscribe(any(Topic.class), any(AsyncCallback.class));
		widget.onSubscribe();
		verify(mockSynAlert).clear();
		verify(mockJsClient).subscribe(any(Topic.class), any(AsyncCallback.class));
		assertEquals(mockSubscription, widget.getCurrentSubscription());
		verify(mockView).showUnfollowButton();
		verify(mockSubscribeCallback).invoke();
	}

	@Test
	public void testSubscribeFailure() {
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).subscribe(any(Topic.class), any(AsyncCallback.class));
		widget.onSubscribe();
		verify(mockSynAlert).clear();
		verify(mockJsClient).subscribe(any(Topic.class), any(AsyncCallback.class));
		assertNull(widget.getCurrentSubscription());
		verify(mockSynAlert).handleException(ex);
		verify(mockView).hideLoading();
		verify(mockSubscribeCallback, never()).invoke();
	}


	@Test
	public void testUnsubscribe() {
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).unsubscribe(anyString(), any(AsyncCallback.class));
		widget.configure(mockSubscription);
		widget.onUnsubscribe();
		verify(mockSynAlert).clear();
		verify(mockView).showLoading();
		verify(mockJsClient).unsubscribe(anyString(), any(AsyncCallback.class));
		assertNull(widget.getCurrentSubscription());
		verify(mockView).showFollowButton();
		verify(mockUnsubscribeCallback).invoke();
	}

	@Test
	public void testUnsubscribeFailure() {
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).unsubscribe(anyString(), any(AsyncCallback.class));
		widget.configure(mockSubscription);
		widget.onUnsubscribe();
		verify(mockSynAlert).clear();
		verify(mockJsClient).unsubscribe(anyString(), any(AsyncCallback.class));
		assertEquals(mockSubscription, widget.getCurrentSubscription());
		verify(mockSynAlert).handleException(ex);
		verify(mockView).hideLoading();
		verify(mockUnsubscribeCallback, never()).invoke();
	}

	@Test
	public void testSetButtonSize() {
		widget.setButtonSize(ButtonSize.LARGE);
		verify(mockView).setButtonSize(ButtonSize.LARGE);
	}
}
