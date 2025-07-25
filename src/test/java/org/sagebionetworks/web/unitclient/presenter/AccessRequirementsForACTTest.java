package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
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
import org.sagebionetworks.web.client.presenter.AccessRequirementsForACT;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class AccessRequirementsForACTTest {

  AccessRequirementsForACT presenter;

  @Mock
  PlaceView mockView;

  @Mock
  AccessRequirementsPlace place;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  EntityIdCellRenderer mockEntityIdCellRenderer;

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

  RestrictableObjectDescriptor testSubject;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    presenter =
      new AccessRequirementsForACT(
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
        mockAuthController
      );

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
    AsyncMockStubber
      .callSuccessWith(accessRequirements)
      .when(mockDataAccessClient)
      .getAccessRequirements(
        any(RestrictableObjectDescriptor.class),
        anyLong(),
        anyLong(),
        any(AsyncCallback.class)
      );
    AsyncMockStubber
      .callSuccessWith(mockBatchAccessApprovalInfoResponse)
      .when(mockDataAccessClient)
      .getAccessRequirementStatus(
        any(BatchAccessApprovalInfoRequest.class),
        any(AsyncCallback.class)
      );
    when(mockBatchAccessApprovalInfoResponse.getResults())
      .thenReturn(accessRequirementApprovalStatus);
    when(mockGinInjector.getAccessRequirementWidget())
      .thenReturn(mockAccessRequirementWidget);
    when(mockAuthController.getCurrentUserPrincipalId())
      .thenReturn(CURRENT_USER_ID);
    when(mockAuthController.isLoggedIn()).thenReturn(true);
    testSubject = new RestrictableObjectDescriptor();
    testSubject.setId(ENTITY_ID);
    testSubject.setType(RestrictableObjectType.ENTITY);
  }

  @Test
  public void testConstruction() {
    verify(mockView, atLeastOnce()).add(any());
    verify(mockView, atLeastOnce()).addTitle(anyString());
    verify(mockView, atLeastOnce()).addAboveBody(any());
  }

  @Test
  public void testLoadDataEntity() {
    presenter.configure(testSubject);
    verify(mockDataAccessClient)
      .getAccessRequirements(
        subjectCaptor.capture(),
        eq(AccessRequirementsForACT.LIMIT),
        eq(0L),
        any(AsyncCallback.class)
      );
    verify(mockDataAccessClient)
      .getAccessRequirementStatus(
        any(BatchAccessApprovalInfoRequest.class),
        any(AsyncCallback.class)
      );
    RestrictableObjectDescriptor subject = subjectCaptor.getValue();
    assertEquals(ENTITY_ID, subject.getId());
    assertEquals(RestrictableObjectType.ENTITY, subject.getType());
    verify(mockEntityIdCellRenderer).setValue(ENTITY_ID);

    verify(mockAccessRequirementWidget, times(4))
      .configure(
        any(AccessRequirement.class),
        any(RestrictableObjectDescriptor.class),
        any(Callback.class)
      );

    verify(mockEmptyResultsDiv, never()).setVisible(true);
    verify(mockMetAccessRequirementsDiv, times(2)).add(any(IsWidget.class));
    verify(mockUnmetAccessRequirementsDiv, times(2)).add(any(IsWidget.class));
    // load the next page
    verify(mockDataAccessClient)
      .getAccessRequirements(
        any(RestrictableObjectDescriptor.class),
        eq(AccessRequirementsForACT.LIMIT),
        eq(AccessRequirementsForACT.LIMIT),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testLoadDataEntityEmptyResults() {
    accessRequirements.clear();
    accessRequirementApprovalStatus.clear();

    presenter.configure(testSubject);

    verify(mockDataAccessClient)
      .getAccessRequirements(
        subjectCaptor.capture(),
        eq(AccessRequirementsForACT.LIMIT),
        eq(0L),
        any(AsyncCallback.class)
      );
    verify(mockEmptyResultsDiv).setVisible(true);
  }

  @Test
  public void testLoadDataEntityFailure() {
    Exception ex = new Exception("failed");
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockDataAccessClient)
      .getAccessRequirements(
        any(RestrictableObjectDescriptor.class),
        anyLong(),
        anyLong(),
        any(AsyncCallback.class)
      );

    presenter.configure(testSubject);
    verify(mockSynAlert).handleException(ex);
  }

  @Test
  public void testLoadDataTeam() {
    testSubject.setId(TEAM_ID);
    testSubject.setType(RestrictableObjectType.TEAM);
    presenter.configure(testSubject);
    verify(mockDataAccessClient)
      .getAccessRequirements(
        subjectCaptor.capture(),
        eq(AccessRequirementsForACT.LIMIT),
        eq(0L),
        any(AsyncCallback.class)
      );
    RestrictableObjectDescriptor subject = subjectCaptor.getValue();
    assertEquals(TEAM_ID, subject.getId());
    assertEquals(RestrictableObjectType.TEAM, subject.getType());
    verify(mockTeamBadge).configure(TEAM_ID);
  }

  @Test
  public void testAnonymous() {
    when(mockAuthController.isLoggedIn()).thenReturn(false);

    presenter.configure(testSubject);
    verify(mockDataAccessClient)
      .getAccessRequirements(
        subjectCaptor.capture(),
        eq(AccessRequirementsForACT.LIMIT),
        eq(0L),
        any(AsyncCallback.class)
      );
    verify(mockDataAccessClient, never())
      .getAccessRequirementStatus(
        any(BatchAccessApprovalInfoRequest.class),
        any(AsyncCallback.class)
      );

    verify(mockUnmetAccessRequirementsDiv, times(4)).add(any(IsWidget.class));
  }
}
