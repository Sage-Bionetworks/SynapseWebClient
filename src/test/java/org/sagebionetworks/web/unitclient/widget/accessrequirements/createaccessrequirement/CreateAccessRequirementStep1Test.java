package org.sagebionetworks.web.unitclient.widget.accessrequirements.createaccessrequirement;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateACTAccessRequirementStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementStep1;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementStep1View;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateTermsOfUseAccessRequirementStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectStep1;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectWizardStep1View;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class CreateAccessRequirementStep1Test {
	
	CreateAccessRequirementStep1 widget;
	@Mock
	CreateAccessRequirementStep1View mockView;
	@Mock
	CreateACTAccessRequirementStep2 mockActStep2;
	@Mock
	CreateTermsOfUseAccessRequirementStep2 mockTouStep2;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	ModalPresenter mockModalPresenter;
	
	@Mock
	ACTAccessRequirement mockACTAccessRequirement;
	@Mock
	TermsOfUseAccessRequirement mockTermsOfUseAccessRequirement;
	@Mock
	EntityIdCellRenderer mockEntityIdCellRenderer;
	@Mock
	TeamBadge mockTeamBadge;
	
	@Mock
	RestrictableObjectDescriptor mockEntityRestrictableObjectDescriptor;
	@Mock
	RestrictableObjectDescriptor mockTeamRestrictableObjectDescriptor;
	
	@Captor
	ArgumentCaptor<AccessRequirement> arCaptor;
	
	public static final String VIEW_TEAM_ID1 = "5678";
	public static final String VIEW_TEAM_ID2 = "8765";
	public static final String VIEW_TEAM_IDS = VIEW_TEAM_ID1 + ", " + VIEW_TEAM_ID2;

	public static final String VIEW_ENTITY_ID1 = "syn97";
	public static final String VIEW_ENTITY_ID2 = "syn79";
	public static final String VIEW_ENTITY_IDS = VIEW_ENTITY_ID1 + ", " + VIEW_ENTITY_ID2;
	
	public static final String ROD_ENTITY_ID = "syn97992";
	public static final String ROD_TEAM_ID = "87654";
	
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new CreateAccessRequirementStep1(mockView, mockActStep2, mockTouStep2, mockGinInjector, mockSynapseClient);
		widget.setModalPresenter(mockModalPresenter);
		when(mockGinInjector.createEntityIdCellRenderer()).thenReturn(mockEntityIdCellRenderer);
		when(mockGinInjector.getTeamBadgeWidget()).thenReturn(mockTeamBadge);
		when(mockView.getTeamIds()).thenReturn(VIEW_TEAM_IDS);
		when(mockView.getEntityIds()).thenReturn(VIEW_ENTITY_IDS);
		when(mockEntityRestrictableObjectDescriptor.getType()).thenReturn(RestrictableObjectType.ENTITY);
		when(mockEntityRestrictableObjectDescriptor.getId()).thenReturn(ROD_ENTITY_ID);
		when(mockTeamRestrictableObjectDescriptor.getType()).thenReturn(RestrictableObjectType.TEAM);
		when(mockTeamRestrictableObjectDescriptor.getId()).thenReturn(ROD_TEAM_ID);
		AsyncMockStubber.callSuccessWith(mockACTAccessRequirement).when(mockSynapseClient).createOrUpdateAccessRequirement(any(AccessRequirement.class),  any(AsyncCallback.class));
		
		when(mockView.isACTAccessRequirementType()).thenReturn(true);
		when(mockACTAccessRequirement.getSubjectIds()).thenReturn(new ArrayList<RestrictableObjectDescriptor>());
		when(mockTermsOfUseAccessRequirement.getSubjectIds()).thenReturn(new ArrayList<RestrictableObjectDescriptor>());
	}

	@Test
	public void testConfigureWithEntityRod() {
		widget.configure(mockEntityRestrictableObjectDescriptor);
		verify(mockView).clearSubjects();
		verify(mockGinInjector).createEntityIdCellRenderer();
		verify(mockEntityIdCellRenderer).setValue(ROD_ENTITY_ID);
		verify(mockView).addSubject(any(IsWidget.class));
		
		//go to the next page
		widget.onPrimary();
		verify(mockSynapseClient).createOrUpdateAccessRequirement(arCaptor.capture(),  any(AsyncCallback.class));
		AccessRequirement ar = arCaptor.getValue();
		// in setUp, we have the view tell us that ACT is selected.
		assertTrue(ar instanceof ACTAccessRequirement);
		assertEquals(ACCESS_TYPE.DOWNLOAD, ar.getAccessType());
		assertEquals(1, ar.getSubjectIds().size());
		assertEquals(mockEntityRestrictableObjectDescriptor, ar.getSubjectIds().get(0));
		
		verify(mockActStep2).configure(mockACTAccessRequirement);
		verify(mockModalPresenter).setNextActivePage(mockActStep2);
	}
	
	@Test
	public void testConfigureWithTeamRod() {
		widget.configure(mockTeamRestrictableObjectDescriptor);
		verify(mockView).clearSubjects();
		verify(mockGinInjector).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(ROD_TEAM_ID);
		verify(mockView).addSubject(any(IsWidget.class));
		
		when(mockView.isACTAccessRequirementType()).thenReturn(false);
		AsyncMockStubber.callSuccessWith(mockTermsOfUseAccessRequirement).when(mockSynapseClient).createOrUpdateAccessRequirement(any(AccessRequirement.class),  any(AsyncCallback.class));
		
		//go to the next page
		widget.onPrimary();
		verify(mockSynapseClient).createOrUpdateAccessRequirement(arCaptor.capture(),  any(AsyncCallback.class));
		AccessRequirement ar = arCaptor.getValue();
		// in here, we have the view tell us that TermsOfUse was selected (not ACT).
		assertTrue(ar instanceof TermsOfUseAccessRequirement);
		assertEquals(ACCESS_TYPE.PARTICIPATE, ar.getAccessType());
		assertEquals(1, ar.getSubjectIds().size());
		assertEquals(mockTeamRestrictableObjectDescriptor, ar.getSubjectIds().get(0));
		
		verify(mockTouStep2).configure(mockTermsOfUseAccessRequirement);
		verify(mockModalPresenter).setNextActivePage(mockTouStep2);
	}
	
	@Test
	public void testConfigureWithACTAccessRequirement() {
		widget.configure(mockACTAccessRequirement);
		// on save, we should be updating the ar we passed in
		widget.onPrimary();
		verify(mockACTAccessRequirement).setAccessType(any(ACCESS_TYPE.class));
		verify(mockACTAccessRequirement).setSubjectIds(anyList());
		verify(mockSynapseClient).createOrUpdateAccessRequirement(eq(mockACTAccessRequirement),  any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigureWithToUAccessRequirement() {
		widget.configure(mockTermsOfUseAccessRequirement);
		// on save, we should be updating the ar we passed in
		//also verify any errors are shown
		String error = "error occurred";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).createOrUpdateAccessRequirement(any(AccessRequirement.class),  any(AsyncCallback.class));
		
		widget.onPrimary();
		verify(mockTermsOfUseAccessRequirement).setAccessType(any(ACCESS_TYPE.class));
		verify(mockTermsOfUseAccessRequirement).setSubjectIds(anyList());
		verify(mockSynapseClient).createOrUpdateAccessRequirement(eq(mockTermsOfUseAccessRequirement),  any(AsyncCallback.class));
		verify(mockModalPresenter).setErrorMessage(error);
	}
	
	@Test
	public void testSetEntitiesFromView() {
		widget.onSetEntities();
		verify(mockGinInjector, times(2)).createEntityIdCellRenderer();
		verify(mockEntityIdCellRenderer).setValue(VIEW_ENTITY_ID1);
		verify(mockEntityIdCellRenderer).setValue(VIEW_ENTITY_ID2);
	}
	
	@Test
	public void testSetTeamsFromView() {
		widget.onSetTeams();
		verify(mockGinInjector, times(2)).getTeamBadgeWidget();
		verify(mockTeamBadge).configure(VIEW_TEAM_ID1);
		verify(mockTeamBadge).configure(VIEW_TEAM_ID2);
	}
}
