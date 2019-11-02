package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;

public class RegisterAccountPresenterTest {

	@Mock
	RegisterAccountPresenter registerAccountPresenter;
	@Mock
	RegisterAccountView mockView;
	@Mock
	RegisterAccount mockPlace;
	String email = "test@test.com";
	@Mock
	RegisterWidget mockRegisterWidget;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	EventBus mockEventBus;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	AcceptsOneWidget mockAcceptsOneWidget;
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockGoogleSynAlert;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockSynapseClient, mockRegisterWidget, mockAuthController, mockGlobalAppState, mockGoogleSynAlert);
	}

	@Test
	public void testStart() {
		registerAccountPresenter.start(mockAcceptsOneWidget, mockEventBus);
		verify(mockAcceptsOneWidget).setWidget(mockView);
	}

	@Test
	public void testSetPlace() {
		// with email
		when(mockPlace.toToken()).thenReturn(email);
		registerAccountPresenter.setPlace(mockPlace);

		verify(mockRegisterWidget).setEmail(email);
		verify(mockView).setRegisterWidget(any(Widget.class));
	}

	@Test
	public void testSetPlaceLoggedIn() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);

		registerAccountPresenter.setPlace(mockPlace);

		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		Place wentToPlace = placeCaptor.getValue();
		assertTrue(wentToPlace instanceof Profile);
		assertEquals(Profile.VIEW_PROFILE_TOKEN, ((Profile) wentToPlace).toToken());
	}

	@Test
	public void testIsUsernameAvailableTrue() {
		registerAccountPresenter.checkUsernameAvailable("abcd");
		verify(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_NAME.toString()), any(AsyncCallback.class));
		verify(mockGoogleSynAlert).clear();
		verify(mockGoogleSynAlert, never()).showError(anyString());
		verify(mockGoogleSynAlert, never()).handleException(any(Throwable.class));
	}

	@Test
	public void testIsUsernameAvailableFalse() {
		AsyncMockStubber.callSuccessWith(false).when(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_NAME.toString()), any(AsyncCallback.class));
		registerAccountPresenter.checkUsernameAvailable("abcd");
		verify(mockSynapseClient).isAliasAvailable(anyString(), eq(AliasType.USER_NAME.toString()), any(AsyncCallback.class));
		verify(mockGoogleSynAlert).clear();
		verify(mockGoogleSynAlert).showError(DisplayConstants.ERROR_USERNAME_ALREADY_EXISTS);
	}
}
