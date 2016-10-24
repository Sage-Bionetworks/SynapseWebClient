package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

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
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.CancelControlWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.CallbackMockStubber;

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
	@Mock
	GWTWrapper mockGwt;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	CancelControlWidget widget;
	
	@Mock
	Callback mockRefreshCallback;
	public static final String CANCEL_JSON = "{\"canCancel\":false,\"cancelRequested\":false,\"userId\":\"1131050\",\"submissionId\":\"7115005\"}";
	
	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		widget = new CancelControlWidget(mockView, mockChallengeClient, mockAuthController, mockSynAlert, adapterFactory, mockGwt);
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).requestToCancelSubmission(anyString(), any(AsyncCallback.class));
		CallbackMockStubber.invokeCallback().when(mockGwt).scheduleExecution(any(Callback.class), anyInt());
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
	public void testConfigure() {
		
	}
	
	@Test
	public void testOnClick() {
		
	}
	
	@Test
	public void testRequestToCancelSubmission() {
		widget.configure(CANCEL_JSON, mockRefreshCallback);
		widget.requestToCancelSubmission();
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setLoading(true);
		inOrder.verify(mockView).setLoading(false);
		verify(mockRefreshCallback).invoke();
	}
}











