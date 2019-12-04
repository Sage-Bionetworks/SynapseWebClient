package org.sagebionetworks.web.unitclient.widget.entity.act;

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
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.NO_EMAIL_MESSAGE;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.NO_USER_SELECTED;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.QUERY_CANCELLED;
import static org.sagebionetworks.web.client.widget.entity.act.ApproveUserAccessModal.REVOKED_USER;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
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
import com.google.gwt.user.client.rpc.AsyncCallback;

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
	@Captor
	ArgumentCaptor<AsyncCallback<AccessApproval>> aaCaptor;
	@Captor
	ArgumentCaptor<AsynchronousProgressHandler> phCaptor;
	@Captor
	ArgumentCaptor<AsyncCallback<String>> sCaptor;
	@Captor
	ArgumentCaptor<AsyncCallback<PaginatedResults<AccessApproval>>> prCaptor;
	@Captor
	ArgumentCaptor<AsyncCallback<Void>> vCaptor;

	Long accessReq;
	String userId;
	String message;
	List<ACTAccessRequirement> actList;
	Exception ex;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		dialog = new ApproveUserAccessModal(mockView, mockSynAlert, mockPeopleSuggestWidget, mockProvider, mockSynapseClient, mockSynapseProperties, mockProgressWidget, mockDataAccessClient);
		when(mockSynapseProperties.getSynapseProperty(anyString())).thenReturn("syn7444807");

		message = "Message";
		userId = "1234567";
		accessReq = 123L;
		ex = new Exception("error message");

		ACTAccessRequirement act = Mockito.mock(ACTAccessRequirement.class);
		actList = new ArrayList<ACTAccessRequirement>();
		actList.add(act);

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
	}

	@Test
	public void testConfigureNoAccessReqs() {
		List<ACTAccessRequirement> accessReqs = new ArrayList<ACTAccessRequirement>();
		dialog.configure(accessReqs, mockEntityBundle);
		verify(mockView, times(0)).setAccessRequirement(anyString(), anyString());
	}

	@Test
	public void testConfigureOneAccessReq() {
		ACTAccessRequirement ar = actList.get(0);
		String num = Long.toString(ar.getId());
		String text = GovernanceServiceHelper.getAccessRequirementText(ar);
		dialog.configure(actList, mockEntityBundle);
		verify(mockView).setAccessRequirement(eq(num), eq(text));
		verify(mockView, times(1)).setAccessRequirement(anyString(), anyString());
		verify(mockView).setDatasetTitle(mockEntityBundle.getEntity().getName());
	}

	@Test
	public void testLoadEmailMessageOnFailure() {
		dialog.configure(actList, mockEntityBundle);
		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
		phCaptor.getValue().onFailure(ex);
		verify(mockView).setLoadingEmailVisible(false);
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testLoadEmailMessageOnCancel() {
		dialog.configure(actList, mockEntityBundle);
		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
		phCaptor.getValue().onCancel();
		verify(mockView).setLoadingEmailVisible(false);
		verify(mockSynAlert).showError(QUERY_CANCELLED);
	}

	@Test
	public void testLoadEmailMessageOnComplete() {
		dialog.configure(actList, mockEntityBundle);
		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
		phCaptor.getValue().onComplete(mockQrb);
		verify(mockView).finishLoadingEmail();
	}

	@Test
	public void testOnStateSelected() {
		ACTAccessRequirement ar = actList.get(0);
		String num = Long.toString(ar.getId());
		String text = GovernanceServiceHelper.getAccessRequirementText(ar);
		dialog.configure(actList, mockEntityBundle);
		dialog.onStateSelected(num);
		verify(mockView, times(2)).setAccessRequirement(eq(num), eq(text));
	}

	@Test
	public void testOnSubmitNoUserSelected() {
		dialog.configure(actList, mockEntityBundle);
		dialog.onSubmit();
		verify(mockSynAlert).showError(eq(NO_USER_SELECTED));
	}

	@Test
	public void testOnSubmitQueryCancelledEditedEmail() {
		dialog.configure(actList, mockEntityBundle);
		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
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
		dialog.configure(actList, mockEntityBundle);
		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
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
		dialog.configure(actList, mockEntityBundle);
		when(mockSynapseProperties.getSynapseProperty(anyString())).thenReturn(null);
		when(mockView.getEmailMessage()).thenReturn("");
		dialog.onUserSelected(mockUser);
		dialog.onSubmit();
		verify(mockSynAlert, times(0)).showError(eq(NO_USER_SELECTED));
		verify(mockSynAlert).showError(MESSAGE_BLANK);
	}

	@Test
	public void testOnSubmitNullAccessReq() {
		dialog.configure(actList, mockEntityBundle);

		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
		phCaptor.getValue().onComplete(mockQrb);

		dialog.onUserSelected(mockUser);
		dialog.onSubmit();
		verify(mockSynAlert, times(0)).showError(anyString());
		verify(mockView).getAccessRequirement();
		verify(mockView).setApproveProcessing(true);
	}

	@Test
	public void testOnSubmitOnFailure() {
		dialog.configure(actList, mockEntityBundle);

		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
		phCaptor.getValue().onComplete(mockQrb);
		verify(mockView).setMessageEditArea(message);

		dialog.onUserSelected(mockUser);
		dialog.onSubmit();
		verify(mockSynAlert, times(0)).showError(anyString());
		verify(mockView).getAccessRequirement();
		verify(mockView).setApproveProcessing(true);

		verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), aaCaptor.capture());
		aaCaptor.getValue().onFailure(ex);
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnSubmitOnSuccess() {
		dialog.configure(actList, mockEntityBundle);

		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
		phCaptor.getValue().onComplete(mockQrb);

		dialog.onUserSelected(mockUser);
		dialog.onSubmit();
		verify(mockSynAlert, times(0)).showError(anyString());
		verify(mockView).getAccessRequirement();
		verify(mockView).setApproveProcessing(true);

		verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), aaCaptor.capture());
		aaCaptor.getValue().onSuccess(mockAccessApproval);
		verify(mockSynapseClient).sendMessage(anySetOf(String.class), anyString(), anyString(), anyString(), sCaptor.capture());
	}

	@Test
	public void testOnSubmitSendMessageOnFailure() {
		dialog.configure(actList, mockEntityBundle);

		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
		phCaptor.getValue().onComplete(mockQrb);

		dialog.onUserSelected(mockUser);
		dialog.onSubmit();
		verify(mockSynAlert, times(0)).showError(NO_USER_SELECTED);
		verify(mockSynAlert, times(0)).showError(NO_EMAIL_MESSAGE);
		verify(mockView).getAccessRequirement();
		verify(mockView).setApproveProcessing(true);

		verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), aaCaptor.capture());
		aaCaptor.getValue().onSuccess(mockAccessApproval);

		verify(mockSynapseClient).sendMessage(anySetOf(String.class), anyString(), anyString(), anyString(), sCaptor.capture());
		sCaptor.getValue().onFailure(ex);

		verify(mockView).setApproveProcessing(false);
		verify(mockSynAlert).showError(APPROVE_BUT_FAIL_TO_EMAIL + ex.getMessage());

	}

	@Test
	public void testOnSubmitSendMessageOnSuccess() {
		dialog.configure(actList, mockEntityBundle);

		verify(mockProgressWidget).startAndTrackJob(anyString(), anyBoolean(), any(AsynchType.class), any(QueryBundleRequest.class), phCaptor.capture());
		phCaptor.getValue().onComplete(mockQrb);

		dialog.onUserSelected(mockUser);
		dialog.onSubmit();
		verify(mockSynAlert, times(0)).showError(anyString());
		verify(mockView).getAccessRequirement();
		verify(mockView).setApproveProcessing(true);

		verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), aaCaptor.capture());
		aaCaptor.getValue().onSuccess(mockAccessApproval);

		verify(mockSynapseClient).sendMessage(anySetOf(String.class), anyString(), anyString(), anyString(), sCaptor.capture());
		sCaptor.getValue().onSuccess(anyString());

		verify(mockView).setApproveProcessing(false);
		verify(mockView).hide();
		verify(mockView).showInfo(APPROVED_USER + EMAIL_SENT);
	}

	@Test
	public void testOnRevokeNoUser() {
		dialog.configure(actList, mockEntityBundle);
		dialog.onRevoke();
		verify(mockSynAlert).showError(NO_USER_SELECTED);
	}

	@Test
	public void testOnRevokeGetAccessApprovalFailure() {
		dialog.configure(actList, mockEntityBundle);
		dialog.onUserSelected(mockUser);
		dialog.onRevoke();

		verify(mockView).setRevokeProcessing(true);
		verify(mockSynapseClient).deleteAccessApprovals(anyString(), anyString(), vCaptor.capture());
		vCaptor.getValue().onFailure(ex);
		verify(mockSynAlert).handleException(ex);
		verify(mockView).setRevokeProcessing(false);
	}

	@Test
	public void testOnRevokeGetAccessApprovalSuccessNoMatch() {
		dialog.configure(actList, mockEntityBundle);
		dialog.onUserSelected(mockUser);
		dialog.onRevoke();

		verify(mockView).setRevokeProcessing(true);
		verify(mockSynapseClient).deleteAccessApprovals(anyString(), anyString(), vCaptor.capture());
		vCaptor.getValue().onSuccess((Void) null);
		verify(mockView).setRevokeProcessing(false);
		verify(mockView).hide();
		verify(mockView).showInfo(REVOKED_USER);
	}

	@Test
	public void testOnRevokeGetAccessApprovalSuccessMatchFound() {
		dialog.configure(actList, mockEntityBundle);
		dialog.onUserSelected(mockUser);
		dialog.onRevoke();

		verify(mockView).setRevokeProcessing(true);
		verify(mockSynapseClient).deleteAccessApprovals(anyString(), anyString(), vCaptor.capture());
		vCaptor.getValue().onSuccess((Void) null);
		verify(mockView).setRevokeProcessing(false);
		verify(mockView).hide();
		verify(mockView).showInfo(REVOKED_USER);
	}

}
