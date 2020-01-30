package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.CancelControlWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class CancelControlWidgetTest {

	@Mock
	SingleButtonView mockView;
	@Mock
	ChallengeClientAsync mockChallengeClient;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	SynapseAlert mockSynAlert;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();

	CancelControlWidget widget;

	public static final String SUBMITTER_ID = "1131050";
	public static final String CAN_CANCEL_JSON = "{\"canCancel\":true,\"cancelRequested\":false,\"userId\":\"1131050\",\"submissionId\":\"7115005\"}";
	public static final String CANNOT_CANCEL_JSON = "{\"canCancel\":false,\"cancelRequested\":false,\"userId\":\"1131050\",\"submissionId\":\"7115005\"}";
	public static final String CAN_CANCEL_CANCEL_REQUESTED_JSON = "{\"canCancel\":true,\"cancelRequested\":true,\"userId\":\"1131050\",\"submissionId\":\"7115005\"}";

	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(SUBMITTER_ID);
		widget = new CancelControlWidget(mockView, mockChallengeClient, mockAuthController, mockSynAlert, adapterFactory);
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).requestToCancelSubmission(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConstructor() {
		verify(mockView).setButtonText(DisplayConstants.BUTTON_CANCEL);
		verify(mockView).setButtonType(ButtonType.DANGER);
		verify(mockView).setPresenter(widget);
		verify(mockView).addWidget(any(Widget.class));
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigureAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		widget.configure(CAN_CANCEL_JSON);
		verify(mockSynAlert).clear();
		verify(mockView).setButtonVisible(false);
		verify(mockView, never()).setButtonVisible(true);
	}

	@Test
	public void testConfigureDifferentUser() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		// different user than the submitter
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn("9876543");
		widget.configure(CAN_CANCEL_JSON);
		verify(mockSynAlert).clear();
		verify(mockView).setButtonVisible(false);
		verify(mockView, never()).setButtonVisible(true);
	}

	@Test
	public void testConfigureSubmitter() {
		widget.configure(CAN_CANCEL_JSON);
		verify(mockSynAlert).clear();
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setButtonVisible(false);
		inOrder.verify(mockView).setButtonVisible(true);

		verify(mockView).setLoading(false);
		verify(mockView, never()).setLoading(true);
	}

	@Test
	public void testConfigureSubmitterCannotCancel() {
		widget.configure(CANNOT_CANCEL_JSON);
		verify(mockSynAlert).clear();
		verify(mockView).setButtonVisible(false);
		verify(mockView, never()).setButtonVisible(true);
	}

	@Test
	public void testConfigureSubmitterCancelRequested() {
		widget.configure(CAN_CANCEL_CANCEL_REQUESTED_JSON);
		verify(mockSynAlert).clear();
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setButtonVisible(false);
		inOrder.verify(mockView).setButtonVisible(true);
		inOrder = inOrder(mockView);
		inOrder.verify(mockView).setLoading(false);
		inOrder.verify(mockView).setLoading(true);
	}

	@Test
	public void testOnClick() {
		widget.onClick();
		verify(mockSynAlert).clear();
		verify(mockView).showConfirmDialog(eq(CancelControlWidget.CONFIRM_CANCEL), any(Callback.class));
	}

	@Test
	public void testConfigureAndRequestToCancelSubmission() {
		widget.configure(CAN_CANCEL_JSON);
		widget.requestToCancelSubmission();
		verify(mockView, times(2)).setLoading(true);
	}
}


