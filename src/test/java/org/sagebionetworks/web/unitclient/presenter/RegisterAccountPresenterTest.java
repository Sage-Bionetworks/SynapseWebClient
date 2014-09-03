package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
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
	GWTWrapper mockGWTWrapper;
	RegisterAccount place = Mockito.mock(RegisterAccount.class);
	SynapseClientAsync mockSynapseClient;
	String email = "test@test.com";
	
	@Before
	public void setup() {
		mockView = mock(RegisterAccountView.class);
		mockCookieProvider = mock(CookieProvider.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGWTWrapper = mock(GWTWrapper.class);
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockUserService, mockGlobalApplicationState, mockSynapseClient, mockGWTWrapper);			
		verify(mockView).setPresenter(registerAccountPresenter);
		
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_NAME.toString()), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_EMAIL.toString()), any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).createUserStep1(anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testStart() {
		reset(mockView);
		reset(mockCookieProvider);
		reset(mockUserService);
		reset(mockGlobalApplicationState);
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockUserService, mockGlobalApplicationState, mockSynapseClient, mockGWTWrapper);	
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
		registerAccountPresenter.registerUser(email);
		verify(mockView).showAccountCreated();
	}


	@Test
	public void testRegisterUserUserExists() {
		reset(mockView);
		reset(mockCookieProvider);
		reset(mockUserService);
		reset(mockGlobalApplicationState);
		AsyncMockStubber.callFailureWith(new ConflictException("user exists")).when(mockUserService).createUserStep1(anyString(), anyString(), any(AsyncCallback.class));
		
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockUserService, mockGlobalApplicationState, mockSynapseClient, mockGWTWrapper);	
		registerAccountPresenter.setPlace(place);
		
		registerAccountPresenter.registerUser(email);
		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_USER_ALREADY_EXISTS);
	}

	@Test
	public void testRegisterUserServiceFailure() {
		reset(mockView);
		reset(mockCookieProvider);
		reset(mockUserService);
		reset(mockGlobalApplicationState);
		AsyncMockStubber.callFailureWith(new RestServiceException("unknown error")).when(mockUserService).createUserStep1(anyString(), anyString(), any(AsyncCallback.class));
		
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockUserService, mockGlobalApplicationState, mockSynapseClient, mockGWTWrapper);	
		registerAccountPresenter.setPlace(place);
		
		registerAccountPresenter.registerUser(email);
		
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC_NOTIFY);
	}
	
	@Test
	public void testIsEmailAvailableTrue() {
		registerAccountPresenter.checkEmailAvailable("abcd@efg.com");
		verify(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_EMAIL.toString()), any(AsyncCallback.class));
		verify(mockView, never()).markEmailUnavailable();
	}
	
	@Test
	public void testIsEmailAvailableFalse() {
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_EMAIL.toString()), any(AsyncCallback.class));
		registerAccountPresenter.checkEmailAvailable("abcd@efg.com");
		verify(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_EMAIL.toString()), any(AsyncCallback.class));
		verify(mockView).markEmailUnavailable();
	}
}
