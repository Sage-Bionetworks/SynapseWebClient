package org.sagebionetworks.web.unitclient.widget.footer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.footer.Footer.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.client.widget.footer.VersionState;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class FooterTest {
	Footer footer;
	@Mock
	FooterView mockView;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	UserProfile mockUserProfile;
	public static final String OWNER_ID = "282711";
	public static final String FIRST_NAME = "Bob";
	public static final String LAST_NAME = "Vance";
	public static final String USERNAME = "bvance";
	public static final String EMAIL = "bob@vancerefrigeration.com";
	
	@Before
	public void setup(){		
		footer = new Footer(mockView, mockGlobalAppState, mockAuthController);
		VersionState versionState = new VersionState("v,v", true);
		AsyncMockStubber.callSuccessWith(versionState).when(mockGlobalAppState).checkVersionCompatibility(any(AsyncCallback.class));
		verify(mockView).setPresenter(footer);
		when(mockUserProfile.getEmails()).thenReturn(Collections.singletonList(EMAIL));
		when(mockUserProfile.getFirstName()).thenReturn(FIRST_NAME);
		when(mockUserProfile.getLastName()).thenReturn(LAST_NAME);
		when(mockUserProfile.getUserName()).thenReturn(USERNAME);
		when(mockUserProfile.getOwnerId()).thenReturn(OWNER_ID);
	}

	@Test
	public void testConstruction(){
		verify(mockGlobalAppState).checkVersionCompatibility(any(AsyncCallback.class));
		verify(mockView).refresh();
	}

	@Test
	public void testConstructionNullVersion(){
		VersionState versionState = new VersionState(null, false);
		AsyncMockStubber.callSuccessWith(versionState).when(mockGlobalAppState).checkVersionCompatibility(any(AsyncCallback.class));
		footer = new Footer(mockView, mockGlobalAppState, mockAuthController);
		verify(mockView).setVersion(Footer.UNKNOWN, Footer.UNKNOWN);
	}

	@Test
	public void testAsWidget(){
		footer.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testOnReportAbuse() {
		when(mockAuthController.getCurrentUserProfile()).thenReturn(mockUserProfile);
		footer.onReportAbuseClicked();
		verify(mockView).showJiraIssueCollector(OWNER_ID, DisplayUtils.getDisplayName(FIRST_NAME, LAST_NAME, USERNAME), EMAIL);
	}
	
	@Test
	public void testOnReportAbuseAnonymous() {
		//current user profile is null
		footer.onReportAbuseClicked();
		verify(mockView).showJiraIssueCollector(ANONYMOUS, ANONYMOUS, ANONYMOUS);
	}
}
