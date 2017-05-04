package org.sagebionetworks.web.unitclient.widget.accessrequirements;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTRevokeUserAccessButton;
import org.sagebionetworks.web.client.widget.accessrequirements.ManageAccessButton;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.act.ACTRevokeUserAccessModal;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;

public class ACTRevokeUserAccessButtonTest {
	ACTRevokeUserAccessButton widget;
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
	PortalGinInjector mockPortalGinInjector;
	@Mock
	ACTRevokeUserAccessModal mockACTRevokeUserAccessModal;
	
	ClickHandler onButtonClickHandler;
	public static final Long AR_ID = 87654444L;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new ACTRevokeUserAccessButton(mockButton, mockIsACTMemberAsyncHandler, mockPortalGinInjector);
		verify(mockButton).addClickHandler(clickHandlerCaptor.capture());
		onButtonClickHandler = clickHandlerCaptor.getValue();
		when(mockPortalGinInjector.getACTRevokeUserAccessModal()).thenReturn(mockACTRevokeUserAccessModal);
	}

	@Test
	public void testConstruction() {
		verify(mockButton).setVisible(false);
		verify(mockButton).setText(ACTRevokeUserAccessButton.REVOKE_BUTTON_TEXT);
	}

	@Test
	public void testConfigureWithAR() {
		widget.configure(mockAccessRequirement);
		verify(mockIsACTMemberAsyncHandler).isACTMember(callbackPCaptor.capture());
		
		CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
		// invoking with false should hide the button again
		isACTMemberCallback.invoke(false);
		verify(mockButton, times(2)).setVisible(false);
		
		isACTMemberCallback.invoke(true);
		verify(mockButton).setVisible(true);
		
		// configured with an AR, when clicked it should show the modal 
		onButtonClickHandler.onClick(null);
		verify(mockACTRevokeUserAccessModal).configure(mockAccessRequirement);
	}
}
