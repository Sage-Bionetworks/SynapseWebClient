package org.sagebionetworks.web.unitclient.widget.footer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.footer.FooterView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FooterTest {
		
	Footer footer;
	FooterView mockView;
	CookieProvider mockCookies;
	UserAccountServiceAsync mockUserService;
	
	@Before
	public void setup(){		
		mockView = Mockito.mock(FooterView.class);
		mockCookies = mock(CookieProvider.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		footer = new Footer(mockView, mockCookies, mockUserService);
		
		verify(mockView).setPresenter(footer);
	}
	
	@Test
	public void testAsWidget(){
		footer.asWidget();
	}
	
	@Test
	public void testSupportLinkClicked() throws RestServiceException{
		//getFastPassSupportUrl is called when opening the support site
		footer.gotoSupport();
		verify(mockUserService).getFastPassSupportUrl(any(AsyncCallback.class));
	}

}
