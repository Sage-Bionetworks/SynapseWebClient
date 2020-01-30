package org.sagebionetworks.web.unitclient.view.users;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.auth.NewUser;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.view.users.RegisterWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RegisterWidgetTest {

	@Mock
	RegisterWidgetView mockView;
	@Mock
	UserAccountServiceAsync mockUserService;
	@Mock
	GWTWrapper mockGWTWrapper;
	@Mock
	SynapseAlert mockSynAlert;

	RegisterWidget widget;
	String email = "test@test.com";
	NewUser newUser;

	@Before
	public void setup() {
		newUser = new NewUser();
		newUser.setEmail(email);
		MockitoAnnotations.initMocks(this);
		mockGWTWrapper = mock(GWTWrapper.class);
		widget = new RegisterWidget(mockView, mockUserService, mockGWTWrapper, mockSynAlert);
		verify(mockView).setPresenter(widget);

		AsyncMockStubber.callSuccessWith(null).when(mockUserService).createUserStep1(any(NewUser.class), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testRegisterUser() {
		widget.registerUser(email);
		verify(mockView).enableRegisterButton(false);
		verify(mockView).showInfo(DisplayConstants.ACCOUNT_EMAIL_SENT);
		verify(mockView).enableRegisterButton(true);
		verify(mockView).clear();
		verify(mockUserService).createUserStep1(eq(newUser), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testRegisterUserUserExists() {
		AsyncMockStubber.callFailureWith(new ConflictException("user exists")).when(mockUserService).createUserStep1(any(NewUser.class), anyString(), any(AsyncCallback.class));
		widget.registerUser(email);
		verify(mockSynAlert).showError(DisplayConstants.ERROR_EMAIL_ALREADY_EXISTS);
	}

	@Test
	public void testRegisterUserServiceFailure() {
		Exception ex = new Exception("unknown");
		AsyncMockStubber.callFailureWith(ex).when(mockUserService).createUserStep1(any(NewUser.class), anyString(), any(AsyncCallback.class));
		widget.registerUser(email);

		verify(mockSynAlert).handleException(ex);
	}

}
