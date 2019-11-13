package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter.NO_COMMITS_SELECTED_MSG;
import static org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter.ZERO_COMMITS_ERROR;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.MemberSubmissionEligibility;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.SubmissionEligibility;
import org.sagebionetworks.evaluation.model.TeamSubmissionEligibility;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationSubmitterView;
import org.sagebionetworks.web.shared.FormParams;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EvaluationSubmitterTest {

	private static final String EVALUATION_2_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 2 Submission Receipt Message";
	private static final String EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE = "Evaluation 1 Submission Receipt Message";
	public static final String HOST_PAGE_URL = "http://localhost:8080/test/";
	EvaluationSubmitter submitter;
	@Mock
	EvaluationSubmitterView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	ChallengeClientAsync mockChallengeClient;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	EvaluationSubmitter mockEvaluationSubmitter;
	@Mock
	GWTWrapper mockGWTWrapper;
	@Mock
	PortalGinInjector mockInjector;
	@Mock
	DockerCommitListWidget mockDockerCommitListWidget;
	@Mock
	DockerCommit mockCommit;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	JSONObjectAdapter jSONObjectAdapter = new JSONObjectAdapterImpl();
	FileEntity entity;
	EntityBundle bundle;
	PaginatedResults<TermsOfUseAccessRequirement> requirements;
	Submission returnSubmission;
	Evaluation e1;
	TeamSubmissionEligibility testTeamSubmissionEligibility;
	SubmissionEligibility teamEligibility;
	List<MemberSubmissionEligibility> memberEligibilityList;

	public static final Long ELIGIBILITY_STATE_HASH = 314159269L;

	@Before
	public void setup() throws RestServiceException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		when(mockInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		submitter = new EvaluationSubmitter(mockView, mockSynapseJavascriptClient, mockGlobalApplicationState, mockAuthenticationController, mockChallengeClient, mockGWTWrapper, mockInjector, mockDockerCommitListWidget);
		verify(mockView).setChallengesSynAlertWidget(mockSynAlert.asWidget());
		verify(mockView).setTeamSelectSynAlertWidget(mockSynAlert.asWidget());
		verify(mockView).setContributorsSynAlertWidget(mockSynAlert.asWidget());
		verify(mockView).setDockerCommitSynAlert(mockSynAlert.asWidget());
		UserProfile profile = new UserProfile();
		profile.setOwnerId("test owner ID");

		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(profile);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		returnSubmission = new Submission();
		returnSubmission.setId("363636");
		AsyncMockStubber.callSuccessWith(returnSubmission).when(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(returnSubmission).when(mockChallengeClient).createTeamSubmission(any(Submission.class), anyString(), anyString(), anyString(), any(AsyncCallback.class));

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
		AsyncMockStubber.callSuccessWith(evaluationList).when(mockSynapseJavascriptClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));

		entity = new FileEntity();
		entity.setVersionNumber(5l);
		entity.setId("file entity test id");
		bundle = new EntityBundle();
		bundle.setEntity(entity);

		AsyncMockStubber.callSuccessWith(entity).when(mockSynapseJavascriptClient).getEntity(anyString(), any(AsyncCallback.class));

		requirements = new PaginatedResults<TermsOfUseAccessRequirement>();
		requirements.setTotalNumberOfResults(0);
		List<TermsOfUseAccessRequirement> ars = new ArrayList<TermsOfUseAccessRequirement>();
		requirements.setResults(ars);

		// by default, this is a standard evaluation (no challenge)
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));

		setupTeamSubmissionEligibility();
		when(mockGWTWrapper.getHostPageBaseURL()).thenReturn(HOST_PAGE_URL);
		when(mockCommit.getDigest()).thenReturn("digest");
	}

	public void setupTeamSubmissionEligibility() {
		testTeamSubmissionEligibility = new TeamSubmissionEligibility();
		testTeamSubmissionEligibility.setEligibilityStateHash(ELIGIBILITY_STATE_HASH);
		teamEligibility = new SubmissionEligibility();
		teamEligibility.setIsEligible(true);
		testTeamSubmissionEligibility.setTeamEligibility(teamEligibility);
		memberEligibilityList = new ArrayList<MemberSubmissionEligibility>();
		testTeamSubmissionEligibility.setMembersEligibility(memberEligibilityList);
		AsyncMockStubber.callSuccessWith(testTeamSubmissionEligibility).when(mockChallengeClient).getTeamSubmissionEligibility(anyString(), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testSubmitToEvaluation() throws RestServiceException, JSONObjectAdapterException {
		requirements.setTotalNumberOfResults(0);
		submitter.configure(entity, null, null);
		verify(mockView).resetSubmitButton();
		submitter.onNextClicked(null, null, e1);
		// should invoke submission directly without terms of use
		verify(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), eq(HOST_PAGE_URL), any(AsyncCallback.class));

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		// submitted status shown
		verify(mockView).showSubmissionAcceptedDialogs(captor.capture());
		// verify evaluation receipt message is returned
		String receiptMessage = captor.getValue();
		assertTrue(receiptMessage.contains(EVALUATION_1_SUBMISSION_RECEIPT_MESSAGE));
	}

	@Test
	public void testSubmitToEvaluationsWithSubmissionName() throws RestServiceException, JSONObjectAdapterException {
		String submissionName = "my custom submission name";
		requirements.setTotalNumberOfResults(0);
		submitter.configure(entity, null, null);

		// also set contributors, and verify on individual submission that this is not set in the submission
		// add eligible member
		Long eligibleMemberId = 60L;
		MemberSubmissionEligibility memberEligibility = new MemberSubmissionEligibility();
		memberEligibility.setPrincipalId(eligibleMemberId);
		memberEligibility.setIsEligible(true);
		memberEligibilityList.add(memberEligibility);
		submitter.getContributorList(new Evaluation(), new Team());

		submitter.onNextClicked(null, submissionName, e1);
		// should invoke submission directly without terms of use
		ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
		verify(mockChallengeClient).createIndividualSubmission(captor.capture(), anyString(), eq(HOST_PAGE_URL), any(AsyncCallback.class));
		Submission submission = captor.getValue();
		assertNull(submission.getContributors());
		assertEquals(submissionName, submission.getName());
	}

	@Test
	public void testSubmitToEvaluationsFailure() throws RestServiceException, JSONObjectAdapterException {
		submitter.configure(entity, null, null);
		reset(mockView);

		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), anyString(), any(AsyncCallback.class));

		submitter.onNextClicked(null, null, e1);
		// Should invoke once directly without terms of use
		verify(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), eq(HOST_PAGE_URL), any(AsyncCallback.class));

		// submitted status shown
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testShowSingleAvailableEvaluation() throws RestServiceException, JSONObjectAdapterException {
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		evaluationList.add(new Evaluation());
		AsyncMockStubber.callSuccessWith(evaluationList).when(mockSynapseJavascriptClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		submitter.configure(entity, null, null);
		verify(mockSynapseJavascriptClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showModal1(anyBoolean(), any(FormParams.class), any(List.class));
	}

	@Test
	public void testShowAvailableEvaluations() throws RestServiceException, JSONObjectAdapterException {
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		evaluationList.add(new Evaluation());
		evaluationList.add(new Evaluation());
		AsyncMockStubber.callSuccessWith(evaluationList).when(mockSynapseJavascriptClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		submitter.configure(entity, null, null);
		verify(mockSynapseJavascriptClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showModal1(anyBoolean(), any(FormParams.class), any(List.class));
	}

	@Test
	public void testShowAvailableEvaluationsNoResults() throws RestServiceException, JSONObjectAdapterException {
		// mock empty evaluation list
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		AsyncMockStubber.callSuccessWith(evaluationList).when(mockSynapseJavascriptClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		submitter.configure(entity, null, null);
		verify(mockSynapseJavascriptClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		// no evaluations to join error message
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testShowAvailableEvaluationsFailure1() throws RestServiceException, JSONObjectAdapterException {
		Exception caught = new ForbiddenException("this is forbidden");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseJavascriptClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		submitter.configure(entity, null, null);
		verify(mockSynapseJavascriptClient).getAvailableEvaluations(anySet(), anyBoolean(), anyInt(), anyInt(), any(AsyncCallback.class));
		// no evaluations to join error message
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
	public void testQueryForChallengeAndTeams() throws RestServiceException {
		submitter.configure(entity, null, null);
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
		// the first team should be selected by default
		assertEquals(testTeam, submitter.getSelectedTeam());
		verify(mockView).hideModal1();
		verify(mockView).showModal2();

		// team submission is selected by default if there's a registered team
		assertFalse(submitter.getIsIndividualSubmission());
		verify(mockView).setTeamSubmissionActive();
		verify(mockView).showTeamsUI(anyList());

		submitter.onIndividualSubmissionOptionClicked();
		verify(mockView).showTeamsUI(eq(submissionTeams));
		assertTrue(submitter.getIsIndividualSubmission());

		submitter.onTeamSubmissionOptionClicked();
		verify(mockView, times(2)).showTeamsUI(eq(submissionTeams));

		// try selecting invalid indexes
		submitter.onTeamSelected(-1);
		assertNull(submitter.getSelectedTeam());

		submitter.onTeamSelected(0);
		assertEquals(testTeam, submitter.getSelectedTeam());

		submitter.onTeamSelected(1);
		assertNull(submitter.getSelectedTeam());

		// select and create the team submission
		submitter.onTeamSelected(0);
		assertEquals(testTeam, submitter.getSelectedTeam());

		// set contributor list
		Long eligibleMemberId = 60L;
		MemberSubmissionEligibility memberEligibility = new MemberSubmissionEligibility();
		memberEligibility.setPrincipalId(eligibleMemberId);
		memberEligibility.setIsEligible(true);
		memberEligibilityList.add(memberEligibility);
		submitter.getContributorList(new Evaluation(), new Team());

		submitter.onDoneClicked();
		verify(mockView).setSubmitButtonLoading();
		verify(mockChallengeClient).createTeamSubmission(any(Submission.class), anyString(), anyString(), eq(HOST_PAGE_URL), any(AsyncCallback.class));
	}


	private void configureSubmitter() {
		submitter.configure(entity, null, null);
		reset(mockView);
	}

	@Test
	public void testQueryForChallengeAndEmptyTeams() throws RestServiceException {
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

		// individual submission is selected by default
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
	public void testContributorsListMemberEligibility() throws RestServiceException {
		configureSubmitter();

		// add eligible member
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

		// show loading, true then false
		verify(mockView, times(2)).setContributorsLoading(anyBoolean());

		// by default, team is eligible. In this test, one member is eligible, and one is not
		verify(mockView).addEligibleContributor(eq(eligibleMemberId.toString()));
		verify(mockView).addInEligibleContributor(eq(inEligibleMemberId.toString()), anyString());
		assertEquals(ELIGIBILITY_STATE_HASH.toString(), submitter.getSelectedTeamMemberStateHash());
	}


	@Test
	public void testContributorsListInEligibleTeam() throws RestServiceException {
		configureSubmitter();
		teamEligibility.setIsEligible(false);
		teamEligibility.setIsQuotaFilled(true);
		teamEligibility.setIsRegistered(true);
		submitter.getContributorList(new Evaluation(), new Team());

		// show loading, true then false
		verify(mockView, times(2)).setContributorsLoading(anyBoolean());

		// by default, team is eligible. In this test, one member is eligible, and one is not
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockView).setTeamInEligibleError(stringCaptor.capture());
		assertFalse(stringCaptor.getValue().isEmpty());
	}

	@Test
	public void testQueryForChallengeForbidden() throws RestServiceException {
		configureSubmitter();

		AsyncMockStubber.callFailureWith(new ForbiddenException()).when(mockChallengeClient).getChallengeForProject(anyString(), any(AsyncCallback.class));
		submitter.onNextClicked(new Reference(), "named submission", new Evaluation());
		verify(mockView).hideModal1();
		verify(mockChallengeClient).createIndividualSubmission(any(Submission.class), anyString(), eq(HOST_PAGE_URL), any(AsyncCallback.class));
	}

	@Test
	public void testOnTeamSelected() {
		configureSubmitter();
		submitter.onTeamSelected(0);
		verify(mockView).clearContributors();
		verify(mockView).setTeamInEligibleError("");
	}

	@Test
	public void testOnDockerCommitNextButtonNoCommitsSelected() {
		configureSubmitter();
		submitter.onDockerCommitNextButton();
		verify(mockSynAlert).showError(NO_COMMITS_SELECTED_MSG);
	}

	@Test
	public void testOnDockerCommitNextButton() {
		configureSubmitter();
		when(mockDockerCommitListWidget.getCurrentCommit()).thenReturn(mockCommit);
		submitter.onDockerCommitNextButton();
		verify(mockSynAlert, never()).showError(NO_COMMITS_SELECTED_MSG);
		verify(mockView).hideDockerCommitModal();
	}

	@Test
	public void testConfigureWithDockerEntity() {
		String entityId = "syn123";
		DockerRepository dockerEntity = new DockerRepository();
		dockerEntity.setId(entityId);
		submitter.configure(dockerEntity, null, null);
		// challengeListSynAlert.clear();
		// teamSelectSynAlert.clear();
		// contributorSynAlert.clear();
		verify(mockSynAlert, times(4)).clear();
		verify(mockView).resetNextButton();
		verify(mockView).resetSubmitButton();
		verify(mockView).setContributorsLoading(false);
		ArgumentCaptor<Callback> emptyCallbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockDockerCommitListWidget).setEmptyListCallback(emptyCallbackCaptor.capture());
		verify(mockDockerCommitListWidget).configure(entityId, true);
		verify(mockView).showDockerCommitModal();

		emptyCallbackCaptor.getValue().invoke();
		verify(mockView).hideDockerCommitModal();
		verify(mockView).showErrorMessage(ZERO_COMMITS_ERROR);
	}

	@Test
	public void testSubmitDockerRepo() throws RestServiceException {
		String entityId = "syn123";
		DockerRepository dockerEntity = new DockerRepository();
		dockerEntity.setId(entityId);
		submitter.configure(dockerEntity, null, null);
		submitter.setDigest(mockCommit);
		submitter.onDockerCommitNextButton();

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

		submitter.onTeamSubmissionOptionClicked();
		submitter.onTeamSelected(0);
		Long eligibleMemberId = 60L;
		MemberSubmissionEligibility memberEligibility = new MemberSubmissionEligibility();
		memberEligibility.setPrincipalId(eligibleMemberId);
		memberEligibility.setIsEligible(true);
		memberEligibilityList.add(memberEligibility);
		submitter.getContributorList(new Evaluation(), new Team());

		ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
		submitter.onDoneClicked();
		verify(mockView).setSubmitButtonLoading();
		verify(mockChallengeClient).createTeamSubmission(captor.capture(), anyString(), anyString(), eq(HOST_PAGE_URL), any(AsyncCallback.class));
		assertNotNull(captor.getValue().getDockerDigest());
	}
}
