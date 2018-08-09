package org.sagebionetworks.web.unitclient.widget.clienthelp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelpView;

public class FileClientsHelpTest {
	FileClientsHelp widget;
	@Mock
	FileClientsHelpView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new FileClientsHelp(mockView, mockSynapseClient);
	}

	@Test
	public void testConfigure() {
		fail("Not yet implemented");
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
