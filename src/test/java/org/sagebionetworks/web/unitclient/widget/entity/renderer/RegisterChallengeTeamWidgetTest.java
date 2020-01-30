package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialog;
import org.sagebionetworks.web.client.widget.entity.renderer.RegisterChallengeTeamWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import com.google.gwt.user.client.ui.Widget;

public class RegisterChallengeTeamWidgetTest {

	SingleButtonView mockView;
	PortalGinInjector mockPortalGinInjector;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;

	RegisterTeamDialog mockRegisterTeamDialog;

	RegisterChallengeTeamWidget widget;
	Map<String, String> descriptor;
	public static final String CHALLENGE_ID = "55555";
	public static final String CUSTOM_BUTTON_TEXT = "register a team for this challenge";
	String entityId = "syn22";

	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		mockView = mock(SingleButtonView.class);
		mockPortalGinInjector = mock(PortalGinInjector.class);
		mockRegisterTeamDialog = mock(RegisterTeamDialog.class);
		when(mockPortalGinInjector.getRegisterTeamDialog()).thenReturn(mockRegisterTeamDialog);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		widget = new RegisterChallengeTeamWidget(mockView, mockPortalGinInjector, mockAuthenticationController, mockGlobalApplicationState);
		verify(mockView).setPresenter(widget);
		descriptor = new HashMap<String, String>();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		descriptor.put(WidgetConstants.CHALLENGE_ID_KEY, CHALLENGE_ID);
		descriptor.put(WidgetConstants.BUTTON_TEXT_KEY, CUSTOM_BUTTON_TEXT);
	}

	@Test
	public void testHappyCaseConfigureLoggedIn() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockView).setButtonText(CUSTOM_BUTTON_TEXT);

		widget.onClick();
		// add dialog to view
		verify(mockView).clearWidgets();
		verify(mockView).addWidget(any(Widget.class));
		// and configure dialog
		verify(mockRegisterTeamDialog).configure(eq(CHALLENGE_ID), any(Callback.class));
	}

	@Test
	public void testHappyCaseConfigureAnonymous() throws Exception {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockView).setButtonText(CUSTOM_BUTTON_TEXT);

		// on click, should show anonymous join message
		widget.onClick();
		verify(mockView).showConfirmDialog(anyString(), any(Callback.class));
	}

	@Test
	public void testHappyCaseConfigureDefaultButtonText() throws Exception {
		descriptor.remove(WidgetConstants.BUTTON_TEXT_KEY);
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockView).setButtonText(RegisterChallengeTeamWidget.DEFAULT_BUTTON_TEXT);
	}


	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setButtonType(ButtonType.PRIMARY);
	}
}


