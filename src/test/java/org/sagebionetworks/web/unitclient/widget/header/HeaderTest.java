package org.sagebionetworks.web.unitclient.widget.header;

import static org.mockito.Matchers.*;
import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.HeaderView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class HeaderTest {
		
	Header header;
	HeaderView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	@Before
	public void setup(){
		mockView = Mockito.mock(HeaderView.class);		
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		mockGlobalApplicationState = Mockito.mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		header = new Header(mockView, mockAuthenticationController, mockGlobalApplicationState);
	}
	
	@Test
	public void testSetPresenter() {
		verify(mockView).setPresenter(header);
	}
	
	@Test
	public void testAsWidget(){
		header.asWidget();
	}
	
	@Test
	public void testOnGettingStartedClick() {
		header.onGettingStartedClick();
		verify(mockPlaceChanger).goTo(any(Help.class));
	}
	
	@Test
	public void testOnDashboardClickLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn("008");
		header.onDashboardClick();
		verify(mockPlaceChanger).goTo(any(Profile.class));
	}
	@Test
	public void testOnDashboardClickAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		header.onDashboardClick();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}
	@Test
	public void testOnTrashClick() {
		header.onTrashClick();
		verify(mockPlaceChanger).goTo(any(Trash.class));
	}
	
	@Test
	public void testOnLogoutClick() {
		header.onLogoutClick();
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Place place = captor.getValue();
		assertTrue(place instanceof LoginPlace);
		assertEquals(LoginPlace.LOGOUT_TOKEN, ((LoginPlace)place).toToken());
	}
	

	@Test
	public void testOnLoginClick() {
		header.onLoginClick();
		ArgumentCaptor<Place> captor = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(captor.capture());
		Place place = captor.getValue();
		assertTrue(place instanceof LoginPlace);
		assertEquals(LoginPlace.LOGIN_TOKEN, ((LoginPlace)place).toToken());
	}
	
	@Test
	public void testOnRegisterClick() {
		header.onRegisterClick();
		verify(mockPlaceChanger).goTo(any(RegisterAccount.class));
	}
}
