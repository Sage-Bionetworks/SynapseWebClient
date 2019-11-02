package org.sagebionetworks.web.unitclient.widget.evaluation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsListView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModal;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AdministerEvaluationsListTest {

	AdministerEvaluationsList evalList;
	@Mock
	AdministerEvaluationsListView mockView;
	@Mock
	ChallengeClientAsync mockChallengeClient;
	@Mock
	EvaluationAccessControlListModalWidget mockAclEditor;
	@Mock
	EvaluationEditorModal mockEvalEditor;
	@Mock
	SynapseAlert mockSynAlert;

	Evaluation e1, e2;

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		evalList = new AdministerEvaluationsList(mockView, mockChallengeClient, mockAclEditor, mockEvalEditor, mockSynAlert);

		ArrayList<Evaluation> evaluationResults = new ArrayList<Evaluation>();

		e1 = new Evaluation();
		e1.setId("101");
		evaluationResults.add(e1);

		e2 = new Evaluation();
		e1.setId("102");
		evaluationResults.add(e2);
		AsyncMockStubber.callSuccessWith(evaluationResults).when(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() {
		evalList.configure("syn100");
		verify(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
		verify(mockView).addRow(e1);
		verify(mockView).addRow(e2);
	}

	@Test
	public void testConfigureZeroResults() {
		AsyncMockStubber.callSuccessWith(new ArrayList<Evaluation>()).when(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
		evalList.configure("syn100");
		verify(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
		verify(mockView, never()).addRow(e1);
	}

	@Test
	public void testConfigureFailure() throws Exception {
		Exception ex = new Exception("bad time");
		AsyncMockStubber.callFailureWith(ex).when(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
		evalList.configure("syn100");
		verify(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnEditClicked() {
		evalList.onEditClicked(e1);
		ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockEvalEditor).configure(eq(e1), callbackCaptor.capture());
		verify(mockEvalEditor).show();
		callbackCaptor.getValue().invoke();
		verify(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testOnShareClicked() {
		evalList.onShareClicked(e1);
		verify(mockAclEditor).configure(eq(e1), any(Callback.class));
		verify(mockAclEditor).show();
	}

	@Test
	public void testOnDeleteEvaluationClicked() {
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).deleteEvaluation(anyString(), any(AsyncCallback.class));
		evalList.onDeleteClicked(e1);
		verify(mockChallengeClient).deleteEvaluation(eq(e1.getId()), any(AsyncCallback.class));
		// refresh
		verify(mockChallengeClient).getSharableEvaluations(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testOnDeleteEvaluationClickedFailure() {
		Exception ex = new Exception("does not compute");
		AsyncMockStubber.callFailureWith(ex).when(mockChallengeClient).deleteEvaluation(anyString(), any(AsyncCallback.class));
		evalList.onDeleteClicked(e1);
		verify(mockChallengeClient).deleteEvaluation(eq(e1.getId()), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
		// no refresh
		verify(mockChallengeClient, never()).getSharableEvaluations(anyString(), any(AsyncCallback.class));
	}

}
