package org.sagebionetworks.web.unitclient.widget.accessrequirements;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandlerImpl.SESSION_KEY_PREFIX;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.HideACTActionsButton;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;

import com.google.gwt.event.dom.client.ClickHandler;

public class HideACTActionsButtonTest {
	HideACTActionsButton widget;
	@Mock
	Button mockButton; 
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SessionStorage mockSessionStorage;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Captor
	ArgumentCaptor<CallbackP<Boolean>> callbackPCaptor;
	ClickHandler onButtonClickHandler;
	public static final String CURRENT_USER_ID = "33325";
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(CURRENT_USER_ID);
		widget = new HideACTActionsButton(mockButton, mockIsACTMemberAsyncHandler, mockSessionStorage, mockAuthenticationController, mockGlobalApplicationState);
		verify(mockButton).addClickHandler(clickHandlerCaptor.capture());
		onButtonClickHandler = clickHandlerCaptor.getValue();
	}

	private void verifyIsACTMember(boolean isACT) {
		verify(mockIsACTMemberAsyncHandler).isACTMember(callbackPCaptor.capture());
		callbackPCaptor.getValue().invoke(isACT);
	}
	
	@Test
	public void testConstruction() {
		verify(mockButton).setVisible(false);
		verify(mockButton).setText(HideACTActionsButton.HIDE_ACT_UI);
	}
	
	@Test
	public void testOnClick() {
		onButtonClickHandler.onClick(null);
		verify(mockSessionStorage).setItem(SESSION_KEY_PREFIX + CURRENT_USER_ID, Boolean.FALSE.toString());
		verify(mockGlobalApplicationState).refreshPage();
	}
	
	@Test
	public void testIsACTMember() {
		verifyIsACTMember(true);
		verify(mockButton).setVisible(true);
	}
	
	@Test
	public void testIsNotACTMember() {
		verifyIsACTMember(false);
		verify(mockButton, times(2)).setVisible(false);
	}
}
