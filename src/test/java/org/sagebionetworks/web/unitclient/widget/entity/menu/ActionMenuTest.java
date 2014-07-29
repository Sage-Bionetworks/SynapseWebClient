package org.sagebionetworks.web.unitclient.widget.entity.menu;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityEditor;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenu;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class ActionMenuTest {
		
	ActionMenu actionMenu;
	ActionMenuView mockView;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	EntityTypeProvider mockEntityTypeProvider;
	SynapseClientAsync mockSynapseClient;
	EntityEditor mockEntityEditor;
	GlobalApplicationState mockGlobalApplicationState;
	JSONObjectAdapter jSONObjectAdapter = new JSONObjectAdapterImpl();
	AutoGenFactory mockAutoGenFactory;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	CookieProvider mockCookieProvider;
	EvaluationSubmitter mockEvaluationSubmitter;
	FileEntity entity;
	EntityBundle bundle;
	String submitterAlias = "MyAlias";
	
	@Before
	public void setup() throws RestServiceException, JSONObjectAdapterException{	
		mockView = mock(ActionMenuView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockCookieProvider = mock(CookieProvider.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockAutoGenFactory = mock(AutoGenFactory.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockEntityEditor = mock(EntityEditor.class);
		mockEvaluationSubmitter = mock(EvaluationSubmitter.class);
		actionMenu = new ActionMenu(mockView, mockNodeModelCreator, mockAuthenticationController, mockEntityTypeProvider, mockGlobalApplicationState, mockSynapseClient, jSONObjectAdapter, mockEntityEditor, mockAutoGenFactory, mockSynapseJSNIUtils, mockCookieProvider, mockEvaluationSubmitter);
		UserSessionData usd = new UserSessionData();
		UserProfile profile = new UserProfile();
		profile.setOwnerId("test owner ID");
		usd.setProfile(profile);
		
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		
		entity = new FileEntity();
		entity.setVersionNumber(5l);
		entity.setId("file entity test id");
		bundle = new EntityBundle(entity, null, null, null, null, null, null, null);
		
		actionMenu.asWidget(bundle, true, true, null);
	}
	
	@Test
	public void testShowAvailableEvaluations() throws RestServiceException {
		actionMenu.showAvailableEvaluations();
		verify(mockEvaluationSubmitter).configure(any(Entity.class), anySet());
	}
}
