package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
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
	Header mockHeader;
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
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockRegisterWidget, mockHeader, mockAuthController, mockGlobalAppState);			
	}
	
	@Test
	public void testStart() {
		registerAccountPresenter.start(mockAcceptsOneWidget, mockEventBus);		
		verify(mockAcceptsOneWidget).setWidget(mockView);
	}
	
	@Test
	public void testSetPlace() {
		//with email
		when(mockPlace.toToken()).thenReturn(email);
		registerAccountPresenter.setPlace(mockPlace);
		
		verify(mockRegisterWidget).setEmail(email);
		verify(mockView).setRegisterWidget(any(Widget.class));
		verify(mockHeader).configure();
		verify(mockHeader).refresh();
	}
	@Test
	public void testSetPlaceLoggedIn() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		
		registerAccountPresenter.setPlace(mockPlace);
		
		verify(mockPlaceChanger).goTo(placeCaptor.capture());
		Place wentToPlace = placeCaptor.getValue();
		assertTrue(wentToPlace instanceof Profile);
		assertEquals(Profile.VIEW_PROFILE_TOKEN, ((Profile)wentToPlace).toToken());
	}	
}
