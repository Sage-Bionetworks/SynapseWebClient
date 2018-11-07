package org.sagebionetworks.web.unitclient.widget.entity.controller;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertImpl;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertView;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ConflictingUpdateException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Widget;

public class SynapseAlertImplTest {
	@Mock
	SynapseAlertView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	SynapseAlertImpl widget;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	JiraURLHelper mockJiraClient;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	LoginWidget mockLoginWidget;
	@Mock
	SynapseProperties mockSynapseProperties;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	// a new jira odyssey
	String newJiraKey = "SWC-2001";
	
	public static final String HOST_PAGE_URL="http://foobar";
	public static final String JIRA_ENDPOINT_URL="http://foo.bar.com/";
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		widget = new SynapseAlertImpl(mockView, mockGlobalApplicationState, mockAuthenticationController, mockGWT, mockPortalGinInjector, mockJsniUtils);
		UserProfile mockProfile = mock(UserProfile.class);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(mockProfile);
		
		AsyncMockStubber.callSuccessWith(newJiraKey).when(mockJiraClient).createIssueOnBackend(anyString(),  any(Throwable.class),  anyString(), any(AsyncCallback.class));
		
		when(mockGWT.getHostPageBaseURL()).thenReturn(HOST_PAGE_URL);
		
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockGlobalApplicationState.getJiraURLHelper()).thenReturn(mockJiraClient);
		when(mockSynapseProperties.getSynapseProperty(WebConstants.CONFLUENCE_ENDPOINT)).thenReturn(JIRA_ENDPOINT_URL);
		
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		verify(mockView).setPresenter(widget);
		when(mockPortalGinInjector.getLoginWidget()).thenReturn(mockLoginWidget);
		when(mockPortalGinInjector.getSynapseProperties()).thenReturn(mockSynapseProperties);
	}
	
	@Test
	public void testHandleServiceExceptionReadOnly() {
		widget.handleException(new ReadOnlyModeException());
		verify(mockView).clearState();
		verify(mockPlaceChanger).goTo(isA(Down.class));
	}
	
	@Test
	public void testHandleServiceExceptionDown() {
		widget.handleException(new SynapseDownException());
		verify(mockView).clearState();
		verify(mockPlaceChanger).goTo(isA(Down.class));
	}
	
	@Test
	public void testHandleStatusCodeExceptionWithMessage() {
		String statusText = "error described here";
		widget.handleException(new StatusCodeException(1, statusText, ""));
		verify(mockView).clearState();
		verify(mockView).showError(statusText);
		verify(mockView, never()).setRetryButtonVisible(true);
	}

	@Test
	public void testHandleStatusCodeExceptionWithStatusCodeOnly() {
		int statusCode = 418; //I'm a teapot status code
		widget.handleException(new StatusCodeException(statusCode, ""));
		verify(mockView).clearState();
		verify(mockView).showError(SynapseAlertImpl.SERVER_STATUS_CODE_MESSAGE + statusCode);
		verify(mockView, never()).setRetryButtonVisible(true);
	}
	
	public void testHandleStatusCodeExceptionZero() {
		widget.handleException(new StatusCodeException(0, ""));
		verify(mockView).clearState();
		verify(mockView).showError(DisplayConstants.NETWORK_ERROR);
		verify(mockView).setRetryButtonVisible(true);
	}
	
	@Test
	public void testHandleServiceExceptionForbiddenLoggedIn() {
		widget.handleException(new ForbiddenException());
		verify(mockView).clearState();
		ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
		verify(mockView).showError(c.capture());
		assertTrue(c.getValue().startsWith(DisplayConstants.ERROR_FAILURE_PRIVLEDGES));
	}
	
	@Test
	public void testHandleServiceExceptionForbiddenNotLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.handleException(new ForbiddenException());
		verify(mockView, atLeastOnce()).clearState();
		verify(mockPortalGinInjector).getLoginWidget();
		verify(mockView).setLoginWidget(any(Widget.class));
		verify(mockView).showLogin();
	}
	
	@Test
	public void testHandleServiceExceptionNotFound() {
		widget.handleException(new NotFoundException());
		verify(mockView).clearState();
		ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
		verify(mockView).showError(c.capture());
		assertTrue(c.getValue().startsWith(DisplayConstants.ERROR_NOT_FOUND));
	}
	
	@Test
	public void testHandleServiceUnknownErrorExceptionLoggedIn() {
		String errorMessage= "unknown";
		widget.handleException(new UnknownErrorException(errorMessage));
		verify(mockView).clearState();
		verify(mockView).showError(errorMessage);
		verify(mockView).showJiraDialog(errorMessage);
	}
	
	@Test
	public void testHandleServiceUnknownErrorExceptionNotLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		String errorMessage= "unknown";
		widget.handleException(new UnknownErrorException(errorMessage));
		verify(mockView).clearState();
		verify(mockView).showError(errorMessage);
		verify(mockView, never()).showJiraDialog(errorMessage);
		
	}
	
	@Test
	public void testOnCreateJiraIssue() {
		widget.handleException(new UnknownErrorException());
		String userReport = "clicked a button";
		widget.onCreateJiraIssue(userReport);
		verify(mockView).hideJiraDialog();
		
		// tell user that the jira has been created, and include a link to the new issue!
		verify(mockView).showJiraIssueOpen(newJiraKey, JIRA_ENDPOINT_URL + SynapseAlertImpl.BROWSE_PATH + newJiraKey);
	}
	
	@Test
	public void testOnCreateJiraIssueFailure() {
		widget.handleException(new UnknownErrorException());
		
		AsyncMockStubber.callFailureWith(new Exception("ex")).when(mockJiraClient).createIssueOnBackend(anyString(),  any(Throwable.class),  anyString(), any(AsyncCallback.class));
		String userReport = "clicked a button";
		widget.onCreateJiraIssue(userReport);
		verify(mockView).hideJiraDialog();
		verify(mockView, times(2)).showError(anyString());
	}
	
	@Test
	public void testHandleServiceUnrecognizedException() {
		String errorMessage = "unrecognized";
		widget.handleException(new IllegalArgumentException(errorMessage));
		verify(mockView).clearState();
		verify(mockView).showError(errorMessage);
	}
	
	@Test
	public void testHandleServiceUnrecognizedExceptionNullMessage() {
		String errorMessage = null;
		widget.handleException(new IllegalArgumentException(errorMessage));
		verify(mockView).clearState();
		verify(mockView).showError(DisplayConstants.ERROR_RESPONSE_UNAVAILABLE);
	}
	
	@Test
	public void testHandleServiceUnrecognizedExceptionEmptyMessage() {
		String errorMessage = "";
		widget.handleException(new IllegalArgumentException(errorMessage));
		verify(mockView).clearState();
		verify(mockView).showError(DisplayConstants.ERROR_RESPONSE_UNAVAILABLE);
	}
	
	@Test
	public void testHandleServiceUnrecognizedExceptionZeroMessage() {
		String errorMessage = "0";
		widget.handleException(new IllegalArgumentException(errorMessage));
		verify(mockView).clearState();
		verify(mockView).showError(DisplayConstants.ERROR_RESPONSE_UNAVAILABLE);
	}
	
	@Test
	public void testHandleServiceUnauthorizedExceptionMessage() {
		widget.handleException(new UnauthorizedException());
		verify(mockAuthenticationController).logoutUser();
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
	}
	
	@Test
	public void testHandleServiceConflictingUpdateExceptionMessage() {
		String errorMessage = "error";
		widget.handleException(new ConflictingUpdateException(errorMessage));
		verify(mockView, times(0)).showJiraDialog(anyString());
		verify(mockView).showError(DisplayConstants.ERROR_CONFLICTING_UPDATE + "\n" + errorMessage);
	}
	
	@Test
	public void testIsUserLoggedIn() {
		widget.isUserLoggedIn();
		verify(mockAuthenticationController).isLoggedIn();
	}
	
	@Test
	public void testShowMustLogin() {
		widget.showLogin();
		verify(mockView).clearState();
		verify(mockPortalGinInjector).getLoginWidget();
		verify(mockView).setLoginWidget(any(Widget.class));
		verify(mockView).showLogin();
	}
	
	@Test
	public void testShowError() {
		String errorMessage = "a handled error";
		widget.showError(errorMessage);
		verify(mockView).clearState();
		verify(mockView).showError(errorMessage);
	}
}
