package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.WebConstants.ISSUE_PRIORITY_MINOR;
import static org.sagebionetworks.web.shared.WebConstants.SWC_ISSUE_COLLECTOR_URL;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertImpl;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertView;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ConflictingUpdateException;
import org.sagebionetworks.web.shared.exceptions.DeprecatedServiceException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import com.amazonaws.services.greengrass.model.BadRequestException;
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
	JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();

	public static final String HOST_PAGE_URL = "http://foobar";
	public static final String JIRA_ENDPOINT_URL = "http://foo.bar.com/";

	public static final String USER_EMAIL = "email@email.com";
	public static final String USER_ID = "123";
	public static final String USERNAME = "Clue";
	public static final String FIRST_NAME = "Professor";
	public static final String LAST_NAME = "Plum";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new SynapseAlertImpl(mockView, mockGlobalApplicationState, mockAuthenticationController, mockGWT, mockPortalGinInjector, mockJsniUtils, jsonObjectAdapter);
		UserProfile mockProfile = mock(UserProfile.class);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(mockProfile);
		when(mockGWT.getHostPageBaseURL()).thenReturn(HOST_PAGE_URL);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockPortalGinInjector.getLoginWidget()).thenReturn(mockLoginWidget);
		when(mockPortalGinInjector.getSynapseProperties()).thenReturn(mockSynapseProperties);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(mockProfile);
		when(mockProfile.getOwnerId()).thenReturn(USER_ID);
		when(mockProfile.getEmails()).thenReturn(Collections.singletonList(USER_EMAIL));
		when(mockProfile.getFirstName()).thenReturn(FIRST_NAME);
		when(mockProfile.getLastName()).thenReturn(LAST_NAME);
		when(mockProfile.getUserName()).thenReturn(USERNAME);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
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
		int statusCode = 418; // I'm a teapot status code
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
	public void testHandleServiceExceptionDeprecatedService() {
		widget.handleException(new DeprecatedServiceException());
		verify(mockView).clearState();
		ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
		verify(mockView).showError(c.capture());
		assertTrue(c.getValue().startsWith(DisplayConstants.ERROR_DEPRECATED_SERVICE));
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
		String errorMessage = "unknown";
		widget.handleException(new UnknownErrorException(errorMessage));
		verify(mockView).clearState();
		verify(mockView).showError(errorMessage);

		verify(mockJsniUtils).showJiraIssueCollector(eq(""), anyString(), eq(SWC_ISSUE_COLLECTOR_URL), eq(USER_ID), eq(DisplayUtils.getDisplayName(FIRST_NAME, LAST_NAME, USERNAME)), eq(WebConstants.ANONYMOUS), // not included
				eq(""), eq(""), eq(""), eq(ISSUE_PRIORITY_MINOR));
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
		verify(mockView).showError(DisplayConstants.ERROR_CONFLICTING_UPDATE + "\n" + errorMessage);
	}

	@Test
	public void testHandleBadRequestExceptionWithJsonReason() {
		String reason = "This password is known to be a commonly used password. Please choose another password!";
		String json = "{\"reason\":\"" + reason + "\"}";

		widget.handleException(new BadRequestException(json));

		verify(mockView).showError(reason);
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
