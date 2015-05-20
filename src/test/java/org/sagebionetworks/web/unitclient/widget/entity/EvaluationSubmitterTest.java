package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.MemberSubmissionEligibility;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.SubmissionEligibility;
import org.sagebionetworks.evaluation.model.TeamSubmissionEligibility;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitterView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EvaluationSubmitterTest {
		
	private static final String EVALUATION_2_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 2 Submission Receipt Message";
	private static final String EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 1 Submission Receipt Message";
	EvaluationSubmitter submitter;
	EvaluationSubmitterView mockView;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;
	ChallengeClientAsync mockChallengeClient;
	GlobalApplicationState mockGlobalApplicationState;
	JSONObjectAdapter jSONObjectAdapter = new JSONObjectAdapterImpl();
	SynapseAlert mockSynAlert;
	EvaluationSubmitter mockEvaluationSubmitter;
	FileEntity entity;
	EntityBundle bundle;
	PaginatedResults<TermsOfUseAccessRequirement> requirements;
	AccessRequirementsTransport art;
	Submission returnSubmission;
	Evaluation e1;
	TeamSubmissionEligibility testTeamSubmissionEligibility;
	SubmissionEligibility teamEligibility;
	List<MemberSubmissionEligibility> memberEligibilityList;
	
	public static final Long ELIGIBILITY_STATE_HASH = 314159269L;
	
	@Before
	public void setup() throws RestServiceException, JSONObjectAdapterException{	
		mockView = mock(EvaluationSubmitterView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockChallengeClient = mock(ChallengeClientAsync.class);
		mockEvaluationSubmitter = mock(EvaluationSubmitter.class);
		mockSynAlert = mock(SynapseAlert.class);
		submitter = new EvaluationSubmitter(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, mockChallengeClient, mockSynAlert);
		verify(mockView).setSynAlertWidget(mockSynAlert.asWidget());
		UserSessionData usd = new UserSessionData();
		UserProfile profile = new UserProfile();
		profile.setOwnerId("test owner ID");
		usd.setProfile(profile);
		
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		returnSubmission = new Submission();
		returnSubmission.setId("363636");
		AsyncMockStubber.callSuccessWith(returnSubmission).when(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), any(AsyncCallback.class));

		
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		availableEvaluations.setTotalNumberOfResults(2);
		ArrayList<Evaluation> evaluationList = new ArrayList<Evaluation>();
		e1 = new Evaluation();
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
		AsyncMockStubber.callSuccessWith(availableEvaluations).when(mockChallengeClient).getAvailableEvaluations(any(AsyncCallback.class));
		
		entity = new FileEntity();
		entity.setVersionNumber(5l);
		entity.setId("file entity test id");
		bundle = new EntityBundle();
		bundle.setEntity(entity);
		
		AsyncMockStubber.callSuccessWith(entity).when(mockSynapseClient).getEntity(anyString(), any(AsyncCallback.class));

		requirements = new PaginatedResults<TermsOfUseAccessRequirement>();
		requirements.setTotalNumberOfResults(0);
		List<TermsOfUseAccessRequirement> ars = new ArrayList<TermsOfUseAccessRequirement>();
		requirements.setResults(ars);
		
		//by default, this is a standard evaluation (no challenge)
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		
		setupTeamSubmissionEligibility();
	}
	
	public void setupTeamSubmissionEligibility() {
		testTeamSubmissionEligibility =  new TeamSubmissionEligibility();
		testTeamSubmissionEligibility.setEligibilityStateHash(ELIGIBILITY_STATE_HASH);
		teamEligibility = new SubmissionEligibility();
		teamEligibility.setIsEligible(true);
		testTeamSubmissionEligibility.setTeamEligibility(teamEligibility);
		memberEligibilityList = new ArrayList<MemberSubmissionEligibility>();
		testTeamSubmissionEligibility.setMembersEligibility(memberEligibilityList);
		AsyncMockStubber.callSuccessWith(testTeamSubmissionEligibility).when(mockChallengeClient).getTeamSubmissionEligibility(anyString(), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testSubmitToEvaluation() throws RestServiceException, JSONObjectAdapterException{
		requirements.setTotalNumberOfResults(0);
		submitter.configure(entity, null);
		submitter.onNextClicked(null, null, e1);
		//should invoke submission directly without terms of use
		verify(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), any(AsyncCallback.class));

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		//submitted status shown
		verify(mockView).showSubmissionAcceptedDialogs(captor.capture());
		//verify evaluation receipt message is returned
		String receiptMessage = captor.getValue();
		assertTrue(receiptMessage.contains(EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE));
	}
	
	@Test
	public void testSubmitToEvaluationsWithSubmissionName() throws RestServiceException, JSONObjectAdapterException{
		String submissionName = "my custom submission name";
		requirements.setTotalNumberOfResults(0);
		submitter.configure(entity, null);
		
		//also set contributors, and verify on individual submission that this is not set in the submission
		//add eligible member
		Long eligibleMemberId = 60L;
		MemberSubmissionEligibility memberEligibility = new MemberSubmissionEligibility();
		memberEligibility.setPrincipalId(eligibleMemberId);
		memberEligibility.setIsEligible(true);
		memberEligibilityList.add(memberEligibility);
		submitter.getContributorList(new Evaluation(), new Team());
		
		submitter.onNextClicked(null,  submissionName,  e1);
		//should invoke submission directly without terms of use
		ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
		verify(mockChallengeClient).createIndividualSubmission(captor.capture(), anyString(), any(AsyncCallback.class));
		Submission submission = captor.getValue();
		assertNull(submission.getContributors());
		assertEquals(submissionName, submission.getName());
	}
	
	@Test
	public void testSubmitToEvaluationsFailure() throws RestServiceException, JSONObjectAdapterException{
		submitter.configure(entity, null);
		reset(mockView);
		
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(art).when(mockSynapseClient).getUnmetAccessRequirements(anyString(), any(ACCESS_TYPE.class), any(AsyncCallback.class));

		submitter.onNextClicked(null, null, e1);
		//Should invoke once directly without terms of use
		verify(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), any(AsyncCallback.class));
		
		//submitted status shown
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testShowAvailableEvaluations() throws RestServiceException, JSONObjectAdapterException {
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		availableEvaluations.setTotalNumberOfResults(1);
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		evaluationList.add(new Evaluation());
		availableEvaluations.setResults(evaluationList);
		
		submitter.configure(entity, null);
		verify(mockChallengeClient).getAvailableEvaluations(any(AsyncCallback.class));
		verify(mockView).showModal1(anyBoolean(), any(List.class));
	}
	
	@Test
	public void testShowAvailableEvaluationsNoResults() throws RestServiceException, JSONObjectAdapterException {
		//mock empty evaluation list
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		availableEvaluations.setTotalNumberOfResults(0);
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		availableEvaluations.setResults(evaluationList);
		AsyncMockStubber.callSuccessWith(availableEvaluations).when(mockChallengeClient).getAvailableEvaluations(any(AsyncCallback.class));
		submitter.configure(entity, null);
		verify(mockChallengeClient).getAvailableEvaluations(any(AsyncCallback.class));
		//no evaluations to join error message
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testShowAvailableEvaluationsFailure1() throws RestServiceException, JSONObjectAdapterException {
		Exception caught = new ForbiddenException("this is forbidden");
		AsyncMockStubber.callFailureWith(caught).when(mockChallengeClient).getAvailableEvaluations(any(AsyncCallback.class));
		submitter.configure(entity, null);
		verify(mockChallengeClient).getAvailableEvaluations(any(AsyncCallback.class));
		//no evaluations to join error message
		verify(mockSynAlert).handleException(caught);
	}
	
	/****
	 * Now test challenge based submissions
	 */
	
	private Challenge getTestChallenge() {
		Challenge testChallenge = new Challenge();
		testChallenge.setId("4");
		testChallenge.setProjectId("syn9999");
		testChallenge.setParticipantTeamId("78");
		return testChallenge;
	}
	
	@Test
	public void testQueryForChallengeAndTeams() throws RestServiceException{
		submitter.configure(entity, null);
		reset(mockView);
		
		Challenge testChallenge = getTestChallenge();
		AsyncMockStubber.callSuccessWith(testChallenge).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		Team testTeam = new Team();
		testTeam.setId("80");
		testTeam.setName("test team");
		List<Team> submissionTeams = Collections.singletonList(testTeam);
		AsyncMockStubber.callSuccessWith(submissionTeams).when(mockChallengeClient).getSubmissionTeams(anyString(), anyString(), any(AsyncCallback.class));
		
		Evaluation testEvaluation = new Evaluation();
		testEvaluation.setContentSource("syn9999");
		submitter.onNextClicked(new Reference(), "named submission", testEvaluation);
		assertEquals(testChallenge, submitter.getChallenge());
		//the first team should be selected by default
		assertEquals(testTeam, submitter.getSelectedTeam());
		verify(mockView).hideModal1();
		verify(mockView).showModal2();
		
		//individual submission is selected by default
		assertTrue(submitter.getIsIndividualSubmission());
		verify(mockView).setIsIndividualSubmissionActive(true);
		verify(mockView, never()).showTeamsUI(anyList());
		submitter.onTeamSubmissionOptionClicked();
		verify(mockView).showTeamsUI(eq(submissionTeams));
		assertFalse(submitter.getIsIndividualSubmission());
		
		//try selecting invalid indexes
		submitter.onTeamSelected(-1);
		assertNull(submitter.getSelectedTeam());
		
		submitter.onTeamSelected(0);
		assertEquals(testTeam, submitter.getSelectedTeam());
		
		submitter.onTeamSelected(1);
		assertNull(submitter.getSelectedTeam());
	}
	
	
	private void configureSubmitter() {
		submitter.configure(entity, null);
		reset(mockView);
	}
	
	@Test
	public void testQueryForChallengeAndEmptyTeams() throws RestServiceException{
		configureSubmitter();
		
		Challenge testChallenge = getTestChallenge();
		AsyncMockStubber.callSuccessWith(testChallenge).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		List<Team> submissionTeams = Collections.emptyList();
		AsyncMockStubber.callSuccessWith(submissionTeams).when(mockChallengeClient).getSubmissionTeams(anyString(), anyString(), any(AsyncCallback.class));
		
		Evaluation testEvaluation = new Evaluation();
		testEvaluation.setContentSource("syn9999");
		submitter.onNextClicked(new Reference(), "named submission", testEvaluation);
		assertEquals(testChallenge, submitter.getChallenge());
		verify(mockView).showModal2();
		
		//individual submission is selected by default
		assertTrue(submitter.getIsIndividualSubmission());
		verify(mockView, never()).showEmptyTeams();
		submitter.onTeamSubmissionOptionClicked();
		verify(mockView).showEmptyTeams();
		assertFalse(submitter.getIsIndividualSubmission());
	}
	
	@Test
	public void testOnIndividualSubmissionOptionClicked() {
		configureSubmitter();
		
		submitter.onIndividualSubmissionOptionClicked();
		verify(mockView).hideTeamsUI();
	}
	
	@Test
	public void testContributorsListMemberEligibility() throws RestServiceException{
		configureSubmitter(); 
		
		//add eligible member
		Long eligibleMemberId = 60L;
		MemberSubmissionEligibility memberEligibility = new MemberSubmissionEligibility();
		memberEligibility.setPrincipalId(eligibleMemberId);
		memberEligibility.setIsEligible(true);
		memberEligibilityList.add(memberEligibility);
		
		Long inEligibleMemberId = 70L;
		memberEligibility = new MemberSubmissionEligibility();
		memberEligibility.setPrincipalId(inEligibleMemberId);
		memberEligibility.setIsEligible(false);
		memberEligibility.setIsRegistered(true);
		memberEligibility.setIsQuotaFilled(false);
		memberEligibility.setHasConflictingSubmission(true);
		memberEligibilityList.add(memberEligibility);
		
		submitter.getContributorList(new Evaluation(), new Team());
		
		//show loading, true then false
		verify(mockView, times(2)).setContributorsLoading(anyBoolean());
		
		//by default, team is eligible.  In this test, one member is eligible, and one is not
		verify(mockView).addEligibleContributor(eq(eligibleMemberId.toString()));
		verify(mockView).addInEligibleContributor(eq(inEligibleMemberId.toString()), anyString());
		assertEquals(ELIGIBILITY_STATE_HASH.toString(), submitter.getSelectedTeamMemberStateHash());
	}
	

	@Test
	public void testContributorsListInEligibleTeam() throws RestServiceException{
		configureSubmitter(); 
		teamEligibility.setIsEligible(false);
		teamEligibility.setIsQuotaFilled(true);
		teamEligibility.setIsRegistered(true);
		submitter.getContributorList(new Evaluation(), new Team());
		
		//show loading, true then false
		verify(mockView, times(2)).setContributorsLoading(anyBoolean());
		
		//by default, team is eligible.  In this test, one member is eligible, and one is not
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockView).setTeamInEligibleError(stringCaptor.capture());
		assertFalse(stringCaptor.getValue().isEmpty());
	}
	
	@Test
	public void testQueryForChallengeForbidden() throws RestServiceException{
		configureSubmitter();
		
		AsyncMockStubber.callFailureWith(new ForbiddenException()).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		submitter.onNextClicked(new Reference(), "named submission", new Evaluation());
		verify(mockView).hideModal1();
		verify(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testOnTeamSelected() {
		configureSubmitter();
		submitter.onTeamSelected(0);
		verify(mockView).clearContributors();
		verify(mockView).setTeamInEligibleError("");
	}
	//TODO: add tests for onindividual and onteam
}
