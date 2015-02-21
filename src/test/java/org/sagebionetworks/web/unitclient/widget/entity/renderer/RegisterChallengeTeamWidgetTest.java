package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialog;
import org.sagebionetworks.web.client.widget.entity.renderer.RegisterChallengeTeamWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
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
	public static final String CUSTOM_BUTTON_TEXT="register a team for this challenge";
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
		verify(mockView).setButtonVisible(true);
		
		widget.onClick();
		//add dialog to view
		verify(mockView).clearWidgets();
		verify(mockView).addWidget(any(Widget.class));
		//and configure dialog
		verify(mockRegisterTeamDialog).configure(eq(CHALLENGE_ID), any(Callback.class));
	}
	
	@Test
	public void testHappyCaseConfigureAnonymous() throws Exception {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockView).setButtonText(CUSTOM_BUTTON_TEXT);
		verify(mockView).setButtonVisible(false);
		
		//on click, should show anonymous join message
		widget.onClick();
		verify(mockView).showConfirmDialog(anyString(), any(ConfirmCallback.class));
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
}











