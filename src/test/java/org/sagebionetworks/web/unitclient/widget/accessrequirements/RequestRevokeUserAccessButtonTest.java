package org.sagebionetworks.web.unitclient.widget.accessrequirements;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTRevokeUserAccessButton;
import org.sagebionetworks.web.client.widget.accessrequirements.RequestRevokeUserAccessButton;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;

import com.google.gwt.event.dom.client.ClickHandler;

public class RequestRevokeUserAccessButtonTest {
	RequestRevokeUserAccessButton widget;
	@Mock
	Button mockButton; 
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	ACTAccessRequirement mockAccessRequirement;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	JiraURLHelper mockJiraURLHelper;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PopupUtilsView mockPopupUtilsView;
	@Mock
	UserProfile mockUserProfile;
	@Mock
	UserSessionData mockUserSessionData;
	ClickHandler onButtonClickHandler;
	public static final Long AR_ID = 87654444L;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new RequestRevokeUserAccessButton(mockAuthController, mockIsACTMemberAsyncHandler, mockButton, mockJiraURLHelper, mockGlobalApplicationState, mockPopupUtilsView);
		verify(mockButton).addClickHandler(clickHandlerCaptor.capture());
		onButtonClickHandler = clickHandlerCaptor.getValue();
		when(mockAuthController.getCurrentUserSessionData()).thenReturn(mockUserSessionData);
		when(mockUserSessionData.getProfile()).thenReturn(mockUserProfile);
		when(mockUserProfile.getEmails()).thenReturn(Collections.singletonList("email@email.com"));
	}

	@Test
	public void testConstruction() {
		verify(mockButton).setVisible(false);
		verify(mockButton).setText(ACTRevokeUserAccessButton.REVOKE_BUTTON_TEXT);
	}

	@Test
	public void testConfigureWithAR() {
		//verify it does not even ask if act member unless state is APPROVED
		widget.configure(mockAccessRequirement, DataAccessSubmissionState.NOT_SUBMITTED);
		verify(mockIsACTMemberAsyncHandler, never()).isACTMember(any(CallbackP.class));
		widget.configure(mockAccessRequirement, DataAccessSubmissionState.CANCELLED);
		verify(mockIsACTMemberAsyncHandler, never()).isACTMember(any(CallbackP.class));
		widget.configure(mockAccessRequirement, DataAccessSubmissionState.SUBMITTED);
		verify(mockIsACTMemberAsyncHandler, never()).isACTMember(any(CallbackP.class));
		verify(mockButton, never()).setVisible(true);
		
		widget.configure(mockAccessRequirement, DataAccessSubmissionState.APPROVED);
		verify(mockIsACTMemberAsyncHandler).isACTMember(callbackPCaptor.capture());
		
		CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
		// invoking with false should hide the button again
		isACTMemberCallback.invoke(false);
		verify(mockButton, times(5)).setVisible(false);
		
		isACTMemberCallback.invoke(true);
		verify(mockButton).setVisible(true);
		
		// configured with an AR, when clicked it should open jira issue 
		String requestRevokeURL = "requestRevokeURLString";
		when(mockJiraURLHelper.createRevokeAccessIssue(any(String.class),any(String.class),any(String.class),any(String.class))).thenReturn(requestRevokeURL);
		onButtonClickHandler.onClick(null);
		verify(mockPopupUtilsView).openInNewWindow(requestRevokeURL);
	}
}
