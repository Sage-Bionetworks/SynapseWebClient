package org.sagebionetworks.web.unitclient.widget.entity.menu;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.Code;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.RestResourceList;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;

import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
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
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBarView;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenu;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenuView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

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
		actionMenu = new ActionMenu(mockView, mockNodeModelCreator, mockAuthenticationController, mockEntityTypeProvider, mockGlobalApplicationState, mockSynapseClient, jSONObjectAdapter, mockEntityEditor, mockAutoGenFactory, mockSynapseJSNIUtils, mockCookieProvider);
		UserSessionData usd = new UserSessionData();
		UserProfile profile = new UserProfile();
		profile.setOwnerId("test owner ID");
		usd.setProfile(profile);
		
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		
		AsyncMockStubber.callSuccessWith("fake submission result json").when(mockSynapseClient).createSubmission(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("fake submitter alias results json").when(mockSynapseClient).getAvailableEvaluationsSubmitterAliases(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("fake evaluation results json").when(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		availableEvaluations.setTotalNumberOfResults(2);
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		Evaluation e1 = new Evaluation();
		e1.setId("1");
		e1.setName("Test Evaluation 1");
		evaluationList.add(e1);
		Evaluation e2 = new Evaluation();
		e1.setId("2");
		e1.setName("Test Evaluation 2");
		evaluationList.add(e2);
		availableEvaluations.setResults(evaluationList);

		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(availableEvaluations);
		
		RestResourceList submitterAliases = new RestResourceList();
		List<String> submitterAliasList = new ArrayList<String>();
		submitterAliasList.add("Mr. F");
		submitterAliases.setList(submitterAliasList);
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(RestResourceList.class))).thenReturn(submitterAliases);
		
		entity = new FileEntity();
		entity.setVersionNumber(5l);
		entity.setId("file entity test id");
		bundle = new EntityBundle(entity, null, null, null, null, null, null, null);
		
		actionMenu.asWidget(bundle, true, true, null);
	}
	
	@Test
	public void testSubmitToEvaluations() throws RestServiceException {
		List<String> evalIds = new ArrayList<String>();
		
		actionMenu.submitToEvaluations(evalIds, submitterAlias);
		verify(mockSynapseClient, times(0)).createSubmission(anyString(), anyString(), any(AsyncCallback.class));
		evalIds.add("test evaluation id");
		actionMenu.submitToEvaluations(evalIds, submitterAlias);
		verify(mockSynapseClient).createSubmission(anyString(), anyString(), any(AsyncCallback.class));
		//submitted status shown
		verify(mockView).showInfo(anyString(), anyString());
	}
	
	@Test
	public void testSubmitToEvaluationsFailure() throws RestServiceException {
		List<String> evalIds = new ArrayList<String>();
		AsyncMockStubber.callFailureWith(new ForbiddenException()).when(mockSynapseClient).createSubmission(anyString(), anyString(), any(AsyncCallback.class));
		evalIds.add("test evaluation id");
		actionMenu.submitToEvaluations(evalIds, submitterAlias);
		verify(mockSynapseClient).createSubmission(anyString(), anyString(), any(AsyncCallback.class));
		//submitted status shown
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testShowAvailableEvaluations() throws RestServiceException {
		actionMenu.showAvailableEvaluations();
		verify(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		verify(mockView).popupEvaluationSelector(any(List.class), any(List.class));
	}
	
	@Test
	public void testShowAvailableEvaluationsNoResults() throws RestServiceException, JSONObjectAdapterException {
		//mock empty evaluation list
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		availableEvaluations.setTotalNumberOfResults(0);
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		availableEvaluations.setResults(evaluationList);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(availableEvaluations);
		actionMenu.showAvailableEvaluations();
		verify(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		//no evaluations to join error message
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testShowAvailableEvaluationsFailure1() throws RestServiceException, JSONObjectAdapterException {
		AsyncMockStubber.callFailureWith(new ForbiddenException()).when(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		actionMenu.showAvailableEvaluations();
		verify(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		//no evaluations to join error message
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testShowAvailableEvaluationsFailure2() throws RestServiceException, JSONObjectAdapterException {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getAvailableEvaluationsSubmitterAliases(any(AsyncCallback.class));
		actionMenu.showAvailableEvaluations();
		verify(mockSynapseClient).getAvailableEvaluationsSubmitterAliases(any(AsyncCallback.class));
		//Failure when asking for submitter aliases
		verify(mockView).showErrorMessage(anyString());
	}
}
