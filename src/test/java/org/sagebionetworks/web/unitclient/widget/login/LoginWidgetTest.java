package org.sagebionetworks.web.unitclient.widget.login;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.login.LoginWidgetView;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginWidgetTest {
		
	LoginWidget loginWidget;
	@Mock
	LoginWidgetView mockView;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	Callback mockUserListener;
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	@Captor
	ArgumentCaptor<Place> placeCaptor;
	@Mock
	SynapseAlert mockSynAlert;
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		MockitoAnnotations.initMocks(this);
		when(mockSynapseJSNIUtils.getLocationPath()).thenReturn("/Portal.html");
		when(mockSynapseJSNIUtils.getLocationQueryString()).thenReturn("?foo=bar");

		loginWidget = new LoginWidget(mockView, mockAuthController, mockGlobalApplicationState, mockSynAlert);
		loginWidget.setUserListener(mockUserListener);
		UserProfile p = new UserProfile();
		p.setOwnerId("12");
		AsyncMockStubber.callSuccessWith(p).when(mockAuthController).loginUser(anyString(),anyString(),any(AsyncCallback.class));
		verify(mockView).setSynAlert(mockSynAlert);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}
	
	@Test
	public void testAsWidget(){
		loginWidget.asWidget();
		verify(mockView).setPresenter(loginWidget);
	}
	
	@Test
	public void testSetUsernameAndPassword() {
		String u = "user";
		String p = "pass";
		loginWidget.setUsernameAndPassword(u, p);
		
		verify(mockAuthController).loginUser(anyString(), anyString(), (AsyncCallback) any());
		verify(mockView).clear();
		verify(mockView).clearUsername();
	}
	
	@Test
	public void testSetUsernameAndPasswordErrorHandling() {
		String u = "user";
		String p = "pass";
		String unhandledExceptionMessage = "unhandled exception";
		Exception ex = new Exception(unhandledExceptionMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockAuthController).loginUser(anyString(),anyString(),any(AsyncCallback.class));
		loginWidget.setUsernameAndPassword(u, p);
		verify(mockAuthController).loginUser(anyString(), anyString(), (AsyncCallback) any());
		verify(mockUserListener, never()).invoke();
		verify(mockSynAlert).handleException(ex);
		verify(mockView).clear();
		verify(mockView, never()).clearUsername();
	}

	@Test
	public void testInvalidUsernamePassword() {
		String u = "user";
		String p = "pass";
		String notFoundMessage = "wrong username or password";
		//service call responds with an Unauthorized Exception if credentials are invalid
		UnauthorizedException ex = new UnauthorizedException(notFoundMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockAuthController).loginUser(anyString(),anyString(),any(AsyncCallback.class));
		loginWidget.setUsernameAndPassword(u, p);
		verify(mockAuthController).loginUser(anyString(), anyString(), (AsyncCallback) any());
		verify(mockUserListener, never()).invoke();
		verify(mockView).clear();
		verify(mockView, never()).clearUsername();
		verify(mockSynAlert).clear();
		verify(mockSynAlert).showError(notFoundMessage + LoginWidget.PLEASE_TRY_AGAIN);
	}
	
	@Test
	public void testInvalidPassword() {
		String u = "user";
		String p = "pass";
		String notFoundMessage = "wrong password";
		UnauthorizedException ex = new UnauthorizedException(notFoundMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockAuthController).loginUser(anyString(),anyString(),any(AsyncCallback.class));
		loginWidget.setUsernameAndPassword(u, p);
		verify(mockAuthController).loginUser(anyString(), anyString(), (AsyncCallback) any());
		verify(mockUserListener, never()).invoke();
		verify(mockView).clear();
		verify(mockView, never()).clearUsername();
		verify(mockSynAlert).clear();
		verify(mockSynAlert).showError(notFoundMessage + LoginWidget.PLEASE_TRY_AGAIN);
	}

}
