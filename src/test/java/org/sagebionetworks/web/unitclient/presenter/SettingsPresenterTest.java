package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

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
	
	UserSessionData testUser = new UserSessionData();
	UserProfile profile = new UserProfile();
	String password = "password";
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(SettingsView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockCookieProvider = mock(CookieProvider.class);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider);	
		verify(mockView).setPresenter(profilePresenter);

		profilePresenter.setPlace(place);
		profile.setDisplayName("tester");
		profile.setOwnerId("testuser@test.com");
		profile.setUserName("testuser@test.com");
		testUser.setProfile(profile);
		testUser.setSessionToken("token");
		testUser.setIsSSO(false);
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
	public void testResetPassword() throws RestServiceException {
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
	public void testCreateSynapsePassword() throws RestServiceException {
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