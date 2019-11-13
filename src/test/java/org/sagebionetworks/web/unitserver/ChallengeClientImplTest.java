package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.EvaluationStatus;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.message.MessageToUser;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.server.servlet.ChallengeClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@RunWith(MockitoJUnitRunner.class)
public class ChallengeClientImplTest {


	public static final String TEST_CHALLENGE_PROJECT_NAME = "test challenge project name";
	public static final String MY_USER_PROFILE_OWNER_ID = "MyOwnerID";
	public static final String TEST_HOME_PAGE_BASE = "http://mysynapse.org/";
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	SynapseClient mockSynapse;
	ChallengeClientImpl synapseClient;
	String entityId = "123";
	String inviteeUserId = "900";
	UserProfile inviteeUserProfile;
	ExampleEntity entity;
	Annotations annos;
	UserEntityPermissions eup;
	UserEvaluationPermissions userEvaluationPermissions;
	List<EntityHeader> batchHeaderResults;

	String testFileName = "testFileEntity.R";
	EntityPath path;
	org.sagebionetworks.reflection.model.PaginatedResults<UserGroup> pgugs;
	org.sagebionetworks.reflection.model.PaginatedResults<UserProfile> pgups;
	AccessControlList acl;
	WikiPage page;
	V2WikiPage v2Page;
	S3FileHandle handle;
	Evaluation mockEvaluation;
	UserSessionData mockUserSessionData;
	UserProfile mockUserProfile;
	MembershipInvitation testInvitation;
	MessageToUser sentMessage;

	private static final String testChallengeId = "1";
	private static final String testTeam1 = "3322410";
	private static final String testTeam2 = "3319267";
	private static final String testChallengeProject = "syn2290704";

	private static final String EVAL_ID_1 = "eval ID 1";
	private static final String EVAL_ID_2 = "eval ID 2";
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl();
	ChallengeTeam testChallengeTeam1, testChallengeTeam2;
	Challenge testChallenge;


	@Mock
	ThreadLocal<HttpServletRequest> mockThreadLocal;

	@Mock
	HttpServletRequest mockRequest;

	String userIp = "127.0.0.1";

	@Before
	public void before() throws SynapseException, JSONObjectAdapterException {
		mockSynapse = Mockito.mock(SynapseClient.class);
		mockSynapseProvider = Mockito.mock(SynapseProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		mockTokenProvider = Mockito.mock(TokenProvider.class);

		synapseClient = new ChallengeClientImpl();
		synapseClient.setSynapseProvider(mockSynapseProvider);
		synapseClient.setTokenProvider(mockTokenProvider);

		// user can change permissions on eval 2, but not on 1
		userEvaluationPermissions = new UserEvaluationPermissions();
		userEvaluationPermissions.setCanChangePermissions(false);
		when(mockSynapse.getUserEvaluationPermissions(EVAL_ID_1)).thenReturn(userEvaluationPermissions);

		userEvaluationPermissions = new UserEvaluationPermissions();
		userEvaluationPermissions.setCanChangePermissions(true);
		when(mockSynapse.getUserEvaluationPermissions(EVAL_ID_2)).thenReturn(userEvaluationPermissions);


		org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader> batchHeaders = new org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader>();
		batchHeaderResults = new ArrayList<EntityHeader>();
		for (int i = 0; i < 10; i++) {
			EntityHeader h = new EntityHeader();
			h.setId("syn" + i);
			batchHeaderResults.add(h);
		}
		batchHeaders.setResults(batchHeaderResults);
		when(mockSynapse.getEntityHeaderBatch(anyList())).thenReturn(batchHeaders);

		mockEvaluation = Mockito.mock(Evaluation.class);
		when(mockEvaluation.getStatus()).thenReturn(EvaluationStatus.OPEN);
		when(mockSynapse.getEvaluation(anyString())).thenReturn(mockEvaluation);
		mockUserSessionData = Mockito.mock(UserSessionData.class);
		mockUserProfile = Mockito.mock(UserProfile.class);
		when(mockSynapse.getUserSessionData()).thenReturn(mockUserSessionData);
		when(mockUserSessionData.getProfile()).thenReturn(mockUserProfile);
		when(mockUserProfile.getOwnerId()).thenReturn(MY_USER_PROFILE_OWNER_ID);

		when(mockSynapse.getMyProfile()).thenReturn(mockUserProfile);
		testChallengeTeam1 = getTestChallengeTeam("join the first team", testTeam1);
		testChallengeTeam2 = getTestChallengeTeam("join the second team", testTeam2);
		testChallenge = getTestChallenge();
		setupChallengeteamPagedResults();
		when(mockSynapse.getChallenge(anyString())).thenReturn(testChallenge);

		Whitebox.setInternalState(synapseClient, "perThreadRequest", mockThreadLocal);
		userIp = "127.0.0.1";
		when(mockThreadLocal.get()).thenReturn(mockRequest);
		when(mockRequest.getRemoteAddr()).thenReturn(userIp);
	}

	@Test
	public void testCreateIndividualSubmission() throws RestServiceException, SynapseException {
		// pass through
		Submission mockSubmission = mock(Submission.class);
		String etag = "test etag";
		synapseClient.createIndividualSubmission(mockSubmission, etag, TEST_HOME_PAGE_BASE);
		verify(mockSynapse).createIndividualSubmission(mockSubmission, etag, TEST_HOME_PAGE_BASE + "#!Synapse:", TEST_HOME_PAGE_BASE + "#!SignedToken:");
	}

	@Test
	public void testCreateTeamSubmission() throws RestServiceException, SynapseException {
		Submission mockSubmission = mock(Submission.class);
		String etag = "test etag";
		String memberStateHash = "1244458373";
		synapseClient.createTeamSubmission(mockSubmission, etag, memberStateHash, TEST_HOME_PAGE_BASE);
		verify(mockSynapse).createTeamSubmission(mockSubmission, etag, memberStateHash, TEST_HOME_PAGE_BASE + "#!Synapse:", TEST_HOME_PAGE_BASE + "#!SignedToken:");
	}

	private void setupTeams(String... teamNames) throws SynapseException {
		List<Team> teams = new ArrayList<Team>();
		for (String teamName : teamNames) {
			Team t1 = new Team();
			t1.setName(teamName);
			teams.add(t1);
		}
		when(mockSynapse.listTeams(anyList())).thenReturn(teams);
	}

	@Test
	public void testGetTeamsSorted() throws SynapseException {
		setupTeams("z team", "A team");
		List<Team> returnedTeams = synapseClient.getTeams(Collections.singletonList("1234"), true, mockSynapse);

		assertEquals(2, returnedTeams.size());
		assertEquals("A team", returnedTeams.get(0).getName());
		assertEquals("z team", returnedTeams.get(1).getName());
	}

	@Test
	public void testGetTeamsUnSorted() throws SynapseException {
		setupTeams("z team", "A team");
		List<Team> returnedTeams = synapseClient.getTeams(Collections.singletonList("1234"), false, mockSynapse);

		assertEquals(2, returnedTeams.size());
		assertEquals("z team", returnedTeams.get(0).getName());
		assertEquals("A team", returnedTeams.get(1).getName());
	}


	@Test
	public void testRegisterChallengeTeam() throws SynapseException, RestServiceException {
		// pass through
		ChallengeTeam challengeTeam = mock(ChallengeTeam.class);
		synapseClient.registerChallengeTeam(challengeTeam);
		verify(mockSynapse).createChallengeTeam(challengeTeam);
	}

	@Test
	public void testUnRegisterChallengeTeam() throws SynapseException, RestServiceException {
		// pass through
		String challengeTeamId = "8888";
		synapseClient.unregisterChallengeTeam(challengeTeamId);
		verify(mockSynapse).deleteChallengeTeam(challengeTeamId);
	}

	@Test
	public void testUpdateRegisteredChallengeTeam() throws SynapseException, RestServiceException {
		// pass through
		ChallengeTeam challengeTeam = mock(ChallengeTeam.class);
		synapseClient.updateRegisteredChallengeTeam(challengeTeam);
		verify(mockSynapse).updateChallengeTeam(challengeTeam);
	}

	@Test
	public void testGetChallengeForProject() throws SynapseException, RestServiceException {
		// pass through
		String projectId = "syn0000009";
		synapseClient.getChallengeForProject(projectId);
		verify(mockSynapse).getChallengeForProject(projectId);
	}

	@Test
	public void testGetChallengeTeamsAnonymous() throws SynapseException, RestServiceException {
		ChallengeTeamPagedResults results = synapseClient.getChallengeTeams(null, "2", 10, 0);
		// before we set up challenge results
		// we should not ask the synapse client for team members in this case (only for isAdmin)
		verify(mockSynapse, never()).listTeamMembers(anyList(), anyString());
		verify(mockSynapse).listChallengeTeams(anyString(), anyLong(), anyLong());
		assertTrue(results.getTotalNumberOfResults() == 2);
		assertEquals(testChallengeTeam1, results.getResults().get(0).getChallengeTeam());
		assertFalse(results.getResults().get(0).isAdmin());
		assertEquals(testChallengeTeam2, results.getResults().get(1).getChallengeTeam());
		assertFalse(results.getResults().get(1).isAdmin());
	}

	@Test
	public void testGetChallengeTeamsLoggedIn() throws SynapseException, RestServiceException {
		// respond that user is an admin for test team 1, but not team 2
		List<TeamMember> testTeamMembers = new ArrayList<TeamMember>();
		TeamMember member1 = new TeamMember();
		member1.setTeamId(testTeam1);
		member1.setIsAdmin(true);
		testTeamMembers.add(member1);
		TeamMember member2 = new TeamMember();
		member2.setTeamId(testTeam2);
		member2.setIsAdmin(false);
		testTeamMembers.add(member2);

		when(mockSynapse.getTeamMember(anyString(), anyString())).thenReturn(member1, member2);
		ChallengeTeamPagedResults results = synapseClient.getChallengeTeams("1234", "2", 10, 0);
		verify(mockSynapse, times(2)).getTeamMember(anyString(), anyString());
		verify(mockSynapse).listChallengeTeams(anyString(), anyLong(), anyLong());
		assertTrue(results.getTotalNumberOfResults() == 2);
		assertEquals(testChallengeTeam1, results.getResults().get(0).getChallengeTeam());
		assertTrue(results.getResults().get(0).isAdmin());
		assertEquals(testChallengeTeam2, results.getResults().get(1).getChallengeTeam());
		assertFalse(results.getResults().get(1).isAdmin());
	}

	@Test
	public void testGetChallengeTeamsLoggedInNotFound() throws SynapseException, RestServiceException {
		when(mockSynapse.getTeamMember(anyString(), anyString())).thenThrow(new SynapseNotFoundException());
		ChallengeTeamPagedResults results = synapseClient.getChallengeTeams("1234", "2", 10, 0);
		verify(mockSynapse, times(2)).getTeamMember(anyString(), anyString());
		verify(mockSynapse).listChallengeTeams(anyString(), anyLong(), anyLong());
		assertTrue(results.getTotalNumberOfResults() == 2);
		assertEquals(testChallengeTeam1, results.getResults().get(0).getChallengeTeam());
		assertFalse(results.getResults().get(0).isAdmin());
		assertEquals(testChallengeTeam2, results.getResults().get(1).getChallengeTeam());
		assertFalse(results.getResults().get(1).isAdmin());
	}

	@Test
	public void testGetChallengeParticipants() throws RestServiceException, SynapseException {
		setupChallengeParticipants();
		UserProfilePagedResults results = synapseClient.getChallengeParticipants(false, "12", 10, 0);
		verify(mockSynapse).listChallengeParticipants(anyString(), anyBoolean(), anyLong(), anyLong());
		verify(mockSynapse).listUserProfiles(anyList());
		assertTrue(results.getTotalNumberOfResults() == 1);
		assertEquals(mockUserProfile, results.getResults().get(0));
	}

	@Test(expected = NotFoundException.class)
	public void testGetRegistratableTeamsNotMember() throws SynapseException, RestServiceException {
		// asks for team membership status. if not member, returns notfoundexception
		TeamMembershipStatus status = new TeamMembershipStatus();
		status.setIsMember(false);
		when(mockSynapse.getTeamMembershipStatus(anyString(), anyString())).thenReturn(status);
		synapseClient.getRegistratableTeams("userid", "challengeId");
	}

	@Test
	public void testGetRegistratableTeamsIsMember() throws SynapseException, RestServiceException {
		// asks for team membership status. if not member, returns notfoundexception
		TeamMembershipStatus status = new TeamMembershipStatus();
		status.setIsMember(true);
		when(mockSynapse.getTeamMembershipStatus(anyString(), anyString())).thenReturn(status);

		Team team = new Team();
		team.setId("22");
		team.setName("another team");
		List<Team> teams = new ArrayList<Team>();
		teams.add(team);
		when(mockSynapse.listTeams(anyList())).thenReturn(teams);

		PaginatedIds teamIds = new PaginatedIds();
		teamIds.setResults(Collections.singletonList("22"));
		when(mockSynapse.listRegistratableTeams(anyString(), anyLong(), anyLong())).thenReturn(teamIds);
		List<Team> results = synapseClient.getRegistratableTeams("userid", "challengeId");

		verify(mockSynapse).getChallenge(anyString());
		verify(mockSynapse).getTeamMembershipStatus(anyString(), anyString());
		verify(mockSynapse).listRegistratableTeams(anyString(), anyLong(), anyLong());
		verify(mockSynapse).listTeams(anyList());
		assertTrue(results.size() == 1);
		assertEquals(team, results.get(0));
	}

	@Test
	public void testGetChallengeEvaluationIds() throws SynapseException, RestServiceException {
		setupGetEvaluationsForEntity(testChallengeProject);

		Set<String> results = synapseClient.getChallengeEvaluationIds(testChallengeId);
		assertTrue(results.contains(EVAL_ID_1));
		assertTrue(results.contains(EVAL_ID_2));
		verify(mockSynapse).getEvaluationByContentSource(eq(testChallengeProject), anyInt(), anyInt());
		verify(mockSynapse).getChallenge(anyString());
	}

	public void setupGetEvaluationsForEntity(String sharedEntityId) throws SynapseException {
		org.sagebionetworks.reflection.model.PaginatedResults<Evaluation> testResults = getTestEvaluations(sharedEntityId);
		when(mockSynapse.getEvaluationByContentSource(anyString(), anyInt(), anyInt())).thenReturn(getEmptyPaginatedResults());
		when(mockSynapse.getEvaluationByContentSource(eq(sharedEntityId), anyInt(), anyInt())).thenReturn(testResults);
	}

	private org.sagebionetworks.reflection.model.PaginatedResults<Evaluation> getEmptyPaginatedResults() {
		org.sagebionetworks.reflection.model.PaginatedResults<Evaluation> testResults = new org.sagebionetworks.reflection.model.PaginatedResults<Evaluation>();
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		testResults.setTotalNumberOfResults(0);
		testResults.setResults(evaluationList);
		return testResults;
	}

	private org.sagebionetworks.reflection.model.PaginatedResults<Evaluation> getTestEvaluations(String sharedEntityId) {
		org.sagebionetworks.reflection.model.PaginatedResults<Evaluation> testResults = new org.sagebionetworks.reflection.model.PaginatedResults<Evaluation>();
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		Evaluation e = new Evaluation();
		e.setId(EVAL_ID_1);
		e.setContentSource(sharedEntityId);
		evaluationList.add(e);
		e = new Evaluation();
		e.setId(EVAL_ID_2);
		e.setContentSource(sharedEntityId);
		evaluationList.add(e);
		testResults.setTotalNumberOfResults(2);
		testResults.setResults(evaluationList);
		return testResults;
	}

	public void setupGetAvailableEvaluations(String sharedEntityId) throws SynapseException {
		org.sagebionetworks.reflection.model.PaginatedResults<Evaluation> testResults = getTestEvaluations(sharedEntityId);
		when(mockSynapse.getAvailableEvaluationsPaginated(anyInt(), anyInt())).thenReturn(testResults);
	}

	@Test
	public void testGetSharableEvaluations() throws SynapseException, RestServiceException, JSONObjectAdapterException {
		String myEntityId = "syn123";
		// set up 2 available evaluations associated to this entity id
		setupGetEvaluationsForEntity(myEntityId);

		// "Before" junit test setup configured so this user to have the ability to change permissions on
		// eval 2, but not on eval 1
		List<Evaluation> sharableEvaluations = synapseClient.getSharableEvaluations(myEntityId);
		// verify this is eval 2
		assertEquals(1, sharableEvaluations.size());
		Evaluation e2 = sharableEvaluations.get(0);
		assertEquals(EVAL_ID_2, e2.getId());

		// and verify that no evaluations are returned for a different entity id
		sharableEvaluations = synapseClient.getSharableEvaluations("syn456");
		assertEquals(0, sharableEvaluations.size());
	}

	@Test
	public void testGetTeamSubmissionEligibility() throws SynapseException, RestServiceException {
		// pass through
		String evaluationId = "4444";
		String teamId = "22";
		synapseClient.getTeamSubmissionEligibility(evaluationId, teamId);
		verify(mockSynapse).getTeamSubmissionEligibility(evaluationId, teamId);
	}

	@Test
	public void testSafeLongToInt() {
		int inRangeInt = 500;
		int after = SynapseClientImpl.safeLongToInt(inRangeInt);
		assertEquals(inRangeInt, after);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSafeLongToIntPositive() {
		long testValue = Integer.MAX_VALUE;
		testValue++;
		SynapseClientImpl.safeLongToInt(testValue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSafeLongToIntNegative() {
		long testValue = Integer.MIN_VALUE;
		testValue--;
		SynapseClientImpl.safeLongToInt(testValue);
	}


	/************************************
	 * Helper test methods for object creation
	 */

	public static Challenge getTestChallenge() {
		Challenge c = new Challenge();
		c.setId(testChallengeId);
		c.setParticipantTeamId(testTeam1);
		c.setProjectId(testChallengeProject);
		return c;
	}

	public static ChallengeTeam getTestChallengeTeam(String message, String teamId) {
		ChallengeTeam ct = new ChallengeTeam();
		ct.setChallengeId(testChallengeId);
		ct.setMessage(message);
		ct.setTeamId(teamId);
		return ct;
	}

	public void setupChallengeteamPagedResults() throws SynapseException {
		org.sagebionetworks.repo.model.ChallengeTeamPagedResults results = new org.sagebionetworks.repo.model.ChallengeTeamPagedResults();
		List<ChallengeTeam> resultList = new ArrayList<ChallengeTeam>();
		resultList.add(testChallengeTeam1);
		resultList.add(testChallengeTeam2);
		results.setResults(resultList);
		results.setTotalNumberOfResults(2L);
		when(mockSynapse.listChallengeTeams(anyString(), anyLong(), anyLong())).thenReturn(results);
	}

	public void setupChallengeParticipants() throws SynapseException {
		PaginatedIds participantIds = new PaginatedIds();
		participantIds.setResults(Collections.singletonList("777"));
		participantIds.setTotalNumberOfResults(1L);
		when(mockSynapse.listChallengeParticipants(anyString(), anyBoolean(), anyLong(), anyLong())).thenReturn(participantIds);
		when(mockSynapse.listUserProfiles(anyList())).thenReturn(Collections.singletonList(mockUserProfile));
	}

	public org.sagebionetworks.repo.model.ChallengePagedResults getTestChallengePagedResults() {
		org.sagebionetworks.repo.model.ChallengePagedResults results = new org.sagebionetworks.repo.model.ChallengePagedResults();
		results.setResults(Collections.singletonList(testChallenge));
		results.setTotalNumberOfResults(1L);
		return results;
	}

	public void setupListChallengesForParticipant() throws SynapseException {
		when(mockSynapse.listChallengesForParticipant(anyString(), anyLong(), anyLong())).thenReturn(getTestChallengePagedResults());
		org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader> headers = new org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader>();
		EntityHeader header = new EntityHeader();
		header.setId(testChallengeProject);
		header.setName(TEST_CHALLENGE_PROJECT_NAME);
		headers.setResults(Collections.singletonList(header));
		headers.setTotalNumberOfResults(1L);
		when(mockSynapse.getEntityHeaderBatch(anyList())).thenReturn(headers);
	}

	public static PaginatedIds getTestRegisteredTeams() {
		PaginatedIds ids = new PaginatedIds();
		List<String> idlist = new ArrayList<String>();
		idlist.add(testTeam1);
		idlist.add(testTeam2);
		ids.setResults(idlist);
		ids.setTotalNumberOfResults(2L);
		return ids;
	}
	/************
	 * 	
	 */
}
