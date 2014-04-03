package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.download.CertificateView;
import org.sagebionetworks.web.client.widget.entity.download.CertificateWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CertificateWidgetTest {
	
	CertificateWidget widget;
	CertificateView mockView;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	
	@Before
	public void setup()  {
		mockView = mock(CertificateView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		widget = new CertificateWidget(mockView, mockGlobalApplicationState, mockAuthenticationController, mockSynapseClient);
		verify(mockView).setPresenter(widget);
	}	
	
	@Test
	public void testGetCertificationDate() {
		String testDateResponse = "date certified";
		AsyncMockStubber.callSuccessWith(testDateResponse).when(mockSynapseClient).getCertificationDate(anyString(), any(AsyncCallback.class));
		widget.getCertificationDate("userId");
		verify(mockSynapseClient).getCertificationDate(anyString(), any(AsyncCallback.class));
		ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
		//response passed directly to view
		verify(mockView).setCertificationDate(arg.capture());
		assertEquals(testDateResponse, arg.getValue());
	}
	
	@Test
	public void testGetQuestionaireFailure() {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).getCertificationDate(anyString(), any(AsyncCallback.class));
		widget.getCertificationDate("userId");
		verify(mockSynapseClient).getCertificationDate(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
}
