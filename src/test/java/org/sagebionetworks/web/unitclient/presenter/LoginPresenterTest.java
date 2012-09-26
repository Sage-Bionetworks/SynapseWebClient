package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.AtLeast;
import org.mockito.verification.VerificationMode;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
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
	
	@Before
	public void setup(){
		mockView = mock(LoginView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserAccountServiceAsync = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockCookieProvier = mock(CookieProvider.class);
		mockGwtWrapper = mock(GWTWrapper.class);
		loginPresenter = new LoginPresenter(mockView, mockAuthenticationController, mockUserAccountServiceAsync, mockGlobalApplicationState, mockNodeModelCreator,mockCookieProvier, mockGwtWrapper);
		
		verify(mockView).setPresenter(loginPresenter);
	}	
	
	@Test
	public void testSetPlace() {
		LoginPlace place = Mockito.mock(LoginPlace.class);
		loginPresenter.setPlace(place);
	}
	
	@Test
	public void testFastpassValidSession() throws Exception {
		LoginPlace loginPlace = new LoginPlace(LoginPlace.FASTPASS_TOKEN);
		UserSessionData myTestUserSessionData = new UserSessionData();
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(myTestUserSessionData);
		AsyncMockStubber.callSuccessWith("my user").when(mockAuthenticationController).loginUser(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("myfastpassurl").when(mockUserAccountServiceAsync).getFastPassSupportUrl(any(AsyncCallback.class));	
		loginPresenter.showView(loginPlace);
		verify(mockUserAccountServiceAsync).getFastPassSupportUrl(any(AsyncCallback.class));
		verify(mockGwtWrapper).replaceThisWindowWith(any(String.class));
	}
	
	@Test
	public void testFastpassInvalidSession() {
		LoginPlace loginPlace = new LoginPlace(LoginPlace.FASTPASS_TOKEN);
		UserSessionData myTestUserSessionData = new UserSessionData();
		//return the test user the first time (as if logged in), then null the second time (simulate logging out)
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(myTestUserSessionData, null);
		AsyncMockStubber.callFailureWith(new Exception()).when(mockAuthenticationController).loginUser(anyString(), any(AsyncCallback.class));		
		loginPresenter.showView(loginPlace);
		verify(mockAuthenticationController, new AtLeast(1)).logoutUser();
		verify(mockCookieProvier).setCookie(DisplayUtils.FASTPASS_LOGIN_COOKIE_VALUE, Boolean.TRUE.toString());
	}
}
