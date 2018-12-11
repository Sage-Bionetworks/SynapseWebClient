package org.sagebionetworks.web.unitclient.widget.entity.controller;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlertView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class StuAlertTest {

	@Mock
	StuAlertView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	SynapseAlert mockSynapseAlert;
	StuAlert widget;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	UserProfile mockProfile;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	public static final String HOST_PAGE_URL="http://foobar";
	public static final String USER_ID = "1977";
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		widget = new StuAlert(mockView, mockSynapseClient, mockSynapseAlert, mockGWT, mockAuthenticationController);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(mockProfile);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(USER_ID);
		when(mockProfile.getOwnerId()).thenReturn(USER_ID);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).sendMessageToEntityOwner(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		
		when(mockGWT.getHostPageBaseURL()).thenReturn(HOST_PAGE_URL);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testHandleServiceExceptionForbiddenLoggedIn() {
		widget.handleException(new ForbiddenException());
		verify(mockSynapseAlert).clear();
		verify(mockView).clearState();
		verify(mockView).show403();
		verify(mockView).setVisible(true);
	}
	
	@Test
	public void testHandleServiceExceptionForbiddenNotLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.handleException(new ForbiddenException());
		verify(mockView).clearState();
		verify(mockSynapseAlert).showLogin();
		verify(mockView).setVisible(true);
	}
	
	@Test
	public void testHandleServiceExceptionNotFound() {
		widget.handleException(new NotFoundException());
		verify(mockView).clearState();
		verify(mockView).show404();
		verify(mockView).setVisible(true);
	}
	
	@Test
	public void testHandleServiceUnknownErrorExceptionLoggedIn() {
		String errorMessage= "unknown";
		Exception e =new UnknownErrorException(errorMessage);
		widget.handleException(e);
		verify(mockView).clearState();
		verify(mockSynapseAlert).handleException(e);
		verify(mockView).setVisible(true);
	}
	
	@Test
	public void testIsUserLoggedIn() {
		widget.isUserLoggedIn();
		verify(mockSynapseAlert).isUserLoggedIn();
	}
	
	@Test
	public void testShowMustLogin() {
		widget.showLogin();
		verify(mockView).clearState();
		verify(mockSynapseAlert).showLogin();
		verify(mockView).setVisible(true);
	}
	
	@Test
	public void testShowError() {
		String errorMessage = "a handled error";
		widget.showError(errorMessage);
		verify(mockView).clearState();
		verify(mockSynapseAlert).showError(errorMessage);
		verify(mockView).setVisible(true);
	}
	
	@Test
	public void testShowEntity403() {
		String entityId = "syn123";
		widget.show403(entityId);
		verify(mockView).clearState();
		verify(mockView).showRequestAccessUI();
		assertEquals(entityId, widget.getEntityId());
	}
	
	@Test
	public void testOnRequestAccess() {
		widget.onRequestAccess();
		verify(mockView).showRequestAccessButtonLoading();
		verify(mockSynapseClient).sendMessageToEntityOwner(anyString(), anyString(), stringCaptor.capture(), anyString(), any(AsyncCallback.class));
		String messageBody = stringCaptor.getValue();
		assertTrue(messageBody.contains("#!Profile:"+USER_ID));
		verify(mockView).showInfo(anyString());
		verify(mockView).hideRequestAccessUI();
	}
	@Test
	public void testOnRequestAccessFailure() {
		Exception ex = new Exception("ex");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).sendMessageToEntityOwner(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		widget.onRequestAccess();
		verify(mockView).showRequestAccessButtonLoading();
		verify(mockSynapseClient).sendMessageToEntityOwner(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynapseAlert).handleException(ex);
	}
		
}
