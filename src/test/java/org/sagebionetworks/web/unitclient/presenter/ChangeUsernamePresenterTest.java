package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.ChangeUsername;
import org.sagebionetworks.web.client.presenter.ChangeUsernamePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.ChangeUsernameView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ChangeUsernamePresenterTest {
	
	ChangeUsernamePresenter presenter;
	ChangeUsernameView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	SynapseAlert mockSynAlert;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	PlaceChanger mockPlaceChanger;
	AcceptsOneWidget mockPanel;
	EventBus mockEventBus;
	UserProfile profile;
	
	@Before
	public void setup(){
		mockView = mock(ChangeUsernameView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);		
		mockPanel = mock(AcceptsOneWidget.class);
		mockEventBus = mock(EventBus.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSynAlert = mock(SynapseAlert.class);
		
		profile = new UserProfile();
		profile.setOwnerId("1233");
		profile.setUserName("valid-username");
		
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(profile);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		presenter = new ChangeUsernamePresenter(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, jsonObjectAdapter, mockSynAlert);
		presenter.start(mockPanel, mockEventBus);
		verify(mockView).setPresenter(presenter);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
	}	
	
	private void setPlace() {
		ChangeUsername place = Mockito.mock(ChangeUsername.class);
		presenter.setPlace(place);
		verify(mockView).setSynapseAlertWidget(mockSynAlert.asWidget());
	}
	
	@Test
	public void testUpdateProfile() {
		setPlace();
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		AsyncCallback<Void> mockCallback = mock(AsyncCallback.class);
		presenter.updateProfile(profile, mockCallback);
		verify(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		verify(mockAuthenticationController).updateCachedProfile(eq(profile));
		verify(mockCallback).onSuccess(any(Void.class));
	}
	
	@Test
	public void testUpdateProfileFailed() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		setPlace();
		UserProfile profile = new UserProfile();
		profile.setOwnerId("1233");
		AsyncCallback<Void> mockCallback = mock(AsyncCallback.class);
		presenter.updateProfile(profile, mockCallback);
		verify(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		verify(mockCallback).onFailure(any(Throwable.class));
	}
	
	
	@Test 
	public void testSetUsernameSuccess()throws JSONObjectAdapterException {
		presenter.setUsername("newname");
		verify(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		//go to the last place
		verify(mockGlobalApplicationState).gotoLastPlace();	}

	
	@Test 
	public void testSetUsernameFailure()throws JSONObjectAdapterException {
		String exceptionMessage = "unhandled";
		Exception t = new Exception(exceptionMessage);
		AsyncMockStubber.callFailureWith(t).when(mockSynapseClient).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		presenter.setUsername("newname");
		verify(mockSynAlert).handleException(eq(t));
	}
}
