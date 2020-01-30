package org.sagebionetworks.web.unitclient.widget.subscription;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.web.client.widget.subscription.SubscribeButtonWidget;
import org.sagebionetworks.web.client.widget.subscription.TopicRowWidget;
import org.sagebionetworks.web.client.widget.subscription.TopicRowWidgetView;
import org.sagebionetworks.web.client.widget.subscription.TopicWidget;
import com.google.gwt.user.client.ui.Widget;


public class TopicRowWidgetTest {
	TopicRowWidget widget;

	@Mock
	TopicRowWidgetView mockView;
	@Mock
	TopicWidget mockTopicWidget;
	@Mock
	SubscribeButtonWidget mockSubscribeButtonWidget;
	@Mock
	Subscription mockSubscription;

	private static final String TEST_OBJECT_ID = "3";
	private static final String TEST_SUBSCRIPTION_ID = "8837";
	private static final SubscriptionObjectType TEST_TYPE = SubscriptionObjectType.THREAD;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new TopicRowWidget(mockView, mockTopicWidget, mockSubscribeButtonWidget);

		when(mockSubscription.getSubscriptionId()).thenReturn(TEST_SUBSCRIPTION_ID);
		when(mockSubscription.getObjectId()).thenReturn(TEST_OBJECT_ID);
		when(mockSubscription.getObjectType()).thenReturn(TEST_TYPE);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setSubscribeButtonWidget(any(Widget.class));
		verify(mockView).setTopicWidget(any(Widget.class));
		verify(mockSubscribeButtonWidget).setButtonSize(ButtonSize.EXTRA_SMALL);
	}

	@Test
	public void testConfigure() {
		widget.configure(mockSubscription);
		verify(mockSubscribeButtonWidget).configure(mockSubscription);
		verify(mockTopicWidget).configure(TEST_TYPE, TEST_OBJECT_ID);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
