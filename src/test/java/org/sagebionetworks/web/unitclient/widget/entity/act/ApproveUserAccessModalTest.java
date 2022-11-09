package org.sagebionetworks.web.unitclient.widget.entity.act;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.APPROVED_USER;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.APPROVE_BUT_FAIL_TO_EMAIL;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.EMAIL_SENT;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.MESSAGE_BLANK;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.NO_COMPATIBLE_ARS_MESSAGE;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.NO_EMAIL_MESSAGE;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.NO_USER_SELECTED;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.QUERY_CANCELLED;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.REVOKED_USER;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal;
import org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.asynch.AsynchType;

public class ApproveUserAccessModalTest {

  ApproveUserAccessModal dialog;

  @Mock
  ApproveUserAccessModalView mockView;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  SynapseSuggestBox mockPeopleSuggestWidget;

  @Mock
  UserGroupSuggestionProvider mockProvider;

  @Mock
  SynapseClientAsync mockSynapseClient;

  @Mock
  DataAccessClientAsync mockDataAccessClient;

  @Mock
  SynapseProperties mockSynapseProperties;

  @Mock
  JobTrackingWidget mockProgressWidget;

  @Mock
  UserGroupSuggestion mockUser;

  @Mock
  EntityBundle mockEntityBundle;

  @Mock
  Entity mockEntity;

  @Mock
  QueryResultBundle mockQrb;

  @Mock
  QueryResult mockQr;

  @Mock
  RowSet mockRs;

  @Mock
  List<Row> mockLr;

  @Mock
  Row mockR;

  @Mock
  List<String> mockLs;

  @Mock
  AccessApproval mockAccessApproval;

  @Mock
  List<AccessApproval> mockAccAppList;

  @Mock
  Iterator<AccessApproval> mockAccAppItr;

  @Mock
  AccessRequirementWidget mockArWidget;

  @Captor
  ArgumentCaptor<AsyncCallback<AccessApproval>> aaCaptor;

  @Captor
  ArgumentCaptor<List<String>> stringListCaptor;

  @Captor
  ArgumentCaptor<AsynchronousProgressHandler> phCaptor;

  @Captor
  ArgumentCaptor<AsyncCallback<String>> sCaptor;

  @Captor
  ArgumentCaptor<AsyncCallback<Void>> vCaptor;

  @Mock
  ManagedACTAccessRequirement mockManagedACTAccessRequirement;

  @Mock
  ACTAccessRequirement mockACTAccessRequirement;

  @Mock
  SelfSignAccessRequirement mockSelfSignAccessRequirement;

  @Mock
  PopupUtilsView mockPopupUtils;

  RestrictableObjectDescriptor expectedRestrictableObjectDescriptor = new RestrictableObjectDescriptor();
  Long accessReq;
  String userId;
  String message;
  List<AccessRequirement> accessRequirementList;
  Exception ex;
  public static final String ENTITY_ID = "syn1011938";
  public static final Long ACT_ACCESS_REQUIREMENT_ID = 51L;
  public static final Long MANAGED_ACT_ACCESS_REQUIREMENT_ID = 52L;
  public static final Long SELF_SIGN_ACCESS_REQUIREMENT_ID = 53L;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    dialog =
      new ApproveUserAccessModal(
        mockView,
        mockArWidget,
        mockSynAlert,
        mockPeopleSuggestWidget,
        mockProvider,
        mockSynapseClient,
        mockSynapseProperties,
        mockProgressWidget,
        mockDataAccessClient,
        mockPopupUtils
      );
    when(mockSynapseProperties.getSynapseProperty(anyString()))
      .thenReturn("syn7444807");

    message = "Message";
    userId = "1234567";
    accessReq = 123L;
    ex = new Exception("error message");

    when(mockACTAccessRequirement.getId())
      .thenReturn(ACT_ACCESS_REQUIREMENT_ID);
    when(mockManagedACTAccessRequirement.getId())
      .thenReturn(MANAGED_ACT_ACCESS_REQUIREMENT_ID);
    when(mockSelfSignAccessRequirement.getId())
      .thenReturn(SELF_SIGN_ACCESS_REQUIREMENT_ID);

    accessRequirementList = new ArrayList<AccessRequirement>();
    accessRequirementList.add(mockACTAccessRequirement);
    accessRequirementList.add(mockManagedACTAccessRequirement);
    accessRequirementList.add(mockSelfSignAccessRequirement);

    when(mockQrb.getQueryResult()).thenReturn(mockQr);
    when(mockQr.getQueryResults()).thenReturn(mockRs);
    when(mockRs.getRows()).thenReturn(mockLr);
    when(mockLr.size()).thenReturn(1);
    when(mockLr.get(0)).thenReturn(mockR);
    when(mockR.getValues()).thenReturn(mockLs);
    when(mockLs.size()).thenReturn(1);
    when(mockLs.get(0)).thenReturn(message);

    when(mockEntityBundle.getEntity()).thenReturn(mockEntity);
    when(mockUser.getId()).thenReturn(userId);
    when(mockView.getAccessRequirement()).thenReturn(Long.toString(accessReq));
    when(mockView.getEmailMessage()).thenReturn(message);

    when(mockAccAppList.iterator()).thenReturn(mockAccAppItr);
    when(mockAccAppItr.hasNext()).thenReturn(true);
    when(mockAccAppItr.next()).thenReturn(mockAccessApproval);
    when(mockAccessApproval.getRequirementId()).thenReturn(accessReq);
    when(mockAccessApproval.getAccessorId()).thenReturn(userId);
    when(mockEntity.getId()).thenReturn(ENTITY_ID);
    expectedRestrictableObjectDescriptor.setType(RestrictableObjectType.ENTITY);
    expectedRestrictableObjectDescriptor.setId(ENTITY_ID);
  }

  @Test
  public void testConfigureNoAccessReqs() {
    List<AccessRequirement> accessReqs = new ArrayList<AccessRequirement>();
    dialog.configure(accessReqs, mockEntityBundle);
    verify(mockView, times(0)).setAccessRequirement(anyString());
  }

  @Test
  public void testConfigureOneAccessReq() {
    dialog.configure(
      Collections.singletonList(mockACTAccessRequirement),
      mockEntityBundle
    );
    verify(mockArWidget)
      .configure(
        ACT_ACCESS_REQUIREMENT_ID.toString(),
        expectedRestrictableObjectDescriptor
      );
    verify(mockView).setAccessRequirement(ACT_ACCESS_REQUIREMENT_ID.toString());
    verify(mockView).setDatasetTitle(mockEntityBundle.getEntity().getName());
  }

  @Test
  public void testConfigureFilterManagedACTAccessRequirements() {
    dialog.configure(accessRequirementList, mockEntityBundle);

    verify(mockView).setAccessRequirementIDs(stringListCaptor.capture());
    List<String> arIDs = stringListCaptor.getValue();
    // verify managed act AR ID was not included
    assertTrue(arIDs.contains(ACT_ACCESS_REQUIREMENT_ID.toString()));
    assertTrue(arIDs.contains(SELF_SIGN_ACCESS_REQUIREMENT_ID.toString()));
    assertFalse(arIDs.contains(MANAGED_ACT_ACCESS_REQUIREMENT_ID.toString()));
  }

  @Test
  public void testLoadEmailMessageOnFailure() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onFailure(ex);
    verify(mockView).setLoadingEmailVisible(false);
    verify(mockSynAlert).handleException(ex);
  }

  @Test
  public void testLoadEmailMessageOnCancel() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onCancel();
    verify(mockView).setLoadingEmailVisible(false);
    verify(mockSynAlert).showError(QUERY_CANCELLED);
  }

  @Test
  public void testLoadEmailMessageOnComplete() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onComplete(mockQrb);
    verify(mockView).finishLoadingEmail();
  }

  @Test
  public void testOnAccessRequirementSelected() {
    AccessRequirement ar = accessRequirementList.get(0);
    String num = Long.toString(ar.getId());
    dialog.configure(accessRequirementList, mockEntityBundle);
    dialog.onAccessRequirementIDSelected(num);
    verify(mockView, times(2)).setAccessRequirement(eq(num));
  }

  @Test
  public void testOnSubmitNoUserSelected() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    dialog.onSubmit();
    verify(mockSynAlert).showError(eq(NO_USER_SELECTED));
  }

  @Test
  public void testOnSubmitQueryCancelledEditedEmail() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onCancel();
    verify(mockView).setLoadingEmailVisible(false);
    verify(mockSynAlert).showError(QUERY_CANCELLED);

    when(mockView.getEmailMessage()).thenReturn("Updated email message");
    dialog.onUserSelected(mockUser);
    dialog.onSubmit();
    verify(mockSynAlert, times(1)).showError(anyString()); // only shows query cancelled error
  }

  @Test
  public void testOnSubmitQueryFailedEditedEmail() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onFailure(ex);
    verify(mockView).setLoadingEmailVisible(false);
    verify(mockSynAlert).handleException(ex);

    when(mockView.getEmailMessage()).thenReturn("Updated email message");
    dialog.onUserSelected(mockUser);
    dialog.onSubmit();
    verify(mockSynAlert, times(0)).showError(anyString());
  }

  @Test
  public void testOnSubmitNoMessage() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    when(mockSynapseProperties.getSynapseProperty(anyString()))
      .thenReturn(null);
    when(mockView.getEmailMessage()).thenReturn("");
    dialog.onUserSelected(mockUser);
    dialog.onSubmit();
    verify(mockSynAlert, times(0)).showError(eq(NO_USER_SELECTED));
    verify(mockSynAlert).showError(MESSAGE_BLANK);
  }

  @Test
  public void testOnSubmitNullAccessReq() {
    dialog.configure(accessRequirementList, mockEntityBundle);

    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onComplete(mockQrb);

    dialog.onUserSelected(mockUser);
    dialog.onSubmit();
    verify(mockSynAlert, times(0)).showError(anyString());
    verify(mockView).getAccessRequirement();
    verify(mockView).setApproveProcessing(true);
  }

  @Test
  public void testOnSubmitOnFailure() {
    dialog.configure(accessRequirementList, mockEntityBundle);

    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onComplete(mockQrb);
    verify(mockView).setMessageEditArea(message);

    dialog.onUserSelected(mockUser);
    dialog.onSubmit();
    verify(mockSynAlert, times(0)).showError(anyString());
    verify(mockView).getAccessRequirement();
    verify(mockView).setApproveProcessing(true);

    verify(mockSynapseClient)
      .createAccessApproval(any(AccessApproval.class), aaCaptor.capture());
    aaCaptor.getValue().onFailure(ex);
    verify(mockSynAlert).handleException(ex);
  }

  @Test
  public void testOnSubmitOnSuccess() {
    dialog.configure(accessRequirementList, mockEntityBundle);

    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onComplete(mockQrb);

    dialog.onUserSelected(mockUser);
    dialog.onSubmit();
    verify(mockSynAlert, times(0)).showError(anyString());
    verify(mockView).getAccessRequirement();
    verify(mockView).setApproveProcessing(true);

    verify(mockSynapseClient)
      .createAccessApproval(any(AccessApproval.class), aaCaptor.capture());
    aaCaptor.getValue().onSuccess(mockAccessApproval);
    verify(mockSynapseClient)
      .sendMessage(
        anySetOf(String.class),
        anyString(),
        anyString(),
        anyString(),
        sCaptor.capture()
      );
  }

  @Test
  public void testOnSubmitSendMessageOnFailure() {
    dialog.configure(accessRequirementList, mockEntityBundle);

    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onComplete(mockQrb);

    dialog.onUserSelected(mockUser);
    dialog.onSubmit();
    verify(mockSynAlert, times(0)).showError(NO_USER_SELECTED);
    verify(mockSynAlert, times(0)).showError(NO_EMAIL_MESSAGE);
    verify(mockView).getAccessRequirement();
    verify(mockView).setApproveProcessing(true);

    verify(mockSynapseClient)
      .createAccessApproval(any(AccessApproval.class), aaCaptor.capture());
    aaCaptor.getValue().onSuccess(mockAccessApproval);

    verify(mockSynapseClient)
      .sendMessage(
        anySetOf(String.class),
        anyString(),
        anyString(),
        anyString(),
        sCaptor.capture()
      );
    sCaptor.getValue().onFailure(ex);

    verify(mockView).setApproveProcessing(false);
    verify(mockSynAlert).showError(APPROVE_BUT_FAIL_TO_EMAIL + ex.getMessage());
  }

  @Test
  public void testOnSubmitSendMessageOnSuccess() {
    dialog.configure(accessRequirementList, mockEntityBundle);

    verify(mockProgressWidget)
      .startAndTrackJob(
        anyString(),
        anyBoolean(),
        any(AsynchType.class),
        any(QueryBundleRequest.class),
        phCaptor.capture()
      );
    phCaptor.getValue().onComplete(mockQrb);

    dialog.onUserSelected(mockUser);
    dialog.onSubmit();
    verify(mockSynAlert, times(0)).showError(anyString());
    verify(mockView).getAccessRequirement();
    verify(mockView).setApproveProcessing(true);

    verify(mockSynapseClient)
      .createAccessApproval(any(AccessApproval.class), aaCaptor.capture());
    aaCaptor.getValue().onSuccess(mockAccessApproval);

    verify(mockSynapseClient)
      .sendMessage(
        anySetOf(String.class),
        anyString(),
        anyString(),
        anyString(),
        sCaptor.capture()
      );
    sCaptor.getValue().onSuccess(anyString());

    verify(mockView).setApproveProcessing(false);
    verify(mockView).hide();
    verify(mockView).showInfo(APPROVED_USER + EMAIL_SENT);
  }

  @Test
  public void testOnRevokeNoUser() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    dialog.onRevoke();
    verify(mockSynAlert).showError(NO_USER_SELECTED);
  }

  @Test
  public void testOnRevokeGetAccessApprovalFailure() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    dialog.onUserSelected(mockUser);
    dialog.onRevoke();

    verify(mockView).setRevokeProcessing(true);
    verify(mockSynapseClient)
      .deleteAccessApprovals(anyString(), anyString(), vCaptor.capture());
    vCaptor.getValue().onFailure(ex);
    verify(mockSynAlert).handleException(ex);
    verify(mockView).setRevokeProcessing(false);
  }

  @Test
  public void testOnRevokeGetAccessApprovalSuccessNoMatch() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    dialog.onUserSelected(mockUser);
    dialog.onRevoke();

    verify(mockView).setRevokeProcessing(true);
    verify(mockSynapseClient)
      .deleteAccessApprovals(anyString(), anyString(), vCaptor.capture());
    vCaptor.getValue().onSuccess((Void) null);
    verify(mockView).setRevokeProcessing(false);
    verify(mockView).hide();
    verify(mockView).showInfo(REVOKED_USER);
  }

  @Test
  public void testOnRevokeGetAccessApprovalSuccessMatchFound() {
    dialog.configure(accessRequirementList, mockEntityBundle);
    dialog.onUserSelected(mockUser);
    dialog.onRevoke();

    verify(mockView).setRevokeProcessing(true);
    verify(mockSynapseClient)
      .deleteAccessApprovals(anyString(), anyString(), vCaptor.capture());
    vCaptor.getValue().onSuccess((Void) null);
    verify(mockView).setRevokeProcessing(false);
    verify(mockView).hide();
    verify(mockView).showInfo(REVOKED_USER);
  }

  @Test
  public void testShowErrorOnNoCompatibleAccessRequirements() {
    accessRequirementList.clear();
    accessRequirementList.add(mockManagedACTAccessRequirement);
    dialog.configure(accessRequirementList, mockEntityBundle);

    verify(mockPopupUtils).showErrorMessage(NO_COMPATIBLE_ARS_MESSAGE);
    verify(mockView).hide();
  }
}
