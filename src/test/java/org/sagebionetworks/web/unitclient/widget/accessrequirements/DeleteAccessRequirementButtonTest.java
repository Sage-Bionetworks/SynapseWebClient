package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton.DELETED_ACCESS_REQUIREMENT_SUCCESS_MESSAGE;
import static org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton.DELETE_ACCESS_REQUIREMENT_BUTTON_TEXT;
import static org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton.DELETE_ACCESS_REQUIREMENT_MESSAGE;
import static org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton.DELETE_ACCESS_REQUIREMENT_TITLE;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DeleteAccessRequirementButtonTest {
	DeleteAccessRequirementButton widget;
	@Mock
	Button mockButton;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	PopupUtilsView mockPopupUtilsView;
	@Mock
	AccessRequirement mockAccessRequirement;
	@Mock
	RestrictableObjectDescriptor mockSubject;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	CookieProvider mockCookies;
	@Mock
	Callback mockRefreshCallback;
	ClickHandler onButtonClickHandler;
	public static final Long AR_ID = 98L;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new DeleteAccessRequirementButton(mockButton, mockIsACTMemberAsyncHandler, mockSynapseClient, mockPopupUtilsView, mockCookies);
		verify(mockButton).addClickHandler(clickHandlerCaptor.capture());
		onButtonClickHandler = clickHandlerCaptor.getValue();
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteAccessRequirement(anyLong(), any(AsyncCallback.class));
		when(mockAccessRequirement.getId()).thenReturn(AR_ID);
	}

	@Test
	public void testConstruction() {
		verify(mockButton).setVisible(false);
	}

	private Callback verifyConfirmDelete() {
		verify(mockPopupUtilsView).showConfirmDialog(eq(DELETE_ACCESS_REQUIREMENT_TITLE), eq(DELETE_ACCESS_REQUIREMENT_MESSAGE), callbackCaptor.capture());
		return callbackCaptor.getValue();
	}

	@Test
	public void testConfigureWithAR() {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		widget.configure(mockAccessRequirement, mockRefreshCallback);
		verify(mockButton).setText(DELETE_ACCESS_REQUIREMENT_BUTTON_TEXT);
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPCaptor.capture());

		CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
		// invoking with false should hide the button again
		isACTMemberCallback.invoke(false);
		verify(mockButton, times(2)).setVisible(false);

		isACTMemberCallback.invoke(true);
		verify(mockButton).setVisible(true);

		// configured with an AR, when clicked it should confirm delete
		onButtonClickHandler.onClick(null);
		Callback deleteConfirmedCallback = verifyConfirmDelete();
		verifyZeroInteractions(mockSynapseClient);
		// confirm delete
		deleteConfirmedCallback.invoke();
		verify(mockSynapseClient).deleteAccessRequirement(eq(AR_ID), any(AsyncCallback.class));
		verify(mockPopupUtilsView).showInfo(eq(DELETED_ACCESS_REQUIREMENT_SUCCESS_MESSAGE));
		verify(mockRefreshCallback).invoke();
	}


	@Test
	public void testConfigureWithARNotInAlpha() {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn(null);
		widget.configure(mockAccessRequirement, mockRefreshCallback);
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPCaptor.capture());

		CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
		// invoking with false should hide the button again
		isACTMemberCallback.invoke(false);
		verify(mockButton, times(2)).setVisible(false);

		isACTMemberCallback.invoke(true);
		verify(mockButton, times(3)).setVisible(false);
	}

	@Test
	public void testFailureToDelete() {
		widget.configure(mockAccessRequirement, mockRefreshCallback);
		String errorMessage = "error";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).deleteAccessRequirement(anyLong(), any(AsyncCallback.class));
		widget.deleteAccessRequirementAfterConfirmation();
		verify(mockSynapseClient).deleteAccessRequirement(eq(AR_ID), any(AsyncCallback.class));
		verify(mockPopupUtilsView).showErrorMessage(errorMessage);
	}
}
