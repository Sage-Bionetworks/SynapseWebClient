package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.LoginView;

public class LoginPresenterTest {
	
	LoginPresenter loginPresenter;
	LoginView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserAccountServiceAsync;
	GlobalApplicationState mockGlobalApplicationState;
	NodeModelCreator mockNodeModelCreator;
	CookieProvider mockCookieProvier;
	
	@Before
	public void setup(){
		mockView = mock(LoginView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserAccountServiceAsync = mock(UserAccountServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockCookieProvier = mock(CookieProvider.class);
		loginPresenter = new LoginPresenter(mockView, mockAuthenticationController, mockUserAccountServiceAsync, mockGlobalApplicationState, mockNodeModelCreator,mockCookieProvier);
		
		verify(mockView).setPresenter(loginPresenter);
	}	
	
	@Test
	public void testSetPlace() {
		LoginPlace place = Mockito.mock(LoginPlace.class);
		loginPresenter.setPlace(place);
	}	
}
