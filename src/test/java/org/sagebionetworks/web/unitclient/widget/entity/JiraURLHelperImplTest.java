package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.JiraClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.JiraGovernanceConstants;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelperImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * We just tests that the URLs are well formed
 * 
 * @author brucehoff
 *
 */
public class JiraURLHelperImplTest {
	
	private JiraURLHelperImpl helper = null;
	private JiraClientAsync mockJiraClient;
	private GWTWrapper mockGWTWrapper;
	private AuthenticationController mockAuthController;
	private UserProfile testProfile;
	private static final String TEST_URL= "http://url.where.error.occurred.org/";
	@Before
	public void before() throws Exception {
		mockJiraClient = mock(JiraClientAsync.class);
		AsyncMockStubber.callSuccessWith(null).when(mockJiraClient).createJiraIssue(anyString(),  anyString(),  anyString(),anyMap(), any(AsyncCallback.class));
		mockGWTWrapper = mock(GWTWrapper.class);
		when(mockGWTWrapper.getCurrentURL()).thenReturn(TEST_URL);
		mockAuthController = mock(AuthenticationController.class);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		testProfile = new UserProfile();
		testProfile.setUserName("007");
		testProfile.setEmails(Collections.singletonList("test@test.com"));
		when(mockAuthController.getCurrentUserProfile()).thenReturn(testProfile);
		
		helper = new JiraURLHelperImpl(new JiraGovernanceConstants() {
			@Override
			public int jiraProjectId() {
				return 0;
			}
			@Override
			public String confluenceEndpoint() {
				return "http://foo.bar.bas";
			}
			@Override
			public int flagIssueType() {
				return 0;
			}
			@Override
			public int accessRestrictionType() {
				return 0;
			}
			@Override
			public int accessRequestIssueType() {
				return 0;
			}
			@Override
			public String defaultIssueReporter() {
				return "me";
			}
			@Override
			public String issueFieldPrincipalId() {
				return "100";
			}
			@Override
			public String issueFieldUserDisplayName() {
				return "400";
			}
			@Override
			public String issueFieldDataObjectId() {
				return "800";
			}
			@Override
			public String issueFieldUserEmail() {
				return "900";
			}
			@Override
			public String issueFieldAccessRequirementObjectId() {
				return "300";
			}
			@Override
			public int majorPriority() {
				return 10;
			}
		}, mockJiraClient, mockGWTWrapper, mockAuthController);
	}

	@Test
	public void testCreateFlagIssue() throws MalformedURLException {
		String urlString = helper.createFlagIssue("me@place.domain", "me me", "syn123");
		URL url = new URL(urlString); // this will check that the url is well formed
	}

	@Test
	public void testCreateAccessRestrictionIssue() throws MalformedURLException {
		String urlString = helper.createAccessRestrictionIssue("me@place.domain", "me me", "syn123");
		URL url = new URL(urlString); // this will check that the url is well formed
	}

	@Test
	public void testCreateRequestAccessIssue() throws MalformedURLException {
		String urlString = helper.createRequestAccessIssue("101", "me me", "me@place.domain", 
				"syn123", "432");
		URL url = new URL(urlString); // this will check that the url is well formed
	}
	
	@Test
	public void testCreateReportAbuseIssueURL() throws MalformedURLException {
		String urlString = helper.createReportAbuseIssueURL();
		URL url = new URL(urlString); // this will check that the url is well formed
	}
	@Test
	public void testAnonymousCreateReportAbuseIssueURL() throws MalformedURLException {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		String urlString = helper.createReportAbuseIssueURL();
		URL url = new URL(urlString); // this will check that the url is well formed
	}
	
	@Test
	public void testCreateIssueOnBackend() {
		String userSteps = "user steps to repro (optional)";
		String rootCause = "an error in the stack trace";
		Throwable ex = new Exception(rootCause); 
		String friendlyError = "this is the test error message";
		AsyncCallback<String> mockCallback = mock(AsyncCallback.class);
		helper.createIssueOnBackend(userSteps, ex, friendlyError, mockCallback);
		
		ArgumentCaptor<String> summaryCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
		
		//called service
		verify(mockJiraClient).createJiraIssue(summaryCaptor.capture(), descriptionCaptor.capture(), anyString(), anyMap(), any(AsyncCallback.class));
		
		//the summary of the issue should be our friendly error
		assertEquals(friendlyError, summaryCaptor.getValue());
		
		//the description should (hopefully) contain all of the information we need to figure out the problem
		String issueDescription = descriptionCaptor.getValue();
		assertTrue(issueDescription.contains(testProfile.getUserName()));
		assertTrue(issueDescription.contains(userSteps));
		assertTrue("issue description was: " + issueDescription, issueDescription.contains(rootCause));
		//exception should point to this class
		assertTrue(issueDescription.contains(JiraURLHelperImplTest.class.getName()));
		//and method
		assertTrue(issueDescription.contains("testCreateIssueOnBackend"));
	}
}
