package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.DoiWidget;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetView;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class DoiWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	DoiWidgetView mockView;
	String entityId = "syn123";
	String testDoiPrefix = "testDoiPrefix";
	DoiWidget doiWidget;
	Doi testDoi;
	StackConfigServiceAsync mockStackConfigService;
	AuthenticationController mockAuthenticationController;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(DoiWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockStackConfigService = mock(StackConfigServiceAsync.class);
		testDoi = new Doi();
		testDoi.setDoiStatus(DoiStatus.CREATED);
		AsyncMockStubber.callSuccessWith(testDoi).when(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(testDoiPrefix).when(mockStackConfigService).getDoiPrefix(any(AsyncCallback.class));
		doiWidget = new DoiWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockStackConfigService, mockAuthenticationController);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureReadyStatus() throws Exception {
		doiWidget.configure(entityId, true, null);
		verify(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showDoiCreated(doiWidget.getDoiHtml(testDoiPrefix, false));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureReadyStatusNotEditable() throws Exception {
		doiWidget.configure(entityId, false, null);
		verify(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showDoiCreated(doiWidget.getDoiHtml(testDoiPrefix, false));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureErrorStatus() throws Exception {
		testDoi.setDoiStatus(DoiStatus.ERROR);
		doiWidget.configure(entityId, true, null);
		verify(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showDoiError();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureNotFound() throws Exception {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		doiWidget.configure(entityId, true, null);
		verify(mockView).showCreateDoi();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureNotFoundNonEditable() throws Exception {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		doiWidget.configure(entityId, false, null);
		verify(mockView, Mockito.times(0)).showCreateDoi();
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureOtherError() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		doiWidget.configure(entityId, true, null);
		verify(mockView).showErrorMessage(anyString());
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateDoi() throws Exception {
		doiWidget.createDoi();
		verify(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		verify(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
	}
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateDoiFail() throws Exception {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		doiWidget.createDoi();
		verify(mockSynapseClient).createDoi(anyString(), anyLong(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetDoiPrefix() throws Exception {
		doiWidget.getDoiPrefix(mock(AsyncCallback.class));
		verify(mockStackConfigService).getDoiPrefix(any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetDoiLink() throws Exception {
		String prefix = "10.5072/FK2.";
		Long version = 42l;
		doiWidget.configure(entityId, true, version);
		String link = doiWidget.getDoiHtml(prefix, true);
		assertTrue(link.contains(entityId));
		assertTrue(link.contains(version.toString()));
		assertTrue(link.contains(prefix));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetDoiLinkNoPrefix() throws Exception {
		String prefix = "";
		Long version = 42l;
		doiWidget.configure(entityId, true, version);
		String link = doiWidget.getDoiHtml(prefix, true);
		assertTrue(link.length() == 0);
		link = doiWidget.getDoiHtml(null, true);
		assertTrue(link.length() == 0);
	}
	
}
