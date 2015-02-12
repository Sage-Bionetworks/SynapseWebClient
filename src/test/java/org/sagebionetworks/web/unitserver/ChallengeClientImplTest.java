package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ANNOTATIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY_PATH;
import static org.sagebionetworks.web.shared.EntityBundleTransport.HAS_CHILDREN;
import static org.sagebionetworks.web.shared.EntityBundleTransport.PERMISSIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.UNMET_ACCESS_REQUIREMENTS;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.EvaluationStatus;
import org.sagebionetworks.evaluation.model.Participant;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityIdList;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.LayerTypeNames;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.LocationTypeNames;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.MembershipRqstSubmission;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.RestResourceList;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.VariableContentPaginatedResults;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.repo.model.file.ChunkRequest;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.CompleteAllChunksRequest;
import org.sagebionetworks.repo.model.file.CompleteChunkedFileRequest;
import org.sagebionetworks.repo.model.file.CreateChunkedFileTokenRequest;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.file.State;
import org.sagebionetworks.repo.model.file.UploadDaemonStatus;
import org.sagebionetworks.repo.model.message.MessageToUser;
import org.sagebionetworks.repo.model.principal.AddEmailInfo;
import org.sagebionetworks.repo.model.quiz.PassingRecord;
import org.sagebionetworks.repo.model.quiz.Quiz;
import org.sagebionetworks.repo.model.quiz.QuizResponse;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
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
import org.sagebionetworks.web.client.widget.table.v2.TableModelUtils;
import org.sagebionetworks.web.server.servlet.ChallengeClientImpl;
import org.sagebionetworks.web.server.servlet.MarkdownCacheRequest;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.AccessRequirementUtils;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.MembershipInvitationBundle;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.common.cache.Cache;

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
	public void testCreateSubmission() throws SynapseException, RestServiceException, MalformedURLException, JSONObjectAdapterException {
		Submission inputSubmission = new Submission();
		inputSubmission.setId("my submission id");
		when(mockSynapse.createSubmission(any(Submission.class), anyString())).thenReturn(inputSubmission);
		Submission returnSubmission = synapseClient.createSubmission(inputSubmission, "fakeEtag");
		verify(mockSynapse).createSubmission(any(Submission.class), anyString());
		assertEquals(inputSubmission, returnSubmission);
	}
	
	private void setupTestSubmitterAliases() throws SynapseException{
		//set up 2 available evaluations
		PaginatedResults<Evaluation> availableEvaluations = new PaginatedResults<Evaluation>();
		List<Evaluation> evalResults = new ArrayList<Evaluation>();
		Evaluation e = new Evaluation();
		String eval1Id ="evaluation1"; 
		e.setId(eval1Id);
		evalResults.add(e);
		e = new Evaluation();
		String eval2Id = "evaluation2";
		e.setId(eval2Id);
		evalResults.add(e);
		availableEvaluations.setResults(evalResults);
		when(mockSynapse.getAvailableEvaluationsPaginated(anyInt(),anyInt())).thenReturn(availableEvaluations);
		
		//test sorting, uniqueness, and empty/null values
		Submission[] submissions = new Submission[6];
		Date date = new Date();
		for (int i = 0; i < submissions.length; i++) {
			submissions[i] = new Submission();
			//submission 0 is the most recently used (largest date time), and submission 6 is the oldest
			submissions[i].setCreatedOn(new Date(date.getTime() - i));	 
			submissions[i].setSubmitterAlias("Alias " + i);
		}
		//set a duplicate
		submissions[3].setSubmitterAlias("Alias 0");
		//and add a null and empty string submitter alias, to verify that these are removed
		submissions[4].setSubmitterAlias(null);
		submissions[5].setSubmitterAlias("");
		
		//assign 2 submissions to evaluation1, and the other 4 submissions to evaluation2
		//mix them up to test sort
		PaginatedResults<Submission> submissionSet1 = new PaginatedResults<Submission>();
		List<Submission> submissionList = new ArrayList<Submission>();
		submissionList.add(submissions[0]);
		submissionList.add(submissions[2]);
		submissionSet1.setTotalNumberOfResults(2);
		submissionSet1.setResults(submissionList);
		
		PaginatedResults<Submission> submissionSet2 = new PaginatedResults<Submission>();
		submissionList = new ArrayList<Submission>();
		submissionList.add(submissions[1]);
		submissionList.add(submissions[3]);
		submissionList.add(submissions[4]);
		submissionList.add(submissions[5]);
		submissionSet2.setTotalNumberOfResults(4);
		submissionSet2.setResults(submissionList);
		when(mockSynapse.getMySubmissions(eq(eval1Id), anyLong(), anyLong())).thenReturn(submissionSet1);
		when(mockSynapse.getMySubmissions(eq(eval2Id), anyLong(), anyLong())).thenReturn(submissionSet2);
	}
	
	@Test
	public void testGetAvailableEvaluationSubmitterAliases() throws SynapseException, RestServiceException, JSONObjectAdapterException {
		setupTestSubmitterAliases();
		String resourceListJson = synapseClient.getAvailableEvaluationsSubmitterAliases();
		RestResourceList resourceList = EntityFactory.createEntityFromJSONString(resourceListJson, RestResourceList.class);
		List<String> submitterAliasList = resourceList.getList();
		//3 unique submitter aliases across the evaluations
		assertEquals(3, submitterAliasList.size());
		
		//order should be Alias 0, Alias 1, Alias 2
		for (int i = 0; i < submitterAliasList.size(); i++) {
			assertEquals("Alias " + i, submitterAliasList.get(i));
		}
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
}
