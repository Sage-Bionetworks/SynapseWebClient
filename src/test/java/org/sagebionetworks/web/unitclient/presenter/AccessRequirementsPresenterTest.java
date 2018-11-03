package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.sagebionetworks.repo.model.AccessApprovalInfo;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.BatchAccessApprovalInfoRequest;
import org.sagebionetworks.repo.model.BatchAccessApprovalInfoResponse;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.presenter.AccessRequirementsPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
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
	List<AccessApprovalInfo> accessRequirementApprovalStatus;
	@Captor
	ArgumentCaptor<RestrictableObjectDescriptor> subjectCaptor;
	@Mock
	AccessRequirementWidget mockAccessRequirementWidget;
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
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	BatchAccessApprovalInfoResponse mockBatchAccessApprovalInfoResponse;
	
	public static final String ENTITY_ID = "syn239834";
	public static final String TEAM_ID = "45678";
	public static final String CURRENT_USER_ID = "11111";
	
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
				mockMetAccessRequirementsDiv,
				mockAuthController);
		
		accessRequirements = new ArrayList<AccessRequirement>();
		accessRequirementApprovalStatus = new ArrayList<AccessApprovalInfo>();
		accessRequirements.add(mockACTAccessRequirement);
		AccessApprovalInfo status = new AccessApprovalInfo();
		status.setHasAccessApproval(true);
		accessRequirementApprovalStatus.add(status);
		accessRequirements.add(mockTermsOfUseAccessRequirement);
		accessRequirementApprovalStatus.add(status);
		accessRequirements.add(mockBasicACTAccessRequirement);
		status = new AccessApprovalInfo();
		status.setHasAccessApproval(false);
		accessRequirementApprovalStatus.add(status);
		accessRequirements.add(mockLockAccessRequirement);
		accessRequirementApprovalStatus.add(status);
		AsyncMockStubber.callSuccessWith(accessRequirements).when(mockDataAccessClient).getAccessRequirements(any(RestrictableObjectDescriptor.class), anyLong(), anyLong(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockBatchAccessApprovalInfoResponse).when(mockDataAccessClient).getAccessRequirementStatus(any(BatchAccessApprovalInfoRequest.class), any(AsyncCallback.class));
		when(mockBatchAccessApprovalInfoResponse.getResults()).thenReturn(accessRequirementApprovalStatus);
		when(mockGinInjector.getAccessRequirementWidget()).thenReturn(mockAccessRequirementWidget);
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(CURRENT_USER_ID);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
	}	
	
	@Test
	public void testConstruction() {
		verify(mockView, atLeastOnce()).add(any(Widget.class));
		verify(mockView, atLeastOnce()).addTitle(any(Widget.class));
		verify(mockView, atLeastOnce()).addAboveBody(any(Widget.class));
	}
	
	@Test
	public void testLoadDataEntity() {
		when(mockPlace.getParam(AccessRequirementsPlace.ID_PARAM)).thenReturn(ENTITY_ID);
		when(mockPlace.getParam(AccessRequirementsPlace.TYPE_PARAM)).thenReturn(RestrictableObjectType.ENTITY.toString());

		presenter.setPlace(mockPlace);
		verify(mockDataAccessClient).getAccessRequirements(subjectCaptor.capture(), eq(AccessRequirementsPresenter.LIMIT), eq(0L), any(AsyncCallback.class));
		verify(mockDataAccessClient).getAccessRequirementStatus(any(BatchAccessApprovalInfoRequest.class), any(AsyncCallback.class));
		RestrictableObjectDescriptor subject = subjectCaptor.getValue();
		assertEquals(ENTITY_ID, subject.getId());
		assertEquals(RestrictableObjectType.ENTITY, subject.getType());
		verify(mockEntityIdCellRenderer).setValue(ENTITY_ID);
		
		verify(mockAccessRequirementWidget, times(4)).configure(any(AccessRequirement.class), any(RestrictableObjectDescriptor.class), any(Callback.class));

		verify(mockEmptyResultsDiv, never()).setVisible(true);
		verify(mockMetAccessRequirementsDiv, times(2)).add(any(IsWidget.class));
		verify(mockUnmetAccessRequirementsDiv, times(2)).add(any(IsWidget.class));
		//load the next page
		verify(mockDataAccessClient).getAccessRequirements(any(RestrictableObjectDescriptor.class), eq(AccessRequirementsPresenter.LIMIT), eq(AccessRequirementsPresenter.LIMIT), any(AsyncCallback.class));
	}
	
	@Test
	public void testLoadDataEntityEmptyResults() {
		accessRequirements.clear();
		accessRequirementApprovalStatus.clear();
		when(mockPlace.getParam(AccessRequirementsPlace.ID_PARAM)).thenReturn(ENTITY_ID);
		when(mockPlace.getParam(AccessRequirementsPlace.TYPE_PARAM)).thenReturn(RestrictableObjectType.ENTITY.toString());
		presenter.setPlace(mockPlace);
		verify(mockDataAccessClient).getAccessRequirements(subjectCaptor.capture(), eq(AccessRequirementsPresenter.LIMIT), eq(0L), any(AsyncCallback.class));
		verify(mockEmptyResultsDiv).setVisible(true);
	}	
	
	@Test
	public void testLoadDataEntityFailure() {
		Exception ex = new Exception("failed");
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).getAccessRequirements(any(RestrictableObjectDescriptor.class), anyLong(), anyLong(), any(AsyncCallback.class));
		when(mockPlace.getParam(AccessRequirementsPlace.ID_PARAM)).thenReturn(ENTITY_ID);
		when(mockPlace.getParam(AccessRequirementsPlace.TYPE_PARAM)).thenReturn(RestrictableObjectType.ENTITY.toString());

		presenter.setPlace(mockPlace);
		verify(mockSynAlert).handleException(ex);
	}	
	
	@Test
	public void testLoadDataTeam() {
		when(mockPlace.getParam(AccessRequirementsPlace.ID_PARAM)).thenReturn(TEAM_ID);
		when(mockPlace.getParam(AccessRequirementsPlace.TYPE_PARAM)).thenReturn(RestrictableObjectType.TEAM.toString());
		presenter.setPlace(mockPlace);
		verify(mockDataAccessClient).getAccessRequirements(subjectCaptor.capture(), eq(AccessRequirementsPresenter.LIMIT), eq(0L), any(AsyncCallback.class));
		RestrictableObjectDescriptor subject = subjectCaptor.getValue();
		assertEquals(TEAM_ID, subject.getId());
		assertEquals(RestrictableObjectType.TEAM, subject.getType());
		verify(mockTeamBadge).configure(TEAM_ID);
	}	
	
	@Test
	public void testAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		when(mockPlace.getParam(AccessRequirementsPlace.ID_PARAM)).thenReturn(ENTITY_ID);
		when(mockPlace.getParam(AccessRequirementsPlace.TYPE_PARAM)).thenReturn(RestrictableObjectType.ENTITY.toString());

		presenter.setPlace(mockPlace);
		verify(mockDataAccessClient).getAccessRequirements(subjectCaptor.capture(), eq(AccessRequirementsPresenter.LIMIT), eq(0L), any(AsyncCallback.class));
		verify(mockDataAccessClient, never()).getAccessRequirementStatus(any(BatchAccessApprovalInfoRequest.class), any(AsyncCallback.class));
		
		verify(mockUnmetAccessRequirementsDiv, times(4)).add(any(IsWidget.class));
	}
}
