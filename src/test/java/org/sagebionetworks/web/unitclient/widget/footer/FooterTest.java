package org.sagebionetworks.web.unitclient.widget.footer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FooterTest {
		
	Footer footer;
	FooterView mockView;
	SynapseClientAsync mockSynapseClient;
	
	@Before
	public void setup(){		
		mockView = Mockito.mock(FooterView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		AsyncMockStubber.callSuccessWith("v,v").when(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		footer = new Footer(mockView, mockSynapseClient);
		
		verify(mockView).setPresenter(footer);
	}
	
	@Test
	public void testAsWidget(){
		footer.asWidget();
	}
}
