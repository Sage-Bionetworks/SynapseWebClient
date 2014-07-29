package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitterView;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EvaluationSubmitterTest {
		
	private static final String EVALUATION_2_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 2 Submission Receipt Message";
	private static final String EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 1 Submission Receipt Message";
	EvaluationSubmitter submitter;
	EvaluationSubmitterView mockView;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	JSONObjectAdapter jSONObjectAdapter = new JSONObjectAdapterImpl();
	EvaluationSubmitter mockEvaluationSubmitter;
	FileEntity entity;
	EntityBundle bundle;
	List<Evaluation> evaluationList;
	PaginatedResults<TermsOfUseAccessRequirement> requirements;
	AccessRequirementsTransport art;
	
	@Before
	public void setup() throws RestServiceException, JSONObjectAdapterException{	
		mockView = mock(EvaluationSubmitterView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockEvaluationSubmitter = mock(EvaluationSubmitter.class);
		submitter = new EvaluationSubmitter(mockView, mockSynapseClient, mockNodeModelCreator, jSONObjectAdapter, mockGlobalApplicationState, mockAuthenticationController);
		UserSessionData usd = new UserSessionData();
		UserProfile profile = new UserProfile();
		profile.setOwnerId("test owner ID");
		usd.setProfile(profile);
		
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		
		AsyncMockStubber.callSuccessWith("fake submission result json").when(mockSynapseClient).createSubmission(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("fake evaluation results json").when(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getUnmetEvaluationAccessRequirements(anyString(), any(AsyncCallback.class));
		
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		availableEvaluations.setTotalNumberOfResults(2);
		evaluationList = new ArrayList<Evaluation>();
		Evaluation e1 = new Evaluation();
		e1.setId("1");
		e1.setName("Test Evaluation 1");
		e1.setSubmissionReceiptMessage(EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE);
		evaluationList.add(e1);
		Evaluation e2 = new Evaluation();
		e2.setId("2");
		e2.setName("Test Evaluation 2");
		e2.setSubmissionReceiptMessage(EVALUATION_2_SUBMISSION_RECEIPT_MESSAGE);
		evaluationList.add(e2);
		availableEvaluations.setResults(evaluationList);

		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(availableEvaluations);
		
		entity = new FileEntity();
		entity.setVersionNumber(5l);
		entity.setId("file entity test id");
		bundle = new EntityBundle(entity, null, null, null, null, null, null, null);
		
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).getEntity(anyString(), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntity(any(EntityWrapper.class))).thenReturn(entity);
		
		requirements = new PaginatedResults<TermsOfUseAccessRequirement>();
		requirements.setTotalNumberOfResults(0);
		List<TermsOfUseAccessRequirement> ars = new ArrayList<TermsOfUseAccessRequirement>();
		requirements.setResults(ars);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(requirements);
	}
	
	@Test
	public void testSubmitToEvaluations() throws RestServiceException, JSONObjectAdapterException{
		requirements.setTotalNumberOfResults(0);
		submitter.configure(entity, null);
		submitter.submitToEvaluations((Reference)null, null, null, evaluationList);
		//should invoke submission twice (once per evaluation), directly without terms of use
		verify(mockView, times(0)).showAccessRequirement(anyString(), any(Callback.class));
		verify(mockSynapseClient, times(2)).createSubmission(anyString(), anyString(), any(AsyncCallback.class));

		ArgumentCaptor<HashSet> captor = ArgumentCaptor.forClass(HashSet.class);
		//submitted status shown
		verify(mockView).showSubmissionAcceptedDialogs(captor.capture());
		//verify both evaluation receipt messages are in the map to display
		HashSet receiptMessage = captor.getValue();
		assertTrue(receiptMessage.contains(EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE));
		assertTrue(receiptMessage.contains(EVALUATION_2_SUBMISSION_RECEIPT_MESSAGE));
	}
	
	@Test
	public void testSubmitToEvaluationsWithSubmissionNameAndTeamName() throws RestServiceException, JSONObjectAdapterException{
		String submissionName = "my custom submission name";
		String teamName = "my custom team name";
		requirements.setTotalNumberOfResults(0);
		submitter.configure(entity, null);
		submitter.submitToEvaluations(null, submissionName, teamName, evaluationList);
		//should invoke submission twice (once per evaluation), directly without terms of use
		verify(mockView, times(0)).showAccessRequirement(anyString(), any(Callback.class));
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient, times(2)).createSubmission(captor.capture(), anyString(), any(AsyncCallback.class));
		String submissionJson = captor.getValue();
		Submission submission = EntityFactory.createEntityFromJSONString(submissionJson, Submission.class);
		assertEquals(submissionName, submission.getName());
		assertEquals(teamName, submission.getSubmitterAlias());
	}
	
	@Test
	public void testSubmitToEvaluationsFailure() throws RestServiceException, JSONObjectAdapterException{
		submitter.configure(entity, null);
		reset(mockView);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(requirements);
		
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).createSubmission(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(art).when(mockSynapseClient).getUnmetAccessRequirements(anyString(), any(AsyncCallback.class));

		List<Evaluation> evals = new ArrayList<Evaluation>();
		evals.add(new Evaluation());
		submitter.submitToEvaluations((Reference)null, null, null, evals);
		//Should invoke once directly without terms of use
		verify(mockSynapseClient).createSubmission(anyString(), anyString(), any(AsyncCallback.class));
		
		//submitted status shown
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testSubmitToEvaluationsWithTermsOfUse() throws RestServiceException, JSONObjectAdapterException{	
		requirements.setTotalNumberOfResults(1);
		TermsOfUseAccessRequirement requirement = new TermsOfUseAccessRequirement();
		requirement.setId(2l);
		requirement.setTermsOfUse("My test ToU");
		List<TermsOfUseAccessRequirement> ars = new ArrayList<TermsOfUseAccessRequirement>();
		ars.add(requirement);
		requirements.setResults(ars);
		
		submitter.configure(entity, null);
		submitter.submitToEvaluations((Reference)null, null, null, evaluationList);
		
		//should show terms of use for the requirement, view does not call back so submission should not be created
		verify(mockView, times(1)).showAccessRequirement(anyString(), any(Callback.class));
		verify(mockSynapseClient, times(0)).createSubmission(anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testShowAvailableEvaluations() throws RestServiceException, JSONObjectAdapterException {
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		availableEvaluations.setTotalNumberOfResults(1);
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		evaluationList.add(new Evaluation());
		availableEvaluations.setResults(evaluationList);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(availableEvaluations);
		
		submitter.configure(entity, null);
		verify(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		verify(mockView).popupSelector(anyBoolean(), any(List.class));
	}
	
	@Test
	public void testShowAvailableEvaluationsNoResults() throws RestServiceException, JSONObjectAdapterException {
		//mock empty evaluation list
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		availableEvaluations.setTotalNumberOfResults(0);
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		availableEvaluations.setResults(evaluationList);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), any(Class.class))).thenReturn(availableEvaluations);
		submitter.configure(entity, null);
		verify(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		//no evaluations to join error message
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testShowAvailableEvaluationsFailure1() throws RestServiceException, JSONObjectAdapterException {
		AsyncMockStubber.callFailureWith(new ForbiddenException()).when(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		submitter.configure(entity, null);
		verify(mockSynapseClient).getAvailableEvaluations(any(AsyncCallback.class));
		//no evaluations to join error message
		verify(mockView).showErrorMessage(anyString());
	}
}
