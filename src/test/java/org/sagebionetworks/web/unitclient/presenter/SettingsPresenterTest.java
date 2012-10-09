package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.storage.StorageUsageSummary;
import org.sagebionetworks.repo.model.storage.StorageUsageSummaryList;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Settings;
import org.sagebionetworks.web.client.presenter.SettingsPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.SettingsView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SettingsPresenterTest {
	
	SettingsPresenter profilePresenter;
	SettingsView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;	
	CookieProvider mockCookieProvider;
	NodeModelCreator mockNodeModelCreator;
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
		mockNodeModelCreator = mock(NodeModelCreator.class);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator);	
		verify(mockView).setPresenter(profilePresenter);

		profilePresenter.setPlace(place);
		profile.setDisplayName("tester");
		profile.setOwnerId("testuser@test.com");
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
		reset(mockNodeModelCreator);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator);	
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
		reset(mockNodeModelCreator);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator);	
		profilePresenter.setPlace(place);
		
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(testUser);
		String newPassword = "otherpassword";
		
		profilePresenter.resetPassword("testuser@test.com", password, newPassword);
	}
	
	@Test
	public void testCreateSynapsePassword() throws RestServiceException {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		reset(mockGlobalApplicationState);
		reset(mockCookieProvider);
		reset(mockNodeModelCreator);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator);	
		profilePresenter.setPlace(place);

		when(mockAuthenticationController.getLoggedInUser()).thenReturn(testUser);
		profilePresenter.createSynapsePassword();
	}
	@Test
	public void testUsage() throws RestServiceException, JSONObjectAdapterException {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		reset(mockGlobalApplicationState);
		reset(mockCookieProvider);
		reset(mockNodeModelCreator);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator);	
		
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(testUser);
		StorageUsageSummaryList usageSummary = new StorageUsageSummaryList();
		Long totalSize = 12345l;
		usageSummary.setTotalSize(totalSize);
		usageSummary.setTotalCount(54321L);
		usageSummary.setSummaryList(new ArrayList<StorageUsageSummary>());
		
		when(mockNodeModelCreator.createEntity(any(String.class), eq(StorageUsageSummaryList.class))).thenReturn(usageSummary);
		AsyncMockStubber.callSuccessWith(EntityFactory.createJSONStringForEntity(usageSummary)).when(mockUserService).getStorageUsage(any(AsyncCallback.class));		
		profilePresenter.setPlace(place);
		verify(mockView).updateStorageUsage(eq(totalSize));
	}
	@Test
	public void testUsageFailure() throws RestServiceException {
		reset(mockView);
		reset(mockAuthenticationController);
		reset(mockUserService);
		reset(mockPlaceChanger);
		reset(mockGlobalApplicationState);
		reset(mockCookieProvider);
		reset(mockNodeModelCreator);
		profilePresenter = new SettingsPresenter(mockView, mockAuthenticationController, mockUserService, mockGlobalApplicationState, mockCookieProvider, mockNodeModelCreator);	
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(testUser);
		AsyncMockStubber.callFailureWith(new Exception()).when(mockUserService).getStorageUsage(any(AsyncCallback.class));		
		profilePresenter.setPlace(place);
		verify(mockView).clearStorageUsageUI();
	}
}