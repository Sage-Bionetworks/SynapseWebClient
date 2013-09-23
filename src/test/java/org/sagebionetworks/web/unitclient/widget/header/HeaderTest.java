package org.sagebionetworks.web.unitclient.widget.header;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.HeaderView;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class HeaderTest {
		
	Header header;
	HeaderView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	
	@Before
	public void setup(){		
		mockView = Mockito.mock(HeaderView.class);		
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		header = new Header(mockView, mockAuthenticationController, mockUserService);
		
		verify(mockView).setPresenter(header);
	}
	
	@Test
	public void testAsWidget(){
		header.asWidget();
	}
		
}
