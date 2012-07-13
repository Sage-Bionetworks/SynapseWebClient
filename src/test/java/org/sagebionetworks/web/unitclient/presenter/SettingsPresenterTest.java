package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.shared.users.UserData;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SettingsPresenterTest {
	
	SettingsPresenter profilePresenter;
	SettingsView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;	
	CookieProvider mockCookieProvider;
	Settings place = Mockito.mock(Settings.class);
	
	UserData testUser = new UserData("testuser@test.com", "tester", "token", false);
	String password = "password";
	
	@Before
	public void setup() {
		mockView = mock(SettingsView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockCookieProvider = mock(CookieProvider.class);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider);	
		verify(mockView).setPresenter(profilePresenter);

		profilePresenter.setPlace(place);		
	}
	
	@Test
	public void testStart() {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider);	
		profilePresenter.setPlace(place);

		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);		
		
		profilePresenter.start(panel, eventBus);		
		verify(panel).setWidget(mockView);
	}
	
	@Test
	public void testSetPlace() {
		Settings newPlace = Mockito.mock(Settings.class);
		profilePresenter.setPlace(newPlace);
	}
	
	@Test
	public void testResetPassword() {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		reset(mockGlobalApplicationState);
		reset(mockCookieProvider);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider);	
		profilePresenter.setPlace(place);
		
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(testUser);
		String newPassword = "otherpassword";
		
		profilePresenter.resetPassword(password, newPassword);
	}
	
	@Test
	public void testCreateSynapsePassword() {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		reset(mockGlobalApplicationState);
		reset(mockCookieProvider);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider);	
		profilePresenter.setPlace(place);

		when(mockAuthenticationController.getLoggedInUser()).thenReturn(testUser);
		
		profilePresenter.createSynapsePassword();
	}	
}