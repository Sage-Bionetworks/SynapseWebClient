package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.accessrequirements.ToggleACTActionsButton;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import com.google.gwt.event.dom.client.ClickHandler;

public class ToggleACTActionsButtonTest {
	ToggleACTActionsButton widget;
	@Mock
	Button mockButton;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Captor
	ArgumentCaptor<CallbackP<Boolean>> callbackPCaptor;
	ClickHandler onButtonClickHandler;
	public static final String CURRENT_USER_ID = "33325";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new ToggleACTActionsButton(mockButton, mockIsACTMemberAsyncHandler, mockGlobalApplicationState);
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
		verify(mockButton).setText(ToggleACTActionsButton.HIDE_ACT_UI);
	}

	@Test
	public void testOnClick() {
		when(mockIsACTMemberAsyncHandler.isACTActionVisible()).thenReturn(true);
		onButtonClickHandler.onClick(null);
		verify(mockIsACTMemberAsyncHandler).setACTActionVisible(false);
		verify(mockGlobalApplicationState).refreshPage();

		when(mockIsACTMemberAsyncHandler.isACTActionVisible()).thenReturn(false);
		onButtonClickHandler.onClick(null);
		verify(mockIsACTMemberAsyncHandler).setACTActionVisible(true);
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
