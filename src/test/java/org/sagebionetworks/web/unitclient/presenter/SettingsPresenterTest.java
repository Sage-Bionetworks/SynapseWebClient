package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.repo.model.storage.StorageUsageSummary;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SettingsPresenterTest {
	
	private static final String APIKEY = "MYAPIKEY";
	SettingsPresenter profilePresenter;
	SettingsView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;	
	CookieProvider mockCookieProvider;
	NodeModelCreator mockNodeModelCreator;
	Settings place = Mockito.mock(Settings.class);
	SynapseClientAsync mockSynapseClient;
	
	UserSessionData testUser = new UserSessionData();
	UserProfile profile = new UserProfile();
	String password = "password";
	String newPassword = "otherpassword";
	String username = "testuser";
	String email = "testuser@test.com";
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(SettingsView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockCookieProvider = mock(CookieProvider.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator, mockSynapseClient);	
		verify(mockView).setPresenter(profilePresenter);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(testUser);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		AsyncMockStubber.callSuccessWith(APIKEY).when(mockSynapseClient).getAPIKey(any(AsyncCallback.class));
		
		profile.setDisplayName("tester");
		profile.setEmail(username);
		profile.setUserName(username);
		List<String> emails = new ArrayList<String>();
		emails.add(email);
		profile.setEmails(emails);
		testUser.setProfile(profile);
		testUser.setSession(new Session());
		testUser.getSession().setSessionToken("token");
		testUser.setIsSSO(false);
	}
	
	@Test
	public void testStart() {
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator, mockSynapseClient);	
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
		
		verify(mockView).setApiKey(APIKEY);		
	}
	
	@Test
	public void testSetPlaceFailAPIKey() {
		Settings newPlace = Mockito.mock(Settings.class);
		AsyncMockStubber.callFailureWith(null).when(mockSynapseClient).getAPIKey(any(AsyncCallback.class));
		profilePresenter.setPlace(newPlace);
				
		verify(mockView).setApiKey(DisplayConstants.ERROR_LOADING);
		
	}
	
	@Test
	public void testResetPassword() throws RestServiceException {
		AsyncMockStubber.callSuccessWith("success initial login").when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(anyString(), eq(newPassword), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("success login with new pw").when(mockAuthenticationController).loginUser(eq(username), eq(newPassword), any(AsyncCallback.class));
		
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator, mockSynapseClient);
		profilePresenter.setPlace(place);
		
		profilePresenter.resetPassword(password, newPassword);
		verify(mockView).showPasswordChangeSuccess();		
	}
	
	@Test
	public void testResetPasswordFailInitialLogin() throws RestServiceException {		
		AsyncMockStubber.callFailureWith(null).when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator, mockSynapseClient);
		profilePresenter.setPlace(place);
		
		profilePresenter.resetPassword(password, newPassword);
		verify(mockView).passwordChangeFailed();		
	}

	@Test
	public void testResetPasswordFailChangePw() throws RestServiceException {		
		AsyncMockStubber.callSuccessWith("success initial login").when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(null).when(mockUserService).changePassword(anyString(), eq(newPassword), any(AsyncCallback.class));
		
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator, mockSynapseClient);
		profilePresenter.setPlace(place);
		
		profilePresenter.resetPassword(password, newPassword);
		verify(mockView).passwordChangeFailed();		
	}
	
	@Test
	public void testResetPasswordFailFinalLogin() throws RestServiceException {		
		AsyncMockStubber.callSuccessWith("success initial login").when(mockAuthenticationController).loginUser(eq(username), eq(password), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockUserService).changePassword(anyString(), eq(newPassword), any(AsyncCallback.class));
		AsyncMockStubber.callFailureWith(new Exception()).when(mockAuthenticationController).loginUser(eq(username), eq(newPassword), any(AsyncCallback.class));
		
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator, mockSynapseClient);
		profilePresenter.setPlace(place);
		
		profilePresenter.resetPassword(password, newPassword);
		verify(mockView).showPasswordChangeSuccess();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));		
	}
	
	@Test
	public void testCreateSynapsePassword() throws RestServiceException {
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator, mockSynapseClient);	
		profilePresenter.setPlace(place);

		profilePresenter.createSynapsePassword();
	}
	@Test
	public void testUsage() throws RestServiceException, JSONObjectAdapterException {
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator, mockSynapseClient);	
		
		StorageUsageSummaryList usageSummary = new StorageUsageSummaryList();
		Long totalSize = 12345l;
		usageSummary.setTotalSize(totalSize);
		usageSummary.setTotalCount(54321L);
		usageSummary.setSummaryList(new ArrayList<StorageUsageSummary>());
		
		when(mockNodeModelCreator.createJSONEntity(any(String.class), eq(StorageUsageSummaryList.class))).thenReturn(usageSummary);
		AsyncMockStubber.callSuccessWith(EntityFactory.createJSONStringForEntity(usageSummary)).when(mockUserService).getStorageUsage(any(AsyncCallback.class));		
		profilePresenter.setPlace(place);
		verify(mockView).updateStorageUsage(eq(totalSize));
	}
	@Test
	public void testUsageFailure() throws RestServiceException {
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator, mockSynapseClient);	
		
		AsyncMockStubber.callFailureWith(new Exception()).when(mockUserService).getStorageUsage(any(AsyncCallback.class));		
		profilePresenter.setPlace(place);
		verify(mockView).clearStorageUsageUI();
	}
}