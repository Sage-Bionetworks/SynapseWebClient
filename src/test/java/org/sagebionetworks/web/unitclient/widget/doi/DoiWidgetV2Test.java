package org.sagebionetworks.web.unitclient.widget.doi;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2;
import org.sagebionetworks.web.client.widget.doi.DoiWidgetV2View;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

@RunWith(MockitoJUnitRunner.class)
public class DoiWidgetV2Test {


	private DoiWidgetV2 doiWidget;
	private Doi testDoi;
	private static final String uri = "10.5072/test-uri";

	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	DoiWidgetV2View mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynAlert;

	@Before
	public void before() {
		testDoi = new Doi();
		testDoi.setDoiUri(uri);
		doiWidget = new DoiWidgetV2(mockView, mockGlobalApplicationState, mockSynapseClient, mockSynAlert);
	}
	
	@Test
	public void testConfigure() throws Exception {
		doiWidget.configure(testDoi);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView).showDoiCreated(uri);
	}

	@Test
	public void testConfigureNullDoi() throws Exception {
		doiWidget.configure(null);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView, never()).showDoiCreated(uri);
	}

	@Test
	public void testConfigureNullUri() throws Exception {
		testDoi.setDoiUri(null);
		doiWidget.configure(testDoi);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView, never()).showDoiCreated(uri);
	}
	
}
