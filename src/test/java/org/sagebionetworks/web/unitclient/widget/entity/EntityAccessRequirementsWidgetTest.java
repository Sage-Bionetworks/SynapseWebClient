package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityAccessRequirementsWidget;
import org.sagebionetworks.web.client.widget.entity.EntityAccessRequirementsWidgetView;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityAccessRequirementsWidgetTest {

	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	AuthenticationController mockAuthenticationController;
	EntityAccessRequirementsWidgetView mockView;
	JSONObjectAdapter jsonObjectAdapter;
	String entityId = "syn123";
	EntityAccessRequirementsWidget widget;
	CallbackP<Boolean> mockCallback;
	List<AccessRequirement> accessRequirements;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockCallback = mock(CallbackP.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);		
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(EntityAccessRequirementsWidgetView.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		
		//mock access requirements
		PaginatedResults ar = mock(PaginatedResults.class);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), eq(TermsOfUseAccessRequirement.class))).thenReturn(ar);
		accessRequirements = new ArrayList<AccessRequirement>();
		when(ar.getResults()).thenReturn(accessRequirements);
		
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getAllEntityUploadAccessRequirements(anyString(), any(AsyncCallback.class));
		widget = new EntityAccessRequirementsWidget(mockView, mockSynapseClient, mockAuthenticationController, mockNodeModelCreator, jsonObjectAdapter);
	}
	
	
	@Test
	public void testShowUploadAccessRequirementsNotLoggedIn() throws Exception {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.showUploadAccessRequirements(entityId, mockCallback);
		//should return false, indicating access requirements were not accepted
		verify(mockCallback).invoke(eq(false));
	}

	
	@Test
	public void testShowUploadAccessRequirementsStep2Failure() throws Exception {
		String errorMessage= "something went wrong";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).getAllEntityUploadAccessRequirements(anyString(), any(AsyncCallback.class));
		widget.showUploadAccessRequirements(entityId, mockCallback);
		//should show error
		verify(mockView).showErrorMessage(eq(errorMessage));
		//should return false, indicating access requirements were not accepted
		verify(mockCallback).invoke(eq(false));
	}
	
	@Test
	public void testNoAccessRequirements() throws Exception {
		widget.showUploadAccessRequirements(entityId, mockCallback);
		verify(mockView).hideWizard();
		verify(mockCallback).invoke(eq(true));
	}
	
	@Test
	public void testWithAccessRequirements() throws Exception {
		TermsOfUseAccessRequirement tou = new TermsOfUseAccessRequirement();
		String arText = "These are the terms";
		tou.setTermsOfUse(arText);
		accessRequirements.add(tou);
		widget.showUploadAccessRequirements(entityId, mockCallback);
		
		verify(mockView).updateWizardProgress(eq(0), eq(accessRequirements.size()));
		verify(mockView).showAccessRequirement(eq(arText), any(Callback.class));
	}
	
	@Test
	public void testWizardCanceled() {
		widget.showUploadAccessRequirements(entityId, mockCallback);
		widget.wizardCanceled();
		verify(mockCallback).invoke(eq(false));
	}
	
}
