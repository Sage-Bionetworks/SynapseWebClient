package org.sagebionetworks.web.unitclient.widget.footer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.client.widget.footer.VersionState;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FooterTest {
		
	Footer footer;
	FooterView mockView;
	GlobalApplicationState mockSynapseClient;
	@Mock
	AuthenticationController mockAuthController;

	@Before
	public void setup(){		
		MockitoAnnotations.initMocks(this);
		mockView = Mockito.mock(FooterView.class);
		mockSynapseClient = Mockito.mock(GlobalApplicationState.class);
		footer = new Footer(mockView, mockSynapseClient, mockAuthController);
		VersionState versionState = new VersionState("v,v", true);
		AsyncMockStubber.callSuccessWith(versionState).when(mockSynapseClient).checkVersionCompatibility(any(AsyncCallback.class));
		verify(mockView).setPresenter(footer);
	}

	@Test
	public void testConstruction(){
		verify(mockSynapseClient).checkVersionCompatibility(any(AsyncCallback.class));
		verify(mockView).refresh();
	}

	@Test
	public void testConstructionNullVersion(){
		VersionState versionState = new VersionState(null, false);
		AsyncMockStubber.callSuccessWith(versionState).when(mockSynapseClient).checkVersionCompatibility(any(AsyncCallback.class));
		footer = new Footer(mockView, mockSynapseClient, mockAuthController);
		verify(mockView).setVersion(Footer.UNKNOWN, Footer.UNKNOWN);
	}

	@Test
	public void testAsWidget(){
		footer.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testOnReportAbuse() {
		footer.onReportAbuseClicked();
		verify(mockView).showJiraIssueCollector(anyString(), anyString(), anyString());
	}
}
