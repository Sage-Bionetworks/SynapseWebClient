package org.sagebionetworks.web.unitclient.widget.footer;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
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
	JiraURLHelper mockJiraHelper;
	public static final String REPORT_ABUSE_URL = "http://jira.com/summary=harassed";
	@Before
	public void setup(){		
		MockitoAnnotations.initMocks(this);
		mockView = Mockito.mock(FooterView.class);
		mockSynapseClient = Mockito.mock(GlobalApplicationState.class);
		footer = new Footer(mockView, mockSynapseClient, mockJiraHelper);
		VersionState versionState = new VersionState("v,v", true);
		AsyncMockStubber.callSuccessWith(versionState).when(mockSynapseClient).checkVersionCompatibility(any(AsyncCallback.class));
		verify(mockView).setPresenter(footer);
		when(mockJiraHelper.createReportAbuseIssueURL()).thenReturn(REPORT_ABUSE_URL);
	}
	
	@Test
	public void testAsWidget(){
		footer.asWidget();
		verify(mockSynapseClient).checkVersionCompatibility(any(AsyncCallback.class));
	}
	
	@Test
	public void testAsWidgetNullVersion(){
		VersionState versionState = new VersionState(null, false);
		AsyncMockStubber.callSuccessWith(versionState).when(mockSynapseClient).checkVersionCompatibility(any(AsyncCallback.class));
		footer.asWidget();
		verify(mockView).setVersion(Footer.UNKNOWN, Footer.UNKNOWN);
	}
	
	@Test
	public void testOnReportAbuse() {
		footer.onReportAbuseClicked();
		verify(mockView).open(REPORT_ABUSE_URL);
	}
}
