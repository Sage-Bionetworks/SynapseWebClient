package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.users.RegisterAccountPresenter;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;

public class RegisterAccountPresenterTest {
	
	@Mock
	RegisterAccountPresenter registerAccountPresenter;
	@Mock
	RegisterAccountView mockView;
	@Mock
	RegisterAccount mockPlace;
	String email = "test@test.com";
	@Mock
	RegisterWidget mockRegisterWidget;
	@Mock
	Header mockHeader;
	@Mock
	Footer mockFooter;
	
	@Mock
	EventBus mockEventBus;
	@Mock
	AcceptsOneWidget mockAcceptsOneWidget;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		registerAccountPresenter = new RegisterAccountPresenter(mockView, mockRegisterWidget, mockHeader, mockFooter );			
	}
	
	@Test
	public void testStart() {
		registerAccountPresenter.start(mockAcceptsOneWidget, mockEventBus);		
		verify(mockAcceptsOneWidget).setWidget(mockView);
	}
	
	@Test
	public void testSetPlace() {
		//with email
		when(mockPlace.toToken()).thenReturn(email);
		registerAccountPresenter.setPlace(mockPlace);
		
		verify(mockRegisterWidget).setEmail(email);
		verify(mockRegisterWidget).configure(false);
		verify(mockView).setRegisterWidget(any(Widget.class));
		verify(mockHeader).configure(false);
		verify(mockHeader).refresh();
		verify(mockView).setFooterWidget(any(Widget.class));
		verify(mockView).setHeaderWidget(any(Widget.class));
	}
}
