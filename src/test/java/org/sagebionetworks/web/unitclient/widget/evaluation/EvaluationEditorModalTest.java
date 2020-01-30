package org.sagebionetworks.web.unitclient.widget.evaluation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.SubmissionQuota;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModal;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationEditorModalView;
import org.sagebionetworks.web.client.widget.user.UserBadge;
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
	@Mock
	UserBadge mockUserBadge;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	SubmissionQuota mockSubmissionQuota;
	@Captor
	ArgumentCaptor<Evaluation> evaluationCaptor;

	Evaluation evaluation;
	String evaluationName = "my existing evaluation queue";
	String submissionInstruction = "Please submit your power of attorney to the Synapse developer";
	String submissionReceiptMessage = "Really?";
	String description = "a description of the evaluation queue";
	Date createdOnDate = new Date();

	Date quotaRoundStart = new Date();
	Long numberOfRounds = 3L;
	Long roundDurationMillis = 10000L;
	Long submissionLimit = 20L;

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		modal = new EvaluationEditorModal(mockView, mockChallengeClient, mockSynAlert, mockUserBadge, mockAuthenticationController, mockDateTimeUtils);
		evaluation = new Evaluation();
		evaluation.setId("3");
		evaluation.setName(evaluationName);
		evaluation.setSubmissionInstructionsMessage(submissionInstruction);
		evaluation.setSubmissionReceiptMessage(submissionReceiptMessage);
		evaluation.setDescription(description);
		evaluation.setCreatedOn(createdOnDate);
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).updateEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockChallengeClient).createEvaluation(any(Evaluation.class), any(AsyncCallback.class));

		when(mockView.getSubmissionLimit()).thenReturn(null);
		when(mockView.getNumberOfRounds()).thenReturn(null);
		when(mockView.getRoundDuration()).thenReturn(null);
		when(mockView.getRoundStart()).thenReturn(null);

		when(mockSubmissionQuota.getFirstRoundStart()).thenReturn(quotaRoundStart);
		when(mockSubmissionQuota.getNumberOfRounds()).thenReturn(numberOfRounds);
		when(mockSubmissionQuota.getRoundDurationMillis()).thenReturn(roundDurationMillis);
		when(mockSubmissionQuota.getSubmissionLimit()).thenReturn(submissionLimit);
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
		verify(mockView).clear();
		verify(mockView).setEvaluationName(null);
		verify(mockView).setSubmissionInstructionsMessage(null);
		verify(mockView).setSubmissionReceiptMessage(null);


		// save new evaluation
		modal.onSave();
		verify(mockChallengeClient).createEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		verify(mockView).hide();
		verify(mockEvaluationUpdatedCallback).invoke();
	}

	@Test
	public void testConfigureExistingEvaluation() {
		modal.configure(evaluation, mockEvaluationUpdatedCallback);
		verify(mockView).clear();
		verify(mockView).setEvaluationName(evaluationName);
		verify(mockView).setSubmissionInstructionsMessage(submissionInstruction);
		verify(mockView).setSubmissionReceiptMessage(submissionReceiptMessage);
		verify(mockView).setDescription(description);
		verify(mockView).setCreatedOn(anyString());
		// verify no quota
		verify(mockView, never()).setRoundStart(any(Date.class));
		verify(mockView, never()).setNumberOfRounds(anyLong());
		verify(mockView, never()).setRoundDuration(anyLong());
		verify(mockView, never()).setSubmissionLimit(anyLong());

		// save existing evaluation
		modal.onSave();
		verify(mockChallengeClient).updateEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		verify(mockView).hide();
		verify(mockEvaluationUpdatedCallback).invoke();
	}

	@Test
	public void testConfigureExistingEvaluationWithQuota() {
		evaluation.setQuota(mockSubmissionQuota);

		modal.configure(evaluation, mockEvaluationUpdatedCallback);

		verify(mockView).clear();
		verify(mockView).setEvaluationName(evaluationName);
		verify(mockView).setSubmissionInstructionsMessage(submissionInstruction);
		verify(mockView).setSubmissionReceiptMessage(submissionReceiptMessage);
		verify(mockView).setDescription(description);
		verify(mockView).setCreatedOn(anyString());
		// verify quota
		verify(mockView).setRoundStart(quotaRoundStart);
		verify(mockView).setNumberOfRounds(numberOfRounds);
		verify(mockView).setRoundDuration(roundDurationMillis);
		verify(mockView).setSubmissionLimit(submissionLimit);
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

		// save existing evaluation
		modal.onSave();
		verify(mockChallengeClient).updateEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
		verify(mockView, never()).hide();
		verify(mockEvaluationUpdatedCallback, never()).invoke();
	}

	@Test
	public void testPartiallyFilledInQuota() {
		when(mockSubmissionQuota.getFirstRoundStart()).thenReturn(null);
		when(mockSubmissionQuota.getNumberOfRounds()).thenReturn(null);
		when(mockSubmissionQuota.getRoundDurationMillis()).thenReturn(null);
		when(mockSubmissionQuota.getSubmissionLimit()).thenReturn(null);
		evaluation.setQuota(mockSubmissionQuota);

		modal.configure(evaluation, mockEvaluationUpdatedCallback);

		// verify quota
		verify(mockView, never()).setRoundStart(any(Date.class));
		verify(mockView, never()).setNumberOfRounds(any(Long.class));
		verify(mockView, never()).setRoundDuration(any(Long.class));
		verify(mockView, never()).setSubmissionLimit(any(Long.class));

		// only the round start is set, no other quota fields
		when(mockView.getRoundStart()).thenReturn(quotaRoundStart);

		modal.onSave();

		verify(mockChallengeClient).updateEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		verify(mockView).hide();
		verify(mockEvaluationUpdatedCallback).invoke();
	}

	@Test
	public void testValidQuota() {
		modal.configure(evaluation, mockEvaluationUpdatedCallback);

		// only the round start is set, no other quota fields
		when(mockView.getRoundStart()).thenReturn(new Date());
		when(mockView.getSubmissionLimit()).thenReturn(22.0);
		when(mockView.getNumberOfRounds()).thenReturn(10.0);
		when(mockView.getRoundDuration()).thenReturn(2000000.0);

		modal.onSave();

		verify(mockChallengeClient).updateEvaluation(any(Evaluation.class), any(AsyncCallback.class));
		verify(mockView).hide();
		verify(mockEvaluationUpdatedCallback).invoke();
	}
}
