package org.sagebionetworks.web.unitclient.widget.evaluation;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsListView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModal;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModalView;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidget;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class EvaluationEditorModalTest {
	
	EvaluationEditorModal modal;
	@Mock
	EvaluationEditorModalView mockView;
	@Mock
	ChallengeClientAsync mockChallengeClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	Callback mockEvaluationUpdatedCallback;
	
	Evaluation evaluation;
	String evaluationName = "my existing evaluation queue";
	String submissionInstruction = "Please submit your power of attorney to the Synapse developer";
	String submissionReceiptMessage = "Really?";
	
	@Before
	public void setup() throws Exception{
		MockitoAnnotations.initMocks(this);
		modal = new EvaluationEditorModal(mockView, mockChallengeClient, mockSynAlert);
		evaluation = new Evaluation();
		evaluation.setId("3");
		evaluation.setName(evaluationName);
		evaluation.setSubmissionInstructionsMessage(submissionInstruction);
		evaluation.setSubmissionReceiptMessage(submissionReceiptMessage);
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).updateEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).createEvaluation(any(Evaluation.class), any(AsyncCallback.class));
	}	
	
	@Test
	public void testConstruction() {
		verify(mockView).setSynAlert(any(IsWidget.class));
		verify(mockView).setPresenter(modal);
	}
	@Test
	public void testConfigureNewEvaluation() {
		String projectId = "syn983948";
		modal.configure(projectId, mockEvaluationUpdatedCallback);
		verify(mockView).setEvaluationName(null);
		verify(mockView).setSubmissionInstructionsMessage(null);
		verify(mockView).setSubmissionReceiptMessage(null);
		
		//save new evaluation
		modal.onSave();
		verify(mockChallengeClient).createEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		verify(mockView).hide();
		verify(mockEvaluationUpdatedCallback).invoke();
	}
	@Test
	public void testConfigureExistingEvaluation() {
		modal.configure(evaluation, mockEvaluationUpdatedCallback);
		verify(mockView).setEvaluationName(evaluationName);
		verify(mockView).setSubmissionInstructionsMessage(submissionInstruction);
		verify(mockView).setSubmissionReceiptMessage(submissionReceiptMessage);

		//save existing evaluation
		modal.onSave();
		verify(mockChallengeClient).updateEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		verify(mockView).hide();
		verify(mockEvaluationUpdatedCallback).invoke();
	}
	
	@Test
	public void testShow() {
		modal.show();
		verify(mockView).show();
	}
	
	@Test
	public void testSaveFailure() {
		Exception ex = new Exception("fail");
		AsyncMockStubber.callFailureWith(ex).when(mockChallengeClient).updateEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		modal.configure(evaluation, mockEvaluationUpdatedCallback);

		//save existing evaluation
		modal.onSave();
		verify(mockChallengeClient).updateEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
		verify(mockView, never()).hide();
		verify(mockEvaluationUpdatedCallback, never()).invoke();
	}
}
