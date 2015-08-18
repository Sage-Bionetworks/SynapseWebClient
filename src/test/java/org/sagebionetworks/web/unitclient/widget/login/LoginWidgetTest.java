package org.sagebionetworks.web.unitclient.widget.login;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.login.LoginWidgetView;
import org.sagebionetworks.web.client.widget.login.UserListener;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginWidgetTest {
		
	LoginWidget loginWidget;
	LoginWidgetView mockView;
	AuthenticationController mockAuthController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	UserListener mockUserListener;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	@Before
	public void setup() throws JSONObjectAdapterException{		
		mockView = mock(LoginWidgetView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockUserListener = mock(UserListener.class);
		when(mockSynapseJSNIUtils.getLocationPath()).thenReturn("/Portal.html");
		when(mockSynapseJSNIUtils.getLocationQueryString()).thenReturn("?foo=bar");

		loginWidget = new LoginWidget(mockView, mockAuthController, mockGlobalApplicationState, mockSynapseJSNIUtils);
		loginWidget.setUserListener(mockUserListener);
		UserSessionData usd = new UserSessionData();
		UserProfile p = new UserProfile();
		p.setOwnerId("12");
		usd.setProfile(p);
		AsyncMockStubber.callSuccessWith(usd).when(mockAuthController).loginUser(anyString(),anyString(),any(AsyncCallback.class));
		verify(mockView).setPresenter(loginWidget);
	}
	
	@Test
	public void testAsWidget(){
		loginWidget.asWidget();
	}
	
	@Test
	public void testSetUsernameAndPassword() {
		String u = "user";
		String p = "pass";
		loginWidget.setUsernameAndPassword(u, p);
		
		verify(mockAuthController).loginUser(anyString(), anyString(), (AsyncCallback<UserSessionData>) any());
		verify(mockUserListener).userChanged(any(UserSessionData.class));
	}

	@Test
	public void testOpenIdReturnUrl() {
		Assert.assertEquals("/Portal.html?foo=bar#!LoginPlace", loginWidget.getOpenIdReturnUrl());
	}
	
	@Test
	public void testSetUsernameAndPasswordErrorHandling() {
		String u = "user";
		String p = "pass";
		String unhandledExceptionMessage = "unhandled exception";
		AsyncMockStubber.callFailureWith(new Exception(unhandledExceptionMessage)).when(mockAuthController).loginUser(anyString(),anyString(),any(AsyncCallback.class));
		loginWidget.setUsernameAndPassword(u, p);
		verify(mockAuthController).loginUser(anyString(), anyString(), (AsyncCallback<UserSessionData>) any());
		verify(mockUserListener, never()).userChanged(any(UserSessionData.class));
		verify(mockSynapseJSNIUtils).consoleError(eq(unhandledExceptionMessage));
	}
}
