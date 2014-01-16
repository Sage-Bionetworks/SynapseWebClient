package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.validation.constraints.AssertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.Session;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class LoginPresenterTest {
	
	LoginPresenter loginPresenter;
	LoginView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	NodeModelCreator mockNodeModelCreator;
	CookieProvider mockCookieProvier;
	GWTWrapper mockGwtWrapper;
	SynapseJSNIUtils mockJSNIUtils;
	SynapseClientAsync mockSynapseClient;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	PlaceChanger mockPlaceChanger;
	AcceptsOneWidget mockPanel;
	EventBus mockEventBus;
	UserSessionData usd;
	
	@Before
	public void setup(){
		mockView = mock(LoginView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockCookieProvier = mock(CookieProvider.class);
		mockGwtWrapper = mock(GWTWrapper.class);
		mockJSNIUtils = mock(SynapseJSNIUtils.class);
		mockPlaceChanger = mock(PlaceChanger.class);		
		mockPanel = mock(AcceptsOneWidget.class);
		mockEventBus = mock(EventBus.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		usd = new UserSessionData();
		Session session = new Session();
		session.setAcceptsTermsOfUse(true);
		usd.setSession(session);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockJSNIUtils.getLocationPath()).thenReturn("/Portal.html");
		when(mockJSNIUtils.getLocationQueryString()).thenReturn("?foo=bar");
				
		loginPresenter = new LoginPresenter(mockView, mockAuthenticationController, mockGlobalApplicationState, mockNodeModelCreator,mockCookieProvier, mockGwtWrapper, mockJSNIUtils, jsonObjectAdapter, mockSynapseClient, adapterFactory);
		loginPresenter.start(mockPanel, mockEventBus);
		verify(mockView).setPresenter(loginPresenter);
	}	
	
	@Test
	public void testSetPlaceDefault() {
		LoginPlace place = Mockito.mock(LoginPlace.class);
		loginPresenter.setPlace(place);
		Assert.assertEquals("/Portal.html?foo=bar#!LoginPlace", loginPresenter.getOpenIdReturnUrl());
	}
	
	@Test 
	public void testSetPlaceLogout() {
		LoginPlace place = new LoginPlace(LoginPlace.LOGOUT_TOKEN);
		loginPresenter.setPlace(place);
		verify(mockView).showLogout(false);
	}

	@Test 
	public void testSetPlaceUnknownSSOUser() {
		LoginPlace place = new LoginPlace(WebConstants.OPEN_ID_UNKNOWN_USER_ERROR_TOKEN);
		loginPresenter.setPlace(place);
		ArgumentCaptor<Place> argument = ArgumentCaptor.forClass(Place.class);				   
		verify(mockView).showErrorMessage(anyString());
		verify(mockPlaceChanger).goTo(argument.capture());
		assertTrue(argument.getValue() instanceof RegisterAccount);
	}

	@Test 
	public void testSetPlaceUnknownSSOError() {
		LoginPlace place = new LoginPlace(WebConstants.OPEN_ID_ERROR_TOKEN);
		loginPresenter.setPlace(place);
		verify(mockView).showErrorMessage(anyString());
		verify(mockView).showLogin(anyString(), anyString());
	}
	
	@Test 
	public void testSetPlaceSSOLogin() {
		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
		LoginPlace place = new LoginPlace(fakeToken);
		AsyncMockStubber.callSuccessWith("success").when(mockAuthenticationController).loginUserSSO(anyString(), any(AsyncCallback.class));		
		
		loginPresenter.setPlace(place);
		verify(mockAuthenticationController).loginUserSSO(eq(fakeToken), any(AsyncCallback.class));
		verify(mockEventBus).fireEvent(any(PlaceChangeEvent.class));
	}

//	@Test 
//	public void testSetPlaceSSOLoginNotSignedToU() {
//		String fakeToken = "0e79b99-4bf8-4999-b3a2-5f8c0a9499eb";
//		LoginPlace place = new LoginPlace(fakeToken);
//		AsyncMockStubber.callSuccessWith("success").when(mockAuthenticationController).loginUserSSO(anyString(), any(AsyncCallback.class));		
//		usd.getSession().setAcceptsTermsOfUse(false);
//		AsyncMockStubber.callSuccessWith("tou").when(mockAuthenticationController).getTermsOfUse(any(AsyncCallback.class));
// 
// 		To be continued...
//		
//		loginPresenter.setPlace(place);
//		verify(mockAuthenticationController).loginUserSSO(eq(fakeToken), any(AsyncCallback.class));
//		verify(mockEventBus).fireEvent(any(PlaceChangeEvent.class));
//	}

	
}
