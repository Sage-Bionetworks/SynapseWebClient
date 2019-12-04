package org.sagebionetworks.web.unitclient.widget.footer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.WebConstants.ANONYMOUS;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_COLLECTOR_URL;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_DESCRIPTION_PART_1;
import static org.sagebionetworks.web.shared.WebConstants.FLAG_ISSUE_PRIORITY;
import static org.sagebionetworks.web.shared.WebConstants.REVIEW_ABUSIVE_CONTENT_REQUEST_COMPONENT_ID;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.client.widget.footer.VersionState;
import org.sagebionetworks.web.shared.WebConstants;
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
	@Mock
	GWTWrapper mockGwt;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	public static final String OWNER_ID = "282711";
	public static final String FIRST_NAME = "Bob";
	public static final String LAST_NAME = "Vance";
	public static final String USERNAME = "bvance";
	public static final String EMAIL = "bob@vancerefrigeration.com";
	public static final String CURRENT_URL = "https://www.synapse.org/flag-this-data";

	@Before
	public void setup() {
		footer = new Footer(mockView, mockGlobalAppState, mockAuthController, mockGwt, mockJsniUtils);
		VersionState versionState = new VersionState("v,v", true);
		AsyncMockStubber.callSuccessWith(versionState).when(mockGlobalAppState).checkVersionCompatibility(any(AsyncCallback.class));
		verify(mockView).setPresenter(footer);
		when(mockUserProfile.getEmails()).thenReturn(Collections.singletonList(EMAIL));
		when(mockUserProfile.getFirstName()).thenReturn(FIRST_NAME);
		when(mockUserProfile.getLastName()).thenReturn(LAST_NAME);
		when(mockUserProfile.getUserName()).thenReturn(USERNAME);
		when(mockUserProfile.getOwnerId()).thenReturn(OWNER_ID);
		when(mockGwt.getCurrentURL()).thenReturn(CURRENT_URL);
	}

	@Test
	public void testConstruction() {
		verify(mockGlobalAppState).checkVersionCompatibility(any(AsyncCallback.class));
		verify(mockView).refresh();
	}

	@Test
	public void testConstructionNullVersion() {
		VersionState versionState = new VersionState(null, false);
		AsyncMockStubber.callSuccessWith(versionState).when(mockGlobalAppState).checkVersionCompatibility(any(AsyncCallback.class));
		footer = new Footer(mockView, mockGlobalAppState, mockAuthController, mockGwt, mockJsniUtils);
		verify(mockView).setVersion(Footer.UNKNOWN, Footer.UNKNOWN);
	}

	@Test
	public void testAsWidget() {
		footer.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testOnReportAbuse() {
		when(mockAuthController.getCurrentUserProfile()).thenReturn(mockUserProfile);
		footer.onReportAbuseClicked();
		verify(mockJsniUtils).showJiraIssueCollector("", // summary
				FLAG_ISSUE_DESCRIPTION_PART_1 + CURRENT_URL + WebConstants.FLAG_ISSUE_DESCRIPTION_PART_2, // description
				FLAG_ISSUE_COLLECTOR_URL, OWNER_ID, DisplayUtils.getDisplayName(FIRST_NAME, LAST_NAME, USERNAME), EMAIL, null, // Synapse data object ID
				REVIEW_ABUSIVE_CONTENT_REQUEST_COMPONENT_ID, null, // Access requirement ID
				FLAG_ISSUE_PRIORITY);
	}

	@Test
	public void testOnReportAbuseAnonymous() {
		// current user profile is null
		footer.onReportAbuseClicked();
		verify(mockJsniUtils).showJiraIssueCollector("", // summary
				FLAG_ISSUE_DESCRIPTION_PART_1 + CURRENT_URL + WebConstants.FLAG_ISSUE_DESCRIPTION_PART_2, // description
				FLAG_ISSUE_COLLECTOR_URL, ANONYMOUS, ANONYMOUS, ANONYMOUS, null, // Synapse data object ID
				REVIEW_ABUSIVE_CONTENT_REQUEST_COMPONENT_ID, null, // Access requirement ID
				FLAG_ISSUE_PRIORITY);
	}
}
