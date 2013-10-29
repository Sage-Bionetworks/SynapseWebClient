package org.sagebionetworks.web.unitclient.widget.login;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.login.LoginWidgetView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginWidgetTest {
		
	LoginWidget loginWidget;
	LoginWidgetView mockView;
	AuthenticationController mockAuthController;
	NodeModelCreator mockNodeModelCreator;
	GlobalApplicationState mockGlobalApplicationState;
	@Before
	public void setup(){		
		mockView = mock(LoginWidgetView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		loginWidget = new LoginWidget(mockView, mockAuthController, mockNodeModelCreator, mockGlobalApplicationState);
		
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
		loginWidget.setUsernameAndPassword(u, p, true);
		
		verify(mockAuthController).loginUser(anyString(), anyString(), anyBoolean(), (AsyncCallback<String>) any());
	}

}
