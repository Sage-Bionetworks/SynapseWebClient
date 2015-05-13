package org.sagebionetworks.web.unitclient.widget.entity.controller;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.JiraClientAsync;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Down;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.controller.AccessRequirementController;
import org.sagebionetworks.web.client.widget.entity.controller.CertifiedUserController;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightControllerImpl;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertImpl;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertView;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SynapseAlertImplTest {

	SynapseAlertView mockView;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	SynapseAlertImpl widget;
	PlaceChanger mockPlaceChanger;
	JiraURLHelper mockJiraClient;
	@Before
	public void before(){
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockJiraClient = mock(JiraURLHelper.class);
		mockView = mock(SynapseAlertView.class);
		widget = new SynapseAlertImpl(mockView, mockGlobalApplicationState, mockAuthenticationController, mockSynapseJSNIUtils);
		
		AsyncMockStubber.callSuccessWith(null).when(mockJiraClient).createIssueOnBackend(anyString(),  any(Throwable.class),  anyString(), any(AsyncCallback.class));
		
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockGlobalApplicationState.getJiraURLHelper()).thenReturn(mockJiraClient);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testHandleServiceExceptionReadOnly() {
		widget.handleException(new ReadOnlyModeException());
		verify(mockView, times(2)).clearState();
		verify(mockView).showError(eq(DisplayConstants.SYNAPSE_IN_READ_ONLY_MODE));
	}
	
	@Test
	public void testHandleServiceExceptionDown() {
		widget.handleException(new SynapseDownException());
		verify(mockView, times(2)).clearState();
		verify(mockPlaceChanger).goTo(any(Down.class));
	}
	
	@Test
	public void testHandleServiceExceptionForbiddenLoggedIn() {
		widget.handleException(new ForbiddenException());
		verify(mockView, times(2)).clearState();
		ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
		verify(mockView).showError(c.capture());
		assertTrue(c.getValue().startsWith(DisplayConstants.ERROR_FAILURE_PRIVLEDGES));
	}
	
	@Test
	public void testHandleServiceExceptionForbiddenNotLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.handleException(new ForbiddenException());
		verify(mockView, times(2)).clearState();
		verify(mockView).showLoginAlert();
	}
	
	@Test
	public void testHandleServiceExceptionBadRequest() {
		String errorMessage = "testing bad request";
		widget.handleException(new BadRequestException(errorMessage));
		verify(mockView, times(2)).clearState();
		verify(mockView).showError(errorMessage);
	}

	@Test
	public void testHandleServiceExceptionNotFound() {
		widget.handleException(new NotFoundException());
		verify(mockView, times(2)).clearState();
		ArgumentCaptor<String> c = ArgumentCaptor.forClass(String.class);
		verify(mockView).showError(c.capture());
		assertTrue(c.getValue().startsWith(DisplayConstants.ERROR_NOT_FOUND));
	}
	
	@Test
	public void testHandleServiceUnknownErrorExceptionLoggedIn() {
		String errorMessage= "unknown";
		widget.handleException(new UnknownErrorException(errorMessage));
		verify(mockView, times(2)).clearState();
		assertFalse(DisplayUtils.handleServiceException(new IllegalArgumentException(), mockGlobalApplicationState, true, mockView));
		verify(mockView).showError(errorMessage);
		verify(mockView).showJiraDialog(errorMessage);
	}
	
	@Test
	public void testHandleServiceUnknownErrorExceptionNotLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		String errorMessage= "unknown";
		widget.handleException(new UnknownErrorException(errorMessage));
		verify(mockView, times(2)).clearState();
		verify(mockView).showError(errorMessage);
		verify(mockView, never()).showJiraDialog(errorMessage);
		
	}
	
	@Test
	public void testOnCreateJiraIssue() {
		widget.handleException(new UnknownErrorException());
		
		String userReport = "clicked a button";
		widget.onCreateJiraIssue(userReport);
		verify(mockView).hideJiraDialog();
		verify(mockView).showInfo(anyString(),  anyString());
	}
	
	@Test
	public void testOnCreateJiraIssueFailure() {
		widget.handleException(new UnknownErrorException());
		
		AsyncMockStubber.callFailureWith(new Exception("ex")).when(mockJiraClient).createIssueOnBackend(anyString(),  any(Throwable.class),  anyString(), any(AsyncCallback.class));
		String userReport = "clicked a button";
		widget.onCreateJiraIssue(userReport);
		verify(mockView, never()).hideJiraDialog();
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testHandleServiceUnrecognizedException() {
		String errorMessage = "unrecognized";
		widget.handleException(new IllegalArgumentException(errorMessage));
		verify(mockView, times(2)).clearState();
		verify(mockView).showError(errorMessage);
	}
	
	@Test
	public void testOnLoginClicked() {
		widget.onLoginClicked();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}

	@Test
	public void testIsUserLoggedIn() {
		widget.isUserLoggedIn();
		verify(mockAuthenticationController).isLoggedIn();
	}
	
	@Test
	public void testShowMustLogin() {
		widget.showMustLogin();
		verify(mockView, times(2)).clearState();
		verify(mockView).showLoginAlert();
	}
	
	@Test
	public void testShowError() {
		String errorMessage = "a handled error";
		widget.showError(errorMessage);
		verify(mockView, times(2)).clearState();
		verify(mockView).showError(errorMessage);
	}
}
