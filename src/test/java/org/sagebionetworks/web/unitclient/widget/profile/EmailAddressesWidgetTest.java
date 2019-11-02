package org.sagebionetworks.web.unitclient.widget.profile;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.principal.EmailQuarantineReason;
import org.sagebionetworks.repo.model.principal.EmailQuarantineStatus;
import org.sagebionetworks.repo.model.principal.NotificationEmail;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.EmailAddressesWidget;
import org.sagebionetworks.web.client.widget.profile.EmailAddressesWidgetView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EmailAddressesWidgetTest {
	EmailAddressesWidget widget;
	@Mock
	EmailAddressesWidgetView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	UserProfile mockUserProfile;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	NotificationEmail mockNotificationEmail;
	List<String> userEmails;
	public static final String USER_ID = "982";
	public static final String EMAIL1 = "one@one.com";
	public static final String EMAIL2 = "two@two.com";
	public static final String EMAIL3 = "three@three.com";
	@Mock
	EmailQuarantineStatus mockEmailQuarantineStatus;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		widget = new EmailAddressesWidget(mockView, mockSynapseClient, mockSynapseJavascriptClient, mockSynAlert, mockAuthenticationController, mockPopupUtils, mockGwt);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(USER_ID);
		userEmails = new ArrayList<>();
		userEmails.add(EMAIL1);
		userEmails.add(EMAIL2);
		when(mockUserProfile.getEmails()).thenReturn(userEmails);
		when(mockUserProfile.getOwnerId()).thenReturn(USER_ID);
		when(mockNotificationEmail.getEmail()).thenReturn(EMAIL1);
		AsyncMockStubber.callSuccessWith(mockNotificationEmail).when(mockSynapseJavascriptClient).getNotificationEmail(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).setNotificationEmail(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).removeEmail(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseJavascriptClient).additionalEmailValidation(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockUserProfile).when(mockSynapseJavascriptClient).getUserProfile(anyString(), any(AsyncCallback.class));

	}

	@Test
	public void testConfigure() {
		when(mockNotificationEmail.getQuarantineStatus()).thenReturn(null);

		widget.configure(mockUserProfile);

		verify(mockSynapseJavascriptClient).getNotificationEmail(any(AsyncCallback.class));
		verify(mockView).clearEmails();
		verify(mockSynAlert).clear();
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockView).addPrimaryEmail(EMAIL1, false);
		verify(mockView, never()).addSecondaryEmail(EMAIL1);
		verify(mockView).addSecondaryEmail(EMAIL2);
		verify(mockView).setVisible(true);
	}

	@Test
	public void testConfigureWithQuarantinedEmail() {
		when(mockEmailQuarantineStatus.getReason()).thenReturn(EmailQuarantineReason.PERMANENT_BOUNCE);
		when(mockNotificationEmail.getQuarantineStatus()).thenReturn(mockEmailQuarantineStatus);

		widget.configure(mockUserProfile);

		verify(mockView).addPrimaryEmail(EMAIL1, true);
		verify(mockView, never()).addSecondaryEmail(EMAIL1);
		verify(mockView).addSecondaryEmail(EMAIL2);
	}

	@Test
	public void testConfigureFailure() {
		Exception ex = new Exception("failed");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getNotificationEmail(any(AsyncCallback.class));
		widget.configure(mockUserProfile);

		verify(mockSynapseJavascriptClient).getNotificationEmail(any(AsyncCallback.class));
		verify(mockView).clearEmails();
		verify(mockSynAlert).clear();
		verify(mockView).setLoadingVisible(true);
		verify(mockView).setLoadingVisible(false);
		verify(mockView).setVisible(true);
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testConfigureAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.configure(mockUserProfile);
		verify(mockView).setVisible(false);
	}

	@Test
	public void testConfigureDifferentProfile() {
		when(mockUserProfile.getOwnerId()).thenReturn("user2");
		widget.configure(mockUserProfile);
		verify(mockView).setVisible(false);
	}

	@Test
	public void testOnAddEmail() {
		widget.configure(mockUserProfile);
		widget.onAddEmail("    " + EMAIL2 + "   ");

		verify(mockSynapseClient).setNotificationEmail(eq(EMAIL2), any(AsyncCallback.class));
		// reload profile
		verify(mockSynapseJavascriptClient).getUserProfile(eq(USER_ID), any(AsyncCallback.class));
	}

	@Test
	public void testOnAddEmailFailure() throws JSONObjectAdapterException {
		Exception caught = new Exception("unexpected exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).setNotificationEmail(anyString(), any(AsyncCallback.class));
		widget.configure(mockUserProfile);
		widget.onAddEmail(EMAIL2);
		verify(mockSynapseClient).setNotificationEmail(anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);
	}

	@Test
	public void testAdditionalEmailValidation() throws JSONObjectAdapterException {
		widget.configure(mockUserProfile);
		widget.additionalEmailValidation(EMAIL3 + "    ");
		verify(mockSynapseJavascriptClient).additionalEmailValidation(eq(USER_ID), eq(EMAIL3), anyString(), any(AsyncCallback.class));
		verify(mockPopupUtils).showInfo(DisplayConstants.EMAIL_ADDED);
	}

	@Test
	public void testAdditionalEmailValidationFailure() throws JSONObjectAdapterException {
		Exception ex = new Exception("unexpected exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).additionalEmailValidation(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		widget.additionalEmailValidation(EMAIL3);
		verify(mockSynapseJavascriptClient).additionalEmailValidation(eq(USER_ID), eq(EMAIL3), anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testAdditionalEmailValidationInvalidEmail() throws JSONObjectAdapterException {
		String email = "invalidEmailAddress";
		widget.additionalEmailValidation(email);
		verify(mockSynAlert).showError(WebConstants.INVALID_EMAIL_MESSAGE);
		verify(mockSynapseJavascriptClient, never()).additionalEmailValidation(anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testOnRemoveEmail() {
		widget.configure(mockUserProfile);
		widget.onRemoveEmail(EMAIL2);

		verify(mockSynapseClient).removeEmail(eq(EMAIL2), any(AsyncCallback.class));
		// reload profile
		verify(mockSynapseJavascriptClient).getUserProfile(eq(USER_ID), any(AsyncCallback.class));
	}

	@Test
	public void testOnRemoveEmailFailure() {
		Exception ex = new Exception("unexpected exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).removeEmail(anyString(), any(AsyncCallback.class));

		widget.configure(mockUserProfile);
		widget.onRemoveEmail(EMAIL2);

		verify(mockSynapseClient).removeEmail(eq(EMAIL2), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
}
