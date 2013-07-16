package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.AtLeast;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.LoginView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginPresenterTest {
	
	LoginPresenter loginPresenter;
	LoginView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserAccountServiceAsync;
	GlobalApplicationState mockGlobalApplicationState;
	NodeModelCreator mockNodeModelCreator;
	CookieProvider mockCookieProvier;
	GWTWrapper mockGwtWrapper;
	SynapseJSNIUtils mockJSNIUtils;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	@Before
	public void setup(){
		mockView = mock(LoginView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserAccountServiceAsync = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockCookieProvier = mock(CookieProvider.class);
		mockGwtWrapper = mock(GWTWrapper.class);
		mockJSNIUtils = mock(SynapseJSNIUtils.class);
		loginPresenter = new LoginPresenter(mockView, mockAuthenticationController, mockUserAccountServiceAsync, mockGlobalApplicationState, mockNodeModelCreator,mockCookieProvier, mockGwtWrapper, mockJSNIUtils);
		
		verify(mockView).setPresenter(loginPresenter);
	}	
	
	@Test
	public void testSetPlace() {
		LoginPlace place = Mockito.mock(LoginPlace.class);
		when(mockJSNIUtils.getLocationPath()).thenReturn("/Portal.html");
		when(mockJSNIUtils.getLocationQueryString()).thenReturn("?foo=bar");
		loginPresenter.setPlace(place);
		Assert.assertEquals("/Portal.html?foo=bar#!LoginPlace", loginPresenter.getOpenIdReturnUrl());
	}
	
	@Test
	public void testFastpassValidSession() throws Exception {
		LoginPlace loginPlace = new LoginPlace(LoginPlace.FASTPASS_TOKEN);
		UserSessionData myTestUserSessionData = new UserSessionData();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		AsyncMockStubber.callSuccessWith("my user").when(mockAuthenticationController).loginUser(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("myfastpassurl").when(mockUserAccountServiceAsync).getFastPassSupportUrl(any(AsyncCallback.class));	
		loginPresenter.showView(loginPlace);
		verify(mockUserAccountServiceAsync).getFastPassSupportUrl(any(AsyncCallback.class));
		verify(mockGwtWrapper).replaceThisWindowWith(any(String.class));
	}
	
	@Test
	public void testFastpassInvalidSession() throws Exception {
		LoginPlace loginPlace = new LoginPlace(LoginPlace.FASTPASS_TOKEN);
		UserSessionData myTestUserSessionData = new UserSessionData();
		//return the test user the first time (as if logged in), then null the second time (simulate logging out)		
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(myTestUserSessionData);
		when(mockNodeModelCreator.createJSONEntity(myTestUserSessionData.writeToJSONObject(adapterFactory.createNew()).toJSONString(), UserSessionData.class)).thenReturn(myTestUserSessionData);
		AsyncMockStubber.callFailureWith(new Exception()).when(mockAuthenticationController).loginUser(anyString(), any(AsyncCallback.class));		
		loginPresenter.showView(loginPlace);
		verify(mockAuthenticationController, new AtLeast(1)).logoutUser();
		verify(mockCookieProvier).setCookie(DisplayUtils.FASTPASS_LOGIN_COOKIE_VALUE, Boolean.TRUE.toString());
	}
}
