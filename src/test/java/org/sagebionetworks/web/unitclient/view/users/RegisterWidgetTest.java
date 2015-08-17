package org.sagebionetworks.web.unitclient.view.users;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.view.users.RegisterWidgetView;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class RegisterWidgetTest {
	
	RegisterWidgetView mockView;
	CookieProvider mockCookieProvider;
	UserAccountServiceAsync mockUserService;
	GlobalApplicationState mockGlobalApplicationState;
	GWTWrapper mockGWTWrapper;
	
	RegisterWidget widget;
	String email = "test@test.com";
	@Before
	public void setup() {
		mockView = mock(RegisterWidgetView.class);
		mockCookieProvider = mock(CookieProvider.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		widget = new RegisterWidget(mockView, mockUserService, mockGlobalApplicationState, mockGWTWrapper);			
		verify(mockView).setPresenter(widget);
		
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).createUserStep1(anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testRegisterUser() {
		widget.registerUser(email);
		verify(mockView).showInfo(anyString(), anyString());
	}

	@Test
	public void testRegisterUserUserExists() {
		AsyncMockStubber.callFailureWith(new ConflictException("user exists")).when(mockUserService).createUserStep1(anyString(), anyString(), any(AsyncCallback.class));
		widget.registerUser(email);
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testRegisterUserServiceFailure() {
		AsyncMockStubber.callFailureWith(new RestServiceException("unknown error")).when(mockUserService).createUserStep1(anyString(), anyString(), any(AsyncCallback.class));
		widget.registerUser(email);
		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
	}

}
