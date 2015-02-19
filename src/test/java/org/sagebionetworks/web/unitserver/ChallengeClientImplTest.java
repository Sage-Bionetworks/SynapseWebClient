package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.EvaluationStatus;
import org.sagebionetworks.evaluation.model.Participant;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.PaginatedIds;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.message.MessageToUser;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.server.servlet.ChallengeClientImpl;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.ChallengeTeamBundle;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class ChallengeClientImplTest {
	
	
	public static final String MY_USER_PROFILE_OWNER_ID = "MyOwnerID";
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	ServiceUrlProvider mockUrlProvider;
	SynapseClient mockSynapse;
	ChallengeClientImpl synapseClient;
	String entityId = "123";
	String inviteeUserId = "900";
	UserProfile inviteeUserProfile;
	ExampleEntity entity;
	AttachmentData attachment1, attachment2;
	Annotations annos;
	UserEntityPermissions eup;
	UserEvaluationPermissions userEvaluationPermissions;
	List<EntityHeader> batchHeaderResults;
	
	String testFileName = "testFileEntity.R";
	EntityPath path;
	org.sagebionetworks.repo.model.PaginatedResults<UserGroup> pgugs;
	org.sagebionetworks.repo.model.PaginatedResults<UserProfile> pgups;
	AccessControlList acl;
	WikiPage page;
	V2WikiPage v2Page;
	S3FileHandle handle;
	Evaluation mockEvaluation;
	Participant mockParticipant;
	UserSessionData mockUserSessionData;
	UserProfile mockUserProfile;
	MembershipInvtnSubmission testInvitation;
	MessageToUser sentMessage;
	
	private static final String testChallengeId = "1";
	private static final String testTeam1 = "3322410";
	private static final String testTeam2 = "3319267";
	private static final String testChallengeProject = "syn2290704";

	private static final String EVAL_ID_1 = "eval ID 1";
	private static final String EVAL_ID_2 = "eval ID 2";
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl();
	private static JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
	private static NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(jsonEntityFactory, jsonObjectAdapter);
	private TeamMembershipStatus membershipStatus;
	@Before
	public void before() throws SynapseException, JSONObjectAdapterException{
		mockSynapse = Mockito.mock(SynapseClient.class);
		mockSynapseProvider = Mockito.mock(SynapseProvider.class);
		mockUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		mockTokenProvider = Mockito.mock(TokenProvider.class);
		
		synapseClient = new ChallengeClientImpl();
		synapseClient.setSynapseProvider(mockSynapseProvider);
		synapseClient.setTokenProvider(mockTokenProvider);
		synapseClient.setServiceUrlProvider(mockUrlProvider);
		
		//user can change permissions on eval 2, but not on 1
		userEvaluationPermissions = new UserEvaluationPermissions();
		userEvaluationPermissions.setCanChangePermissions(false);
		when(mockSynapse.getUserEvaluationPermissions(EVAL_ID_1)).thenReturn(userEvaluationPermissions);
		
		userEvaluationPermissions = new UserEvaluationPermissions();
		userEvaluationPermissions.setCanChangePermissions(true);
		when(mockSynapse.getUserEvaluationPermissions(EVAL_ID_2)).thenReturn(userEvaluationPermissions);
		
		
		BatchResults<EntityHeader> batchHeaders = new BatchResults<EntityHeader>();
		batchHeaderResults = new ArrayList<EntityHeader>();
		for (int i = 0; i < 10; i++) {
			EntityHeader h = new EntityHeader();
			h.setId("syn"+i);
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
		mockParticipant = Mockito.mock(Participant.class);
		when(mockSynapse.getParticipant(anyString(), anyString())).thenReturn(mockParticipant);
		
		when(mockSynapse.getMyProfile()).thenReturn(mockUserProfile);
		when(mockSynapse.createParticipant(anyString())).thenReturn(mockParticipant);
	}
	
	@Test
	public void testGetAvailableEvaluations() throws SynapseException, RestServiceException, MalformedURLException, JSONObjectAdapterException {
		PaginatedResults<Evaluation> testResults = new PaginatedResults<Evaluation>();
		Evaluation e = new Evaluation();
		e.setId("A test ID");
		when(mockSynapse.getAvailableEvaluationsPaginated(anyInt(),anyInt())).thenReturn(testResults);
		String evaluationsJson = synapseClient.getAvailableEvaluations();
		verify(mockSynapse).getAvailableEvaluationsPaginated(anyInt(),anyInt());
		String expectedJson = EntityFactory.createJSONStringForEntity(testResults);
		assertEquals(expectedJson, evaluationsJson);
	}
	
	@Test
	public void testGetEvaluations() throws SynapseException, RestServiceException, MalformedURLException, JSONObjectAdapterException {
		when(mockSynapse.getEvaluation(anyString())).thenReturn(new Evaluation());
		List<String> evaluationIds = new ArrayList<String>();
		evaluationIds.add("1");
		evaluationIds.add("2");
		String evaluationsJson = synapseClient.getEvaluations(evaluationIds);
		
		verify(mockSynapse, Mockito.times(2)).getEvaluation(anyString());
		
		org.sagebionetworks.web.shared.PaginatedResults<Evaluation> evaluationObjectList = 
				nodeModelCreator.createPaginatedResults(evaluationsJson, Evaluation.class);
		assertEquals(2, evaluationObjectList.getTotalNumberOfResults());
		assertEquals(2, evaluationObjectList.getResults().size());
	}

	
	@Test
	public void testHasSubmitted() throws SynapseException, RestServiceException, MalformedURLException, JSONObjectAdapterException {
		String sharedEntityId = "syn123455";
		setupGetAvailableEvaluations(sharedEntityId);
		
		PaginatedResults<Submission> submissions = new PaginatedResults<Submission>();
		//verify when all empty, hasSubmitted returns false
		when(mockSynapse.getMySubmissions(anyString(), anyLong(), anyLong())).thenReturn(submissions);
		assertFalse(synapseClient.hasSubmitted());
		
		//verify when there is a submission, it returns true
		submissions.setTotalNumberOfResults(1);
		List<Submission> submissionList = new ArrayList<Submission>();
		submissionList.add(new Submission());
		submissions.setResults(submissionList);
		assertTrue(synapseClient.hasSubmitted());
	}
	
	public void setupGetAllEvaluations(String sharedEntityId) throws SynapseException {
		PaginatedResults<Evaluation> testResults = getTestEvaluations(sharedEntityId);
		when(mockSynapse.getEvaluationsPaginated(anyInt(),anyInt())).thenReturn(testResults);
	}
	
	public void setupGetEvaluationsForEntity(String sharedEntityId) throws SynapseException {
		PaginatedResults<Evaluation> testResults = getTestEvaluations(sharedEntityId);
		when(mockSynapse.getEvaluationByContentSource(anyString(),anyInt(),anyInt())).thenReturn(getEmptyPaginatedResults());
		when(mockSynapse.getEvaluationByContentSource(eq(sharedEntityId),anyInt(),anyInt())).thenReturn(testResults);
	}
	
	private PaginatedResults<Evaluation> getEmptyPaginatedResults() {
		PaginatedResults<Evaluation> testResults = new PaginatedResults<Evaluation>();
		List<Evaluation> evaluationList = new ArrayList<Evaluation>();
		testResults.setTotalNumberOfResults(0);
		testResults.setResults(evaluationList);
		return testResults;
	}
	
	private PaginatedResults<Evaluation> getTestEvaluations(String sharedEntityId) {
		PaginatedResults<Evaluation> testResults = new PaginatedResults<Evaluation>();
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
		PaginatedResults<Evaluation> testResults = getTestEvaluations(sharedEntityId);
		when(mockSynapse.getAvailableEvaluationsPaginated(anyInt(),anyInt())).thenReturn(testResults);
	}
	
	@Test
	public void testGetSharableEvaluations() throws SynapseException, RestServiceException, JSONObjectAdapterException {
		String myEntityId = "syn123";
		//set up 2 available evaluations associated to this entity id
		setupGetEvaluationsForEntity(myEntityId);
		
		//"Before" junit test setup configured so this user to have the ability to change permissions on eval 2, but not on eval 1
		ArrayList<String> sharableEvaluations = synapseClient.getSharableEvaluations(myEntityId);
		//verify this is eval 2
		assertEquals(1, sharableEvaluations.size());
		Evaluation e2 = nodeModelCreator.createJSONEntity(sharableEvaluations.get(0), Evaluation.class);
		assertEquals(EVAL_ID_2, e2.getId());
		
		//and verify that no evaluations are returned for a different entity id
		sharableEvaluations = synapseClient.getSharableEvaluations("syn456");
		assertEquals(0, sharableEvaluations.size());
	}
	
	
	@Test
	public void testSafeLongToInt() {
		int inRangeInt = 500;
		int after = SynapseClientImpl.safeLongToInt(inRangeInt);
		assertEquals(inRangeInt, after);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testSafeLongToIntPositive() {
		long testValue = Integer.MAX_VALUE;
		testValue++;
		SynapseClientImpl.safeLongToInt(testValue);
	}
	
	@Test (expected=IllegalArgumentException.class)
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
	public static ChallengeTeamPagedResults getTestChallengeTeamPagedResults(){
		ChallengeTeamPagedResults results = new ChallengeTeamPagedResults();
		ChallengeTeamBundle bundle1 = new ChallengeTeamBundle(getTestChallengeTeam("join the first team", testTeam1), true);
		ChallengeTeamBundle bundle2 = new ChallengeTeamBundle(getTestChallengeTeam("join the second team", testTeam2), false);
		List<ChallengeTeamBundle> resultList = new ArrayList<ChallengeTeamBundle>();
		resultList.add(bundle1);
		resultList.add(bundle2);
		results.setResults(resultList);
		results.setTotalNumberOfResults(4L);
		return results;
	}
	
	public static ChallengeTeamPagedResults getTestChallengeTeamPagedEmptyResults(){
		ChallengeTeamPagedResults results = new ChallengeTeamPagedResults();
		List<ChallengeTeamBundle> resultList = new ArrayList<ChallengeTeamBundle>();
		results.setResults(resultList);
		results.setTotalNumberOfResults(0L);
		return results;
	}

	
	public static UserProfilePagedResults getTestUserProfilePagedResults(org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException{
		UserProfilePagedResults results = new UserProfilePagedResults();
		UserProfile profile1 = synapseClient.getUserProfile("1418535");
		UserProfile profile2 = synapseClient.getUserProfile("1118328");
		List<UserProfile> resultList = new ArrayList<UserProfile>();
		resultList.add(profile1);
		resultList.add(profile2);
		results.setResults(resultList);
		results.setTotalNumberOfResults(4L);
		return results;
	}
	
	public static UserProfilePagedResults getTestUserProfilePagedEmptyResults(org.sagebionetworks.client.SynapseClient synapseClient) throws SynapseException{
		UserProfilePagedResults results = new UserProfilePagedResults();
		List<UserProfile> resultList = new ArrayList<UserProfile>();
		results.setResults(resultList);
		results.setTotalNumberOfResults(0L);
		return results;
	}

	
	public static org.sagebionetworks.repo.model.ChallengePagedResults getTestChallengePagedResults() {
		org.sagebionetworks.repo.model.ChallengePagedResults results = new org.sagebionetworks.repo.model.ChallengePagedResults();
		List<Challenge> challangeList = new ArrayList<Challenge>();
		challangeList.add(getTestChallenge());
		results.setResults(challangeList);
		results.setTotalNumberOfResults(1L);
		return results;
	}
	
	public static PaginatedIds getTestRegisteredTeams(){
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
