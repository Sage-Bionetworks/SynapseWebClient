package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.UserRegistration;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class RegisterAccountPresenterTest {
	
	RegisterAccountPresenter registerAccountPresenter;
	RegisterAccountView mockView;
	CookieProvider mockCookieProvider;
	UserAccountServiceAsync mockUserService;
	GlobalApplicationState mockGlobalApplicationState;
	RegisterAccount place = Mockito.mock(RegisterAccount.class);
	
	@Before
	public void setup() {
		mockView = mock(RegisterAccountView.class);
		mockCookieProvider = mock(CookieProvider.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockCookieProvider, mockUserService, mockGlobalApplicationState);			
		verify(mockView).setPresenter(registerAccountPresenter);
	}
	
	@Test
	public void testStart() {
		reset(mockView);
		reset(mockCookieProvider);
		reset(mockUserService);
		reset(mockGlobalApplicationState);
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockCookieProvider, mockUserService, mockGlobalApplicationState);	
		registerAccountPresenter.setPlace(place);

		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);		
		
		registerAccountPresenter.start(panel, eventBus);		
		verify(panel).setWidget(mockView);
	}
	
	@Test
	public void testSetPlace() {
		reset(mockView);
		RegisterAccount newPlace = Mockito.mock(RegisterAccount.class);
		registerAccountPresenter.setPlace(newPlace);
		
		verify(mockView).setPresenter(registerAccountPresenter);
	}
	
	@Test
	public void testRegisterUser() {
		reset(mockView);
		reset(mockCookieProvider);
		reset(mockUserService);
		reset(mockGlobalApplicationState);
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockCookieProvider, mockUserService, mockGlobalApplicationState);	
		registerAccountPresenter.setPlace(place);
		
		String email = "test@test.com";
		String firstName = "Hello";
		String lastName = "Goodbye";
		
		registerAccountPresenter.registerUser(email, firstName, lastName);
	}


	@Test
	public void testRegisterUserUserExists() {
		reset(mockView);
		reset(mockCookieProvider);
		reset(mockUserService);
		reset(mockGlobalApplicationState);
		AsyncMockStubber.callFailureWith(new BadRequestException("user exists")).when(mockUserService).createUser(any(UserRegistration.class), any(AsyncCallback.class));
		
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockCookieProvider, mockUserService, mockGlobalApplicationState);	
		registerAccountPresenter.setPlace(place);
		
		String email = "test@test.com";
		String firstName = "Hello";
		String lastName = "Goodbye";
		
		registerAccountPresenter.registerUser(email, firstName, lastName);
		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_USER_ALREADY_EXISTS);
	}

	@Test
	public void testRegisterUserServiceFailure() {
		reset(mockView);
		reset(mockCookieProvider);
		reset(mockUserService);
		reset(mockGlobalApplicationState);
		AsyncMockStubber.callFailureWith(new RestServiceException("unknown error")).when(mockUserService).createUser(any(UserRegistration.class), any(AsyncCallback.class));
		
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockCookieProvider, mockUserService, mockGlobalApplicationState);	
		registerAccountPresenter.setPlace(place);
		
		String email = "test@test.com";
		String firstName = "Hello";
		String lastName = "Goodbye";
		
		registerAccountPresenter.registerUser(email, firstName, lastName);
		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
	}

}
