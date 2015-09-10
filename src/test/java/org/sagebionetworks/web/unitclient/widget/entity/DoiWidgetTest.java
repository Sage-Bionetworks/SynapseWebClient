package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.DoiWidget;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class DoiWidgetTest {

	GlobalApplicationState mockGlobalApplicationState;
	DoiWidgetView mockView;
	String entityId = "syn123";
	String testDoiPrefix = "testDoiPrefix";
	DoiWidget doiWidget;
	Doi testDoi;
	StackConfigServiceAsync mockStackConfigService;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockView = mock(DoiWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockStackConfigService = mock(StackConfigServiceAsync.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		testDoi = new Doi();
		testDoi.setId(entityId);
		testDoi.setDoiStatus(DoiStatus.CREATED);
		AsyncMockStubber.callSuccessWith(testDoiPrefix).when(mockStackConfigService).getDoiPrefix(any(AsyncCallback.class));
		doiWidget = new DoiWidget(mockView, mockGlobalApplicationState, mockStackConfigService, mockAuthenticationController, mockSynapseClient);
	}
	
	@Test
	public void testConfigureReadyStatus() throws Exception {
		testDoi.setDoiStatus(DoiStatus.READY);
		doiWidget.configure(testDoi);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView).showDoiCreated(doiWidget.getDoi(testDoiPrefix, false));
	}

	@Test
	public void testConfigureErrorStatus() throws Exception {
		testDoi.setDoiStatus(DoiStatus.ERROR);
		doiWidget.configure(testDoi);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView).showDoiError();
	}
	
	@Test
	public void testConfigureNotFound() throws Exception {
		doiWidget.configure(null);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
	}
	
	@Test
	public void testGetDoiPrefix() throws Exception {
		doiWidget.getDoiPrefix(mock(AsyncCallback.class));
		verify(mockStackConfigService).getDoiPrefix(any(AsyncCallback.class));
	}
	
	@Test
	public void testGetDoiLink() throws Exception {
		String prefix = "10.5072/FK2.";
		Long version = 42l;
		testDoi.setObjectVersion(version);
		doiWidget.configure(testDoi);
		String link = doiWidget.getDoi(prefix, true);
		assertTrue(link.contains(entityId));
		assertTrue(link.contains(version.toString()));
		assertTrue(link.contains(prefix));
	}

	@Test
	public void testGetDoiLinkNoPrefix() throws Exception {
		String prefix = "";
		Long version = 42l;
		testDoi.setObjectVersion(version);
		doiWidget.configure(testDoi);
		String link = doiWidget.getDoi(prefix, true);
		assertTrue(link.length() == 0);
		link = doiWidget.getDoi(null, true);
		assertTrue(link.length() == 0);
	}
	
}
