package org.sagebionetworks.web.unitclient.widget.login;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.login.LoginWidget;
import org.sagebionetworks.web.client.widget.login.LoginWidgetView;

public class LoginWidgetTest {

	LoginWidget loginWidget;
	@Mock
	LoginWidgetView mockView;
	@Mock
	AuthenticationController mockAuthController;

	@Before
	public void setup() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		loginWidget = new LoginWidget(mockView, mockAuthController);
		when(mockAuthController.isLoggedIn()).thenReturn(false);
	}

	@Test
	public void testAsWidget() {
		loginWidget.asWidget();

		verify(mockView).setVisible(true);
		verify(mockView).asWidget();
	}

	@Test
	public void testAsWidgetLoggedIn() {
		when(mockAuthController.isLoggedIn()).thenReturn(true);

		loginWidget.asWidget();

		verify(mockView).setVisible(false);
		verify(mockView).asWidget();
	}

	@Test
	public void testClear() {
		loginWidget.clear();

		verify(mockView).clear();
	}

}
