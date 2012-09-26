package org.sagebionetworks.web.unitclient.widget.header;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.HeaderView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class HeaderTest {
		
	Header header;
	HeaderView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	NodeModelCreator mockNodeModelCreator;
	UserAccountServiceAsync mockUserService;
	@Before
	public void setup(){		
		mockView = Mockito.mock(HeaderView.class);		
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		header = new Header(mockView, mockAuthenticationController, mockGlobalApplicationState, mockNodeModelCreator, mockUserService);
		
		verify(mockView).setPresenter(header);
	}
	
	@Test
	public void testAsWidget(){
		header.asWidget();
	}
	
	@Test
	public void testSupportLinkClicked() throws RestServiceException{
		//getFastPassSupportUrl is called when opening the support site
		when(mockAuthenticationController.getLoggedInUser()).thenReturn(new UserSessionData());
		
		header.getSupportHRef(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
		verify(mockUserService).getFastPassSupportUrl(any(AsyncCallback.class));
	}

	
}
