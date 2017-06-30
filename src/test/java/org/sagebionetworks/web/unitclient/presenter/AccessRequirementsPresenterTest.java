package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.presenter.AccessRequirementsPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.LockAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class AccessRequirementsPresenterTest {
	
	AccessRequirementsPresenter presenter;
	@Mock
	PlaceView mockView;
	@Mock
	AccessRequirementsPlace place;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	EntityIdCellRendererImpl mockEntityIdCellRenderer;
	@Mock
	TeamBadge mockTeamBadge;
	
	@Mock
	ManagedACTAccessRequirement mockACTAccessRequirement;
	@Mock
	TermsOfUseAccessRequirement mockTermsOfUseAccessRequirement;
	@Mock
	ACTAccessRequirement mockBasicACTAccessRequirement;
	
	@Mock
	LockAccessRequirement mockLockAccessRequirement;
	@Mock
	AccessRequirementsPlace mockPlace;
	List<AccessRequirement> accessRequirements;
	@Captor
	ArgumentCaptor<RestrictableObjectDescriptor> subjectCaptor;
	@Mock
	ManagedACTAccessRequirementWidget mockACTAccessRequirementWidget;
	@Mock
	ACTAccessRequirementWidget mockBasicACTAccessRequirementWidget;
	@Mock
	TermsOfUseAccessRequirementWidget mockTermsOfUseAccessRequirementWidget;
	@Mock
	LockAccessRequirementWidget mockLockAccessRequirementWidget;
	@Mock
	CreateAccessRequirementButton mockCreateARButton;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	DivView mockEmptyResultsDiv;
	@Mock
	DivView mockUnmetAccessRequirementsDiv;
	@Mock
	DivView mockMetAccessRequirementsDiv;
	public static final String ENTITY_ID = "syn239834";
	public static final String TEAM_ID = "45678";
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		presenter = new AccessRequirementsPresenter(
				mockView, 
				mockDataAccessClient, 
				mockSynAlert, 
				mockGinInjector, 
				mockEntityIdCellRenderer, 
				mockTeamBadge, 
				mockCreateARButton, 
				mockEmptyResultsDiv,
				mockUnmetAccessRequirementsDiv,
				mockMetAccessRequirementsDiv);
		accessRequirements = new ArrayList<AccessRequirement>();
		accessRequirements.add(mockACTAccessRequirement);
		accessRequirements.add(mockTermsOfUseAccessRequirement);
		accessRequirements.add(mockBasicACTAccessRequirement);
		accessRequirements.add(mockLockAccessRequirement);
		AsyncMockStubber.callSuccessWith(accessRequirements).when(mockDataAccessClient).getAccessRequirements(any(RestrictableObjectDescriptor.class), anyLong(), anyLong(), any(AsyncCallback.class));
		when(mockGinInjector.getManagedACTAccessRequirementWidget()).thenReturn(mockACTAccessRequirementWidget);
		when(mockGinInjector.getTermsOfUseAccessRequirementWidget()).thenReturn(mockTermsOfUseAccessRequirementWidget);
		when(mockGinInjector.getACTAccessRequirementWidget()).thenReturn(mockBasicACTAccessRequirementWidget);
		when(mockGinInjector.getLockAccessRequirementWidget()).thenReturn(mockLockAccessRequirementWidget);
	}	
	
	@Test
	public void testConstruction() {
		verify(mockView, atLeastOnce()).add(any(Widget.class));
		verify(mockView, atLeastOnce()).addTitle(any(Widget.class));
		verify(mockView, atLeastOnce()).addAboveBody(any(Widget.class));
	}
	
	@Test
	public void testLoadDataEntity() {
		when(mockPlace.getParam(AccessRequirementsPlace.ENTITY_ID_PARAM)).thenReturn(ENTITY_ID);
		presenter.setPlace(mockPlace);
		verify(mockDataAccessClient).getAccessRequirements(subjectCaptor.capture(), eq(AccessRequirementsPresenter.LIMIT), eq(0L), any(AsyncCallback.class));
		RestrictableObjectDescriptor subject = subjectCaptor.getValue();
		assertEquals(ENTITY_ID, subject.getId());
		assertEquals(RestrictableObjectType.ENTITY, subject.getType());
		verify(mockEntityIdCellRenderer).setValue(ENTITY_ID);
		
		verify(mockACTAccessRequirementWidget).setRequirement(mockACTAccessRequirement);
		verify(mockTermsOfUseAccessRequirementWidget).setRequirement(mockTermsOfUseAccessRequirement);
		verify(mockLockAccessRequirementWidget).setRequirement(mockLockAccessRequirement);
		verify(mockBasicACTAccessRequirementWidget).setRequirement(mockBasicACTAccessRequirement);
		verify(mockEmptyResultsDiv, never()).setVisible(true);
		//load the next page
		verify(mockDataAccessClient).getAccessRequirements(any(RestrictableObjectDescriptor.class), eq(AccessRequirementsPresenter.LIMIT), eq(AccessRequirementsPresenter.LIMIT), any(AsyncCallback.class));
	}
	
	@Test
	public void testLoadDataEntityEmptyResults() {
		accessRequirements.clear();
		when(mockPlace.getParam(AccessRequirementsPlace.ENTITY_ID_PARAM)).thenReturn(ENTITY_ID);
		presenter.setPlace(mockPlace);
		verify(mockDataAccessClient).getAccessRequirements(subjectCaptor.capture(), eq(AccessRequirementsPresenter.LIMIT), eq(0L), any(AsyncCallback.class));
		verify(mockEmptyResultsDiv).setVisible(true);
	}	
	
	@Test
	public void testLoadDataEntityFailure() {
		Exception ex = new Exception("failed");
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).getAccessRequirements(any(RestrictableObjectDescriptor.class), anyLong(), anyLong(), any(AsyncCallback.class));
		when(mockPlace.getParam(AccessRequirementsPlace.ENTITY_ID_PARAM)).thenReturn(ENTITY_ID);
		presenter.setPlace(mockPlace);
		verify(mockSynAlert).handleException(ex);
	}	
	
	@Test
	public void testLoadDataTeam() {
		when(mockPlace.getParam(AccessRequirementsPlace.TEAM_ID_PARAM)).thenReturn(TEAM_ID);
		presenter.setPlace(mockPlace);
		verify(mockDataAccessClient).getAccessRequirements(subjectCaptor.capture(), eq(AccessRequirementsPresenter.LIMIT), eq(0L), any(AsyncCallback.class));
		RestrictableObjectDescriptor subject = subjectCaptor.getValue();
		assertEquals(TEAM_ID, subject.getId());
		assertEquals(RestrictableObjectType.TEAM, subject.getType());
		verify(mockTeamBadge).configure(TEAM_ID);
	}	
}
