package org.sagebionetworks.web.unitclient.presenter;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.OAuth2NewAccount;
import org.sagebionetworks.web.client.presenter.OAuth2NewAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.OAuth2NewAccountView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class OAuth2NewAccountPresenterTest {
	
	OAuth2NewAccountPresenter newAccountPresenter;
	@Mock
	OAuth2NewAccountView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	OAuth2NewAccount mockPlace;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	SynapseAlert mockSynAlert;
	
	String testSessionToken = "1239381foobar";
	@Before
	public void setup() {
		newAccountPresenter = new OAuth2NewAccountPresenter(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthController, mockSynAlert);
		verify(mockView).setPresenter(newAccountPresenter);
		AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).isAliasAvailable(anyString(), anyString(), any(AsyncCallback.class));
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}
	
	@Test
	public void testSetPlaceLoggedIn() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		newAccountPresenter.setPlace(mockPlace);
		verify(mockView, atLeastOnce()).setPresenter(newAccountPresenter);
		verify(mockGlobalApplicationState).clearLastPlace();
		verify(mockAuthController).logoutUser();
	}
	
	@Test
	public void testSetPlaceWithError() {
		String error = "an error occurred";
		when(mockPlace.getParam(OAuth2NewAccountPresenter.ERROR_PLACE_PARAM)).thenReturn(error);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		newAccountPresenter.setPlace(mockPlace);
		verify(mockView, atLeastOnce()).setPresenter(newAccountPresenter);
		verify(mockGlobalApplicationState).clearLastPlace();
		verify(mockAuthController).logoutUser();
		verify(mockSynAlert).showError(error);
	}
	
	@Test
	public void testIsUsernameAvailableTooSmall() {
		//should not check if too short
		newAccountPresenter.checkUsernameAvailable("abc");
		verify(mockSynapseClient, never()).isAliasAvailable(anyString(), eq(AliasType.USER_NAME.toString()), any(AsyncCallback.class));
	}
	
	@Test
	public void testIsUsernameAvailableTrue() {
		newAccountPresenter.checkUsernameAvailable("abcd");
		verify(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_NAME.toString()), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		verify(mockSynAlert, never()).showError(anyString());
		verify(mockSynAlert, never()).handleException(any(Throwable.class));
	}
	
	@Test
	public void testIsUsernameAvailableFalse() {
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_NAME.toString()), any(AsyncCallback.class));
		newAccountPresenter.checkUsernameAvailable("abcd");
		verify(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_NAME.toString()), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		verify(mockSynAlert).showError(DisplayConstants.ERROR_USERNAME_ALREADY_EXISTS);
	}
}
