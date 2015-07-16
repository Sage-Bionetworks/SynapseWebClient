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
import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;
import static org.sagebionetworks.repo.model.EntityBundle.HAS_CHILDREN;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ROOT_WIKI_ID;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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
import org.sagebionetworks.evaluation.model.UserEvaluationPermissions;
import org.sagebionetworks.reflection.model.PaginatedResults;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityIdList;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.MembershipRqstSubmission;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMember;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
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
import org.sagebionetworks.repo.model.message.NotificationSettingsSignedToken;
import org.sagebionetworks.repo.model.message.Settings;
import org.sagebionetworks.repo.model.principal.AddEmailInfo;
import org.sagebionetworks.repo.model.project.ExternalS3StorageLocationSetting;
import org.sagebionetworks.repo.model.project.ExternalStorageLocationSetting;
import org.sagebionetworks.repo.model.project.ProjectSetting;
import org.sagebionetworks.repo.model.project.ProjectSettingsType;
import org.sagebionetworks.repo.model.project.StorageLocationSetting;
import org.sagebionetworks.repo.model.project.UploadDestinationListSetting;
import org.sagebionetworks.repo.model.provenance.Activity;
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
import org.sagebionetworks.util.SerializationUtils;
import org.sagebionetworks.web.client.view.TeamRequestBundle;
import org.sagebionetworks.web.server.servlet.MarkdownCacheRequest;
import org.sagebionetworks.web.server.servlet.NotificationTokenType;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.AccessRequirementUtils;
import org.sagebionetworks.web.shared.EntityBundlePlus;
import org.sagebionetworks.web.shared.OpenTeamInvitationBundle;
import org.sagebionetworks.web.shared.ProjectPagedResults;
import org.sagebionetworks.web.shared.TeamBundle;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.common.cache.Cache;

/**
 * Test for the SynapseClientImpl
 * 
 * @author John
 * 
 */
public class SynapseClientImplTest {
	public static final String TEST_HOME_PAGE_BASE = "http://mysynapse.org/";
	public static final String MY_USER_PROFILE_OWNER_ID = "MyOwnerID";
	
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	ServiceUrlProvider mockUrlProvider;
	SynapseClient mockSynapse;
	SynapseClientImpl synapseClient;
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
	org.sagebionetworks.reflection.model.PaginatedResults<Team> pguts;
	Team teamA, teamZ;
	AccessControlList acl;
	WikiPage page;
	V2WikiPage v2Page;
	S3FileHandle handle;
	Evaluation mockEvaluation;
	Participant mockParticipant;
	UserSessionData mockUserSessionData;
	UserProfile mockUserProfile;
	MembershipInvtnSubmission testInvitation;
	PaginatedResults mockPaginatedMembershipRequest;
	Activity mockActivity;

	MessageToUser sentMessage;
	Long storageLocationId = 9090L;
	UserProfile testUserProfile;
	Long version = 1L;
	
	//Token testing
	NotificationSettingsSignedToken notificationSettingsToken;
	JoinTeamSignedToken joinTeamToken;
	String encodedJoinTeamToken, encodedNotificationSettingsToken;
	
	private static final String testUserId = "myUserId";

	private static final String EVAL_ID_1 = "eval ID 1";
	private static final String EVAL_ID_2 = "eval ID 2";
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl();
//	private static JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(
//			adapterFactory);
	private TeamMembershipStatus membershipStatus;

	@Before
	public void before() throws SynapseException, JSONObjectAdapterException {
		mockSynapse = Mockito.mock(SynapseClient.class);
		mockSynapseProvider = Mockito.mock(SynapseProvider.class);
		mockUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		mockTokenProvider = Mockito.mock(TokenProvider.class);
		mockPaginatedMembershipRequest = Mockito.mock(PaginatedResults.class);
		mockActivity = Mockito.mock(Activity.class);
		when(mockPaginatedMembershipRequest.getTotalNumberOfResults()).thenReturn(3L);
		synapseClient = new SynapseClientImpl();
		synapseClient.setSynapseProvider(mockSynapseProvider);
		synapseClient.setTokenProvider(mockTokenProvider);
		synapseClient.setServiceUrlProvider(mockUrlProvider);

		// Setup the the entity
		entity = new ExampleEntity();
		entity.setId(entityId);
		entity.setEntityType(ExampleEntity.class.getName());
		entity.setModifiedBy(testUserId);
		// the mock synapse should return this object
		when(mockSynapse.getEntityById(entityId)).thenReturn(entity);
		// Setup the annotations
		annos = new Annotations();
		annos.setId(entityId);
		annos.addAnnotation("string", "a string value");
		// the mock synapse should return this object
		when(mockSynapse.getAnnotations(entityId)).thenReturn(annos);
		// Setup the Permissions
		eup = new UserEntityPermissions();
		eup.setCanDelete(true);
		eup.setCanView(false);
		eup.setOwnerPrincipalId(999L);
		// the mock synapse should return this object
		when(mockSynapse.getUsersEntityPermissions(entityId)).thenReturn(eup);

		// user can change permissions on eval 2, but not on 1
		userEvaluationPermissions = new UserEvaluationPermissions();
		userEvaluationPermissions.setCanChangePermissions(false);
		when(mockSynapse.getUserEvaluationPermissions(EVAL_ID_1)).thenReturn(
				userEvaluationPermissions);

		userEvaluationPermissions = new UserEvaluationPermissions();
		userEvaluationPermissions.setCanChangePermissions(true);
		when(mockSynapse.getUserEvaluationPermissions(EVAL_ID_2)).thenReturn(
				userEvaluationPermissions);

		// Setup the path
		path = new EntityPath();
		path.setPath(new ArrayList<EntityHeader>());
		EntityHeader header = new EntityHeader();
		header.setId(entityId);
		header.setName("RomperRuuuu");
		path.getPath().add(header);
		// the mock synapse should return this object
		when(mockSynapse.getEntityPath(entityId)).thenReturn(path);

		pgugs = new org.sagebionetworks.reflection.model.PaginatedResults<UserGroup>();
		List<UserGroup> ugs = new ArrayList<UserGroup>();
		ugs.add(new UserGroup());
		pgugs.setResults(ugs);
		when(mockSynapse.getGroups(anyInt(), anyInt())).thenReturn(pgugs);

		pgups = new org.sagebionetworks.reflection.model.PaginatedResults<UserProfile>();
		List<UserProfile> ups = new ArrayList<UserProfile>();
		ups.add(new UserProfile());
		pgups.setResults(ups);
		when(mockSynapse.getUsers(anyInt(), anyInt())).thenReturn(pgups);

		pguts = new org.sagebionetworks.reflection.model.PaginatedResults<Team>();
		List<Team> uts = new ArrayList<Team>();
		teamZ = new Team();
		teamZ.setId("1");
		teamZ.setName("zygote");
		uts.add(teamZ);
		teamA = new Team();
		teamA.setId("2");
		teamA.setName("Amplitude");
		uts.add(teamA);
		pguts.setResults(uts);
		when(mockSynapse.getTeamsForUser(anyString(), anyInt(), anyInt()))
				.thenReturn(pguts);

		acl = new AccessControlList();
		acl.setId("sys999");
		Set<ResourceAccess> ras = new HashSet<ResourceAccess>();
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(101L);
		ra.setAccessType(AclUtils
				.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER));
		acl.setResourceAccess(ras);
		when(mockSynapse.getACL(anyString())).thenReturn(acl);
		when(mockSynapse.createACL((AccessControlList) any())).thenReturn(acl);
		when(mockSynapse.updateACL((AccessControlList) any())).thenReturn(acl);
		when(mockSynapse.updateACL((AccessControlList) any(), eq(true)))
				.thenReturn(acl);
		when(mockSynapse.updateACL((AccessControlList) any(), eq(false)))
				.thenReturn(acl);

		EntityHeader bene = new EntityHeader();
		bene.setId("syn999");
		when(mockSynapse.getEntityBenefactor(anyString())).thenReturn(bene);

		org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader> batchHeaders = new org.sagebionetworks.reflection.model.PaginatedResults<EntityHeader>();
		batchHeaderResults = new ArrayList<EntityHeader>();
		for (int i = 0; i < 10; i++) {
			EntityHeader h = new EntityHeader();
			h.setId("syn" + i);
			batchHeaderResults.add(h);
		}
		batchHeaders.setResults(batchHeaderResults);
		when(mockSynapse.getEntityHeaderBatch(anyList())).thenReturn(
				batchHeaders);

		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		accessRequirements.add(createAccessRequirement(ACCESS_TYPE.DOWNLOAD));

		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH
				| HAS_CHILDREN | ACCESS_REQUIREMENTS
				| UNMET_ACCESS_REQUIREMENTS;
		int emptyMask = 0;
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(entity);
		bundle.setAnnotations(annos);
		bundle.setPermissions(eup);
		bundle.setPath(path);
		bundle.setHasChildren(false);
		bundle.setAccessRequirements(accessRequirements);
		bundle.setUnmetAccessRequirements(accessRequirements);
		when(mockSynapse.getEntityBundle(anyString(), Matchers.eq(mask)))
				.thenReturn(bundle);
		when(mockSynapse.getEntityBundle(anyString(), Matchers.eq(ENTITY | ANNOTATIONS | ROOT_WIKI_ID)))
				.thenReturn(bundle);

		EntityBundle emptyBundle = new EntityBundle();
		when(mockSynapse.getEntityBundle(anyString(), Matchers.eq(emptyMask)))
				.thenReturn(emptyBundle);

		when(mockSynapse.canAccess("syn101", ACCESS_TYPE.READ))
				.thenReturn(true);

		page = new WikiPage();
		page.setId("testId");
		page.setMarkdown("my markdown");
		page.setParentWikiId(null);
		page.setTitle("A Title");
		v2Page = new V2WikiPage();
		v2Page.setId("v2TestId");
		v2Page.setEtag("122333");
		handle = new S3FileHandle();
		handle.setId("4422");
		handle.setBucketName("bucket");
		handle.setFileName(testFileName);
		handle.setKey("key");
		when(mockSynapse.getRawFileHandle(anyString())).thenReturn(handle);
		when(
				mockSynapse
						.completeChunkFileUpload(any(CompleteChunkedFileRequest.class)))
				.thenReturn(handle);
		org.sagebionetworks.reflection.model.PaginatedResults<AccessRequirement> ars = new org.sagebionetworks.reflection.model.PaginatedResults<AccessRequirement>();
		ars.setTotalNumberOfResults(0);
		ars.setResults(new ArrayList<AccessRequirement>());
		when(
				mockSynapse
						.getAccessRequirements(any(RestrictableObjectDescriptor.class)))
				.thenReturn(ars);
		when(
				mockSynapse.getUnmetAccessRequirements(
						any(RestrictableObjectDescriptor.class),
						any(ACCESS_TYPE.class))).thenReturn(ars);
		mockEvaluation = Mockito.mock(Evaluation.class);
		when(mockEvaluation.getStatus()).thenReturn(EvaluationStatus.OPEN);
		when(mockSynapse.getEvaluation(anyString())).thenReturn(mockEvaluation);
		mockUserSessionData = Mockito.mock(UserSessionData.class);
		mockUserProfile = Mockito.mock(UserProfile.class);
		when(mockSynapse.getUserSessionData()).thenReturn(mockUserSessionData);
		when(mockUserSessionData.getProfile()).thenReturn(mockUserProfile);
		when(mockUserProfile.getOwnerId()).thenReturn(MY_USER_PROFILE_OWNER_ID);
		mockParticipant = Mockito.mock(Participant.class);
		when(mockSynapse.getParticipant(anyString(), anyString())).thenReturn(
				mockParticipant);

		when(mockSynapse.getMyProfile()).thenReturn(mockUserProfile);
		when(mockSynapse.createParticipant(anyString())).thenReturn(
				mockParticipant);

		UploadDaemonStatus status = new UploadDaemonStatus();
		String fileHandleId = "myFileHandleId";
		status.setFileHandleId(fileHandleId);
		status.setState(State.COMPLETED);
		when(mockSynapse.getCompleteUploadDaemonStatus(anyString()))
				.thenReturn(status);

		status = new UploadDaemonStatus();
		status.setState(State.PROCESSING);
		status.setPercentComplete(.05d);
		when(mockSynapse.startUploadDeamon(any(CompleteAllChunksRequest.class)))
				.thenReturn(status);

		PaginatedResults<MembershipInvitation> openInvites = new PaginatedResults<MembershipInvitation>();
		openInvites.setTotalNumberOfResults(0);
		when(
				mockSynapse.getOpenMembershipInvitations(anyString(),
						anyString(), anyLong(), anyLong())).thenReturn(
				openInvites);

		PaginatedResults<MembershipRequest> openRequests = new PaginatedResults<MembershipRequest>();
		openRequests.setTotalNumberOfResults(0);
		when(
				mockSynapse.getOpenMembershipRequests(anyString(), anyString(),
						anyLong(), anyLong())).thenReturn(openRequests);
		membershipStatus = new TeamMembershipStatus();
		membershipStatus.setCanJoin(false);
		membershipStatus.setHasOpenInvitation(false);
		membershipStatus.setHasOpenRequest(false);
		membershipStatus.setHasUnmetAccessRequirement(false);
		membershipStatus.setIsMember(false);
		membershipStatus.setMembershipApprovalRequired(false);
		when(mockSynapse.getTeamMembershipStatus(anyString(), anyString()))
				.thenReturn(membershipStatus);

		sentMessage = new MessageToUser();
		sentMessage.setId("987");
		when(
				mockSynapse.sendMessage(any(MessageToUser.class))).thenReturn(sentMessage);

		// getMyProjects getUserProjects
		PaginatedResults headers = new PaginatedResults<ProjectHeader>();
		headers.setTotalNumberOfResults(1100);
		List<ProjectHeader> projectHeaders = new ArrayList();
		List<UserProfile> userProfile = new ArrayList();
		projectHeaders.add(new ProjectHeader());
		headers.setResults(projectHeaders);
		when(
				mockSynapse.getMyProjects(any(ProjectListType.class),
						any(ProjectListSortColumn.class),
						any(SortDirection.class), anyInt(), anyInt()))
				.thenReturn(headers);
		when(
				mockSynapse.getProjectsFromUser(anyLong(),
						any(ProjectListSortColumn.class),
						any(SortDirection.class), anyInt(), anyInt()))
				.thenReturn(headers);
		when(
				mockSynapse.getProjectsForTeam(anyLong(),
						any(ProjectListSortColumn.class),
						any(SortDirection.class), anyInt(), anyInt()))
				.thenReturn(headers);
		
		testUserProfile = new UserProfile();
		testUserProfile.setUserName("Test User");
		when(mockSynapse.getUserProfile(eq(testUserId))).thenReturn(
				testUserProfile);
		
		joinTeamToken = new JoinTeamSignedToken();
		joinTeamToken.setHmac("98765");
		joinTeamToken.setMemberId("1");
		joinTeamToken.setTeamId("2");
		joinTeamToken.setUserId("3");
		encodedJoinTeamToken = SerializationUtils.serializeAndHexEncode(joinTeamToken);
		
		notificationSettingsToken = new NotificationSettingsSignedToken();
		notificationSettingsToken.setHmac("987654");
		notificationSettingsToken.setSettings(new Settings());
		notificationSettingsToken.setUserId("4");
		encodedNotificationSettingsToken = SerializationUtils.serializeAndHexEncode(notificationSettingsToken);		
	}

	private AccessRequirement createAccessRequirement(ACCESS_TYPE type) {
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirement.setConcreteType(TermsOfUseAccessRequirement.class
				.getName());
		RestrictableObjectDescriptor descriptor = new RestrictableObjectDescriptor();
		descriptor.setId("101");
		descriptor.setType(RestrictableObjectType.ENTITY);
		accessRequirement.setSubjectIds(Arrays
				.asList(new RestrictableObjectDescriptor[] { descriptor }));
		accessRequirement.setAccessType(type);
		return accessRequirement;
	}

	private void setupTeamInvitations() throws SynapseException {
		ArrayList<MembershipInvtnSubmission> testInvitations = new ArrayList<MembershipInvtnSubmission>();
		testInvitation = new MembershipInvtnSubmission();
		testInvitation.setId("628319");
		testInvitation.setInviteeId(inviteeUserId);
		testInvitations.add(testInvitation);
		PaginatedResults<MembershipInvtnSubmission> paginatedInvitations = new PaginatedResults<MembershipInvtnSubmission>();
		paginatedInvitations.setResults(testInvitations);
		when(
				mockSynapse.getOpenMembershipInvitationSubmissions(anyString(),
						anyString(), anyLong(), anyLong())).thenReturn(
				paginatedInvitations);

		inviteeUserProfile = new UserProfile();
		inviteeUserProfile.setUserName("Invitee User");
		inviteeUserProfile.setOwnerId(inviteeUserId);
		when(mockSynapse.getUserProfile(eq(inviteeUserId))).thenReturn(
				inviteeUserProfile);

	}

	@Test
	public void testGetEntityBundleAll() throws RestServiceException {
		// Make sure we can get all parts of the bundel
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH
				| HAS_CHILDREN | ACCESS_REQUIREMENTS
				| UNMET_ACCESS_REQUIREMENTS;
		EntityBundle bundle = synapseClient.getEntityBundle(entityId, mask);
		assertNotNull(bundle);
		// We should have all of the strings
		assertNotNull(bundle.getEntity());
		assertNotNull(bundle.getAnnotations());
		assertNotNull(bundle.getPath());
		assertNotNull(bundle.getPermissions());
		assertNotNull(bundle.getHasChildren());
		assertNotNull(bundle.getAccessRequirements());
		assertNotNull(bundle.getUnmetAccessRequirements());
	}

	@Test
	public void testGetEntityBundleNone() throws RestServiceException {
		// Make sure all are null
		int mask = 0x0;
		EntityBundle bundle = synapseClient.getEntityBundle(entityId, mask);
		assertNotNull(bundle);
		// We should have all of the strings
		assertNull(bundle.getEntity());
		assertNull(bundle.getAnnotations());
		assertNull(bundle.getPath());
		assertNull(bundle.getPermissions());
		assertNull(bundle.getHasChildren());
		assertNull(bundle.getAccessRequirements());
		assertNull(bundle.getUnmetAccessRequirements());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseEntityFromJsonNoType()
			throws JSONObjectAdapterException {
		ExampleEntity example = new ExampleEntity();
		example.setName("some name");
		example.setDescription("some description");
		// do not set the type
		String json = EntityFactory.createJSONStringForEntity(example);
		// This will fail as the type is required
		synapseClient.parseEntityFromJson(json);
	}

	@Test
	public void testParseEntityFromJson() throws JSONObjectAdapterException {
		ExampleEntity example = new ExampleEntity();
		example.setName("some name");
		example.setDescription("some description");
		example.setEntityType(ExampleEntity.class.getName());
		String json = EntityFactory.createJSONStringForEntity(example);
		// System.out.println(json);
		// Now make sure this can be read back
		ExampleEntity clone = (ExampleEntity) synapseClient
				.parseEntityFromJson(json);
		assertEquals(example, clone);
	}

	@Test
	public void testCreateOrUpdateEntityFalse()
			throws JSONObjectAdapterException, RestServiceException,
			SynapseException {
		ExampleEntity in = new ExampleEntity();
		in.setName("some name");
		in.setDescription("some description");
		in.setEntityType(ExampleEntity.class.getName());

		ExampleEntity out = new ExampleEntity();
		out.setName("some name");
		out.setDescription("some description");
		out.setEntityType(ExampleEntity.class.getName());
		out.setId("syn123");
		out.setEtag("45");

		// when in comes in then return out.
		when(mockSynapse.putEntity(in)).thenReturn(out);
		String result = synapseClient.createOrUpdateEntity(in, null, false);
		assertEquals(out.getId(), result);
		verify(mockSynapse).putEntity(in);
	}

	@Test
	public void testCreateOrUpdateEntityTrue()
			throws JSONObjectAdapterException, RestServiceException,
			SynapseException {
		ExampleEntity in = new ExampleEntity();
		in.setName("some name");
		in.setDescription("some description");
		in.setEntityType(ExampleEntity.class.getName());

		ExampleEntity out = new ExampleEntity();
		out.setName("some name");
		out.setDescription("some description");
		out.setEntityType(ExampleEntity.class.getName());
		out.setId("syn123");
		out.setEtag("45");

		// when in comes in then return out.
		when(mockSynapse.createEntity(in)).thenReturn(out);
		String result = synapseClient.createOrUpdateEntity(in, null, true);
		assertEquals(out.getId(), result);
		verify(mockSynapse).createEntity(in);
	}

	@Test
	public void testCreateOrUpdateEntityTrueWithAnnos()
			throws JSONObjectAdapterException, RestServiceException,
			SynapseException {
		ExampleEntity in = new ExampleEntity();
		in.setName("some name");
		in.setDescription("some description");
		in.setEntityType(ExampleEntity.class.getName());

		Annotations annos = new Annotations();
		annos.addAnnotation("someString", "one");

		ExampleEntity out = new ExampleEntity();
		out.setName("some name");
		out.setDescription("some description");
		out.setEntityType(ExampleEntity.class.getName());
		out.setId("syn123");
		out.setEtag("45");

		// when in comes in then return out.
		when(mockSynapse.createEntity(in)).thenReturn(out);
		String result = synapseClient.createOrUpdateEntity(in, annos, true);
		assertEquals(out.getId(), result);
		verify(mockSynapse).createEntity(in);
		annos.setEtag(out.getEtag());
		annos.setId(out.getId());
		verify(mockSynapse).updateAnnotations(out.getId(), annos);
	}

	@Test
	public void testGetEntityBenefactorAcl() throws Exception {
		EntityBundle bundle = new EntityBundle();
		bundle.setBenefactorAcl(acl);
		when(mockSynapse.getEntityBundle("syn101", EntityBundle.BENEFACTOR_ACL))
				.thenReturn(bundle);
		AccessControlList clone = synapseClient
				.getEntityBenefactorAcl("syn101");
		assertEquals(acl, clone);
	}

	@Test
	public void testCreateAcl() throws Exception {
		AccessControlList clone = synapseClient.createAcl(acl);
		assertEquals(acl, clone);
	}

	@Test
	public void testUpdateAcl() throws Exception {
		AccessControlList clone = synapseClient.updateAcl(acl);
		assertEquals(acl, clone);
	}

	@Test
	public void testUpdateAclRecursive() throws Exception {
		AccessControlList clone = synapseClient.updateAcl(acl, true);
		assertEquals(acl, clone);
		verify(mockSynapse).updateACL(any(AccessControlList.class), eq(true));
	}

	@Test
	public void testDeleteAcl() throws Exception {
		EntityBundle bundle = new EntityBundle();
		bundle.setBenefactorAcl(acl);
		when(mockSynapse.getEntityBundle("syn101", EntityBundle.BENEFACTOR_ACL))
				.thenReturn(bundle);
		AccessControlList clone = synapseClient.deleteAcl("syn101");
		assertEquals(acl, clone);
	}

	@Test
	public void testHasAccess() throws Exception {
		assertTrue(synapseClient.hasAccess("syn101", "READ"));
	}


	@Test
	public void testGetUserProfile() throws Exception {
		// verify call is directly calling the synapse client provider
		String testRepoUrl = "http://mytestrepourl";
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(testRepoUrl);
		UserProfile userProfile = synapseClient.getUserProfile(testUserId);
		assertEquals(userProfile, testUserProfile);
	}

	@Test
	public void testGetProjectById() throws Exception {
		String projectId = "syn1029";
		Project project = new Project();
		project.setId(projectId);
		when(mockSynapse.getEntityById(projectId)).thenReturn(project);

		Project actualProject = synapseClient.getProject(projectId);
		assertEquals(project, actualProject);
	}

	@Test
	public void testGetJSONEntity() throws Exception {

		JSONObject json = EntityFactory.createJSONObjectForEntity(entity);
		Mockito.when(mockSynapse.getEntity(anyString())).thenReturn(json);

		String testRepoUri = "/testservice";

		synapseClient.getJSONEntity(testRepoUri);
		// verify that this call uses Synapse.getEntity(testRepoUri)
		verify(mockSynapse).getEntity(testRepoUri);
	}

	@Test
	public void testGetWikiHeaderTree() throws Exception {
		PaginatedResults<WikiHeader> headerTreeResults = new PaginatedResults<WikiHeader>();
		when(mockSynapse.getWikiHeaderTree(anyString(), any(ObjectType.class)))
				.thenReturn(headerTreeResults);
		synapseClient.getWikiHeaderTree("testId", ObjectType.ENTITY.toString());
		verify(mockSynapse).getWikiHeaderTree(anyString(),
				eq(ObjectType.ENTITY));
	}
	
	@Test
	public void testGetWikiAttachmentHandles() throws Exception {
		FileHandleResults testResults = new FileHandleResults();
		Mockito.when(
				mockSynapse
						.getWikiAttachmenthHandles(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class)))
				.thenReturn(testResults);
		synapseClient.getWikiAttachmentHandles(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"));
		verify(mockSynapse).getWikiAttachmenthHandles(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));
	}

	@Test
	public void testDeleteV2WikiPage() throws Exception {
		synapseClient.deleteV2WikiPage(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"));
		verify(mockSynapse).deleteV2WikiPage(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));
	}

	@Test
	public void testGetV2WikiPage() throws Exception {
		Mockito.when(
				mockSynapse
						.getV2WikiPage(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class)))
				.thenReturn(v2Page);
		synapseClient.getV2WikiPage(new WikiPageKey("syn123", ObjectType.ENTITY
				.toString(), "20"));
		verify(mockSynapse).getV2WikiPage(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));

		Mockito.when(
				mockSynapse
						.getVersionOfV2WikiPage(
								any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
								any(Long.class))).thenReturn(v2Page);
		synapseClient.getVersionOfV2WikiPage(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"), new Long(0));
		verify(mockSynapse).getVersionOfV2WikiPage(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
				any(Long.class));
	}

	@Test
	public void testUpdateV2WikiPage() throws Exception {
		Mockito.when(
				mockSynapse.updateV2WikiPage(anyString(),
						any(ObjectType.class), any(V2WikiPage.class)))
				.thenReturn(v2Page);
		synapseClient.updateV2WikiPage("testId", ObjectType.ENTITY.toString(),
				v2Page);
		verify(mockSynapse).updateV2WikiPage(anyString(),
				any(ObjectType.class), any(V2WikiPage.class));
	}

	@Test
	public void testRestoreV2WikiPage() throws Exception {
		String wikiId = "syn123";
		Mockito.when(
				mockSynapse.restoreV2WikiPage(anyString(),
						any(ObjectType.class), any(String.class), anyLong()))
				.thenReturn(v2Page);
		synapseClient.restoreV2WikiPage("ownerId",
				ObjectType.ENTITY.toString(), wikiId, new Long(2));
		verify(mockSynapse).restoreV2WikiPage(anyString(),
				any(ObjectType.class), any(String.class), anyLong());
	}

	@Test
	public void testGetV2WikiHeaderTree() throws Exception {
		PaginatedResults<V2WikiHeader> headerTreeResults = new PaginatedResults<V2WikiHeader>();
		when(
				mockSynapse.getV2WikiHeaderTree(anyString(),
						any(ObjectType.class))).thenReturn(headerTreeResults);
		synapseClient.getV2WikiHeaderTree("testId",
				ObjectType.ENTITY.toString());
		verify(mockSynapse).getV2WikiHeaderTree(anyString(),
				any(ObjectType.class));
	}

	@Test
	public void testGetV2WikiOrderHint() throws Exception {
		V2WikiOrderHint orderHint = new V2WikiOrderHint();
		when(
				mockSynapse
						.getV2OrderHint(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class)))
				.thenReturn(orderHint);
		synapseClient.getV2WikiOrderHint(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"));
		verify(mockSynapse).getV2OrderHint(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));
	}

	@Test
	public void testUpdateV2WikiOrderHint() throws Exception {
		V2WikiOrderHint orderHint = new V2WikiOrderHint();
		when(mockSynapse.updateV2WikiOrderHint(any(V2WikiOrderHint.class)))
				.thenReturn(orderHint);
		synapseClient.updateV2WikiOrderHint(orderHint);
		verify(mockSynapse).updateV2WikiOrderHint(any(V2WikiOrderHint.class));
	}

	@Test
	public void testGetV2WikiHistory() throws Exception {
		PaginatedResults<V2WikiHistorySnapshot> historyResults = new PaginatedResults<V2WikiHistorySnapshot>();
		when(
				mockSynapse
						.getV2WikiHistory(
								any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
								any(Long.class), any(Long.class))).thenReturn(
				historyResults);
		synapseClient.getV2WikiHistory(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"), new Long(10), new Long(0));
		verify(mockSynapse).getV2WikiHistory(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
				any(Long.class), any(Long.class));
	}

	@Test
	public void testGetV2WikiAttachmentHandles() throws Exception {
		FileHandleResults testResults = new FileHandleResults();
		Mockito.when(
				mockSynapse
						.getV2WikiAttachmentHandles(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class)))
				.thenReturn(testResults);
		synapseClient.getV2WikiAttachmentHandles(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"));
		verify(mockSynapse).getV2WikiAttachmentHandles(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));

		Mockito.when(
				mockSynapse
						.getVersionOfV2WikiAttachmentHandles(
								any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
								any(Long.class))).thenReturn(testResults);
		synapseClient.getVersionOfV2WikiAttachmentHandles(new WikiPageKey(
				"syn123", ObjectType.ENTITY.toString(), "20"), new Long(0));
		verify(mockSynapse).getVersionOfV2WikiAttachmentHandles(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
				any(Long.class));
	}

	@Test
	public void testZipAndUpload() throws IOException, RestServiceException,
			JSONObjectAdapterException, SynapseException {
		Mockito.when(
				mockSynapse
						.createFileHandle(any(File.class), any(String.class)))
				.thenReturn(handle);
		synapseClient.zipAndUploadFile("markdown", "fileName");
		verify(mockSynapse)
				.createFileHandle(any(File.class), any(String.class));
	}

	@Test
	public void testGetMarkdown() throws IOException, RestServiceException,
			SynapseException {
		String someMarkDown = "someMarkDown";
		Mockito.when(
				mockSynapse
						.downloadV2WikiMarkdown(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class)))
				.thenReturn(someMarkDown);
		synapseClient.getMarkdown(new WikiPageKey("syn123", ObjectType.ENTITY
				.toString(), "20"));
		verify(mockSynapse).downloadV2WikiMarkdown(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));

		Mockito.when(
				mockSynapse
						.downloadVersionOfV2WikiMarkdown(
								any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
								any(Long.class))).thenReturn(someMarkDown);
		synapseClient.getVersionOfMarkdown(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"), new Long(0));
		verify(mockSynapse).downloadVersionOfV2WikiMarkdown(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
				any(Long.class));
	}

	@Test
	public void testCreateV2WikiPageWithV1() throws Exception {
		Mockito.when(
				mockSynapse.createWikiPage(anyString(), any(ObjectType.class),
						any(WikiPage.class))).thenReturn(page);
		synapseClient.createV2WikiPageWithV1("testId",
				ObjectType.ENTITY.toString(), page);
		verify(mockSynapse).createWikiPage(anyString(), any(ObjectType.class),
				any(WikiPage.class));
	}

	@Test
	public void testUpdateV2WikiPageWithV1() throws Exception {
		Mockito.when(
				mockSynapse.updateWikiPage(anyString(), any(ObjectType.class),
						any(WikiPage.class))).thenReturn(page);
		synapseClient.updateV2WikiPageWithV1("testId",
				ObjectType.ENTITY.toString(), page);
		verify(mockSynapse).updateWikiPage(anyString(), any(ObjectType.class),
				any(WikiPage.class));
	}

	@Test
	public void getV2WikiPageAsV1() throws Exception {
		Mockito.when(
				mockSynapse
						.getWikiPage(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class)))
				.thenReturn(page);
		Mockito.when(
				mockSynapse
						.getV2WikiPage(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class)))
				.thenReturn(v2Page);
		synapseClient.getV2WikiPageAsV1(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"));
		verify(mockSynapse).getWikiPage(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));
		// asking for the same page twice should result in a cache hit, and it
		// should not ask for it from the synapse client
		synapseClient.getV2WikiPageAsV1(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"));
		verify(mockSynapse, Mockito.times(1)).getWikiPage(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));

		Mockito.when(
				mockSynapse
						.getWikiPageForVersion(
								any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
								any(Long.class))).thenReturn(page);
		Mockito.when(
				mockSynapse
						.getVersionOfV2WikiPage(
								any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
								anyLong())).thenReturn(v2Page);
		synapseClient.getVersionOfV2WikiPageAsV1(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"), new Long(0));
		verify(mockSynapse).getWikiPageForVersion(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
				any(Long.class));
		// asking for the same page twice should result in a cache hit, and it
		// should not ask for it from the synapse client
		synapseClient.getVersionOfV2WikiPageAsV1(new WikiPageKey("syn123",
				ObjectType.ENTITY.toString(), "20"), new Long(0));
		verify(mockSynapse, Mockito.times(1)).getWikiPageForVersion(
				any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
				any(Long.class));
	}

	private void resetUpdateExternalFileHandleMocks(String testId,
			FileEntity file, ExternalFileHandle handle)
			throws SynapseException, JSONObjectAdapterException {
		reset(mockSynapse);
		when(mockSynapse.getEntityById(testId)).thenReturn(file);
		when(
				mockSynapse
						.createExternalFileHandle(any(ExternalFileHandle.class)))
				.thenReturn(handle);
		when(mockSynapse.putEntity(any(FileEntity.class))).thenReturn(file);
	}

	@Test
	public void testUpdateExternalFileHandle() throws Exception {
		// verify call is directly calling the synapse client provider, and it
		// tries to rename the entity to the filename
		String myFileName = "testFileName.csv";
		String testUrl = "http://mytesturl/" + myFileName;
		String testId = "myTestId";
		String md5 = "e10e3f4491440ce7b48edc97f03307bb";
		Long fileSize=2048L;
		FileEntity file = new FileEntity();
		String originalFileEntityName = "syn1223";
		file.setName(originalFileEntityName);
		file.setId(testId);
		file.setDataFileHandleId("handle1");
		ExternalFileHandle handle = new ExternalFileHandle();
		handle.setExternalURL(testUrl);

		resetUpdateExternalFileHandleMocks(testId, file, handle);
		synapseClient.updateExternalFile(testId, testUrl, fileSize, md5, storageLocationId);

		verify(mockSynapse).getEntityById(testId);
		
		ArgumentCaptor<ExternalFileHandle> captor = ArgumentCaptor.forClass(ExternalFileHandle.class);
		verify(mockSynapse).createExternalFileHandle(captor.capture());
		ExternalFileHandle capturedValue = captor.getValue();
		assertEquals(testUrl, capturedValue.getExternalURL());
		assertEquals(md5, capturedValue.getContentMd5());
//		assertEquals(fileSize, capturedValue.getContentSize());
		
		verify(mockSynapse).putEntity(any(FileEntity.class));

		// and if rename fails, verify all is well (but the FileEntity name is
		// not updated)
		resetUpdateExternalFileHandleMocks(testId, file, handle);
		file.setName(originalFileEntityName);
		// first call should return file, second call to putEntity should throw
		// an exception
		when(mockSynapse.putEntity(any(FileEntity.class))).thenReturn(file)
				.thenThrow(
						new IllegalArgumentException(
								"invalid name for some reason"));
		synapseClient.updateExternalFile(testId, testUrl, fileSize, md5, storageLocationId);

		// called createExternalFileHandle
		verify(mockSynapse).createExternalFileHandle(
				any(ExternalFileHandle.class));
		// and it should have called putEntity again
		verify(mockSynapse).putEntity(any(FileEntity.class));
	}

	@Test
	public void testCreateExternalFile() throws Exception {
		// test setting file handle name
		String parentEntityId = "syn123333";
		String externalUrl = "sftp://foobar.edu/b/test.txt";
		String fileName = "testing.txt";
		String md5 = "e10e3f4491440ce7b48edc97f03307bb";
		Long fileSize = 1024L;
		when(
				mockSynapse
						.createExternalFileHandle(any(ExternalFileHandle.class)))
				.thenReturn(new ExternalFileHandle());
		when(mockSynapse.createEntity(any(FileEntity.class))).thenReturn(
				new FileEntity());
		synapseClient.createExternalFile(parentEntityId, externalUrl, fileName, fileSize, md5, storageLocationId);
		ArgumentCaptor<ExternalFileHandle> captor = ArgumentCaptor
				.forClass(ExternalFileHandle.class);
		verify(mockSynapse).createExternalFileHandle(captor.capture());
		ExternalFileHandle handle = captor.getValue();
		// verify name is set
		assertEquals(fileName, handle.getFileName());
		assertEquals(externalUrl, handle.getExternalURL());
		assertEquals(storageLocationId, handle.getStorageLocationId());
		assertEquals(md5, handle.getContentMd5());
//		assertEquals(fileSize, handle.getContentSize());
	}

	@Test
	public void testGetEntityDoi() throws Exception {
		// wiring test
		Doi testDoi = new Doi();
		testDoi.setDoiStatus(DoiStatus.CREATED);
		testDoi.setId("test doi id");
		testDoi.setCreatedBy("Test User");
		testDoi.setCreatedOn(new Date());
		testDoi.setObjectId("syn1234");
		Mockito.when(mockSynapse.getEntityDoi(anyString(), anyLong()))
				.thenReturn(testDoi);
		synapseClient.getEntityDoi("test entity id", null);
		verify(mockSynapse).getEntityDoi(anyString(), anyLong());
	}

	private FileEntity getTestFileEntity() {
		FileEntity testFileEntity = new FileEntity();
		testFileEntity.setId("5544");
		testFileEntity.setName(testFileName);
		return testFileEntity;
	}

	@Test(expected = NotFoundException.class)
	public void testGetEntityDoiNotFound() throws Exception {
		// wiring test
		Mockito.when(mockSynapse.getEntityDoi(anyString(), anyLong()))
				.thenThrow(new SynapseNotFoundException());
		synapseClient.getEntityDoi("test entity id", null);
	}

	@Test
	public void testCreateDoi() throws Exception {
		// wiring test
		synapseClient.createDoi("test entity id", null);
		verify(mockSynapse).createEntityDoi(anyString(), anyLong());
	}

	private List<ChunkRequest> getTestChunkRequestJson()
			throws JSONObjectAdapterException {
		ChunkRequest chunkRequest = new ChunkRequest();
		ChunkedFileToken token = new ChunkedFileToken();
		token.setKey("test key");
		chunkRequest.setChunkedFileToken(token);
		chunkRequest.setChunkNumber(1l);
		List<ChunkRequest> chunkRequests = new ArrayList<ChunkRequest>();
		chunkRequests.add(chunkRequest);
		return chunkRequests;
	}

	@Test
	public void testCombineChunkedFileUpload()
			throws JSONObjectAdapterException, SynapseException,
			RestServiceException {
		List<ChunkRequest> chunkRequests = getTestChunkRequestJson();
		synapseClient.combineChunkedFileUpload(chunkRequests);
		verify(mockSynapse).startUploadDeamon(
				any(CompleteAllChunksRequest.class));
	}

	@Test
	public void testGetUploadDaemonStatus() throws JSONObjectAdapterException,
			SynapseException, RestServiceException {
		synapseClient.getUploadDaemonStatus("daemonId");
		verify(mockSynapse).getCompleteUploadDaemonStatus(anyString());
	}

	/**
	 * Direct upload tests. Most of the methods are simple pass-throughs to the
	 * Java Synapse client, but completeUpload has additional logic
	 * 
	 * @throws JSONObjectAdapterException
	 * @throws SynapseException
	 * @throws RestServiceException
	 */
	@Test
	public void testCompleteUpload() throws JSONObjectAdapterException,
			SynapseException, RestServiceException {
		FileEntity testFileEntity = getTestFileEntity();
		when(mockSynapse.createEntity(any(FileEntity.class))).thenReturn(
				testFileEntity);
		when(mockSynapse.putEntity(any(FileEntity.class))).thenReturn(
				testFileEntity);

		// parent entity has no immediate children
		EntityIdList childEntities = new EntityIdList();
		childEntities.setIdList(new ArrayList());

		synapseClient.setFileEntityFileHandle(null, null, "parentEntityId");

		// it should have tried to create a new entity (since entity id was
		// null)
		verify(mockSynapse).createEntity(any(FileEntity.class));
	}

	@Test(expected = NotFoundException.class)
	public void testGetFileEntityIdWithSameNameNotFound()
			throws JSONObjectAdapterException, SynapseException,
			RestServiceException, JSONException {
		JSONObject queryResult = new JSONObject();
		queryResult.put("totalNumberOfResults", (long) 0);
		when(mockSynapse.query(anyString())).thenReturn(queryResult); // TODO

		String fileEntityId = synapseClient.getFileEntityIdWithSameName(
				testFileName, "parentEntityId");
	}

	@Test(expected = ConflictException.class)
	public void testGetFileEntityIdWithSameNameConflict()
			throws JSONObjectAdapterException, SynapseException,
			RestServiceException, JSONException {
		Folder folder = new Folder();
		folder.setName(testFileName);
		JSONObject queryResult = new JSONObject();
		JSONArray results = new JSONArray();

		// Set up results.
		JSONObject objectResult = EntityFactory
				.createJSONObjectForEntity(folder);
		JSONArray typeArray = new JSONArray();
		typeArray.put("Folder");
		objectResult.put("entity.concreteType", typeArray);
		results.put(objectResult);

		// Set up query result.
		queryResult.put("totalNumberOfResults", (long) 1);
		queryResult.put("results", results);

		// Have results returned in query.
		when(mockSynapse.query(anyString())).thenReturn(queryResult);

		String fileEntityId = synapseClient.getFileEntityIdWithSameName(
				testFileName, "parentEntityId");
	}

	@Test
	public void testGetFileEntityIdWithSameNameFound() throws JSONException,
			JSONObjectAdapterException, SynapseException, RestServiceException {
		FileEntity file = getTestFileEntity();
		JSONObject queryResult = new JSONObject();
		JSONArray results = new JSONArray();

		// Set up results.
		JSONObject objectResult = EntityFactory.createJSONObjectForEntity(file);
		JSONArray typeArray = new JSONArray();
		typeArray.put(FileEntity.class.getName());
		objectResult.put("entity.concreteType", typeArray);
		objectResult.put("entity.id", file.getId());
		results.put(objectResult);
		queryResult.put("totalNumberOfResults", (long) 1);
		queryResult.put("results", results);

		// Have results returned in query.
		when(mockSynapse.query(anyString())).thenReturn(queryResult);

		String fileEntityId = synapseClient.getFileEntityIdWithSameName(
				testFileName, "parentEntityId");
		assertEquals(fileEntityId, file.getId());
	}

	@Test
	public void testCompleteChunkedFileUploadExistingEntity()
			throws JSONObjectAdapterException, SynapseException,
			RestServiceException {
		List<ChunkRequest> chunkRequests = getTestChunkRequestJson();
		FileEntity testFileEntity = getTestFileEntity();
		when(mockSynapse.getEntityById(anyString())).thenReturn(testFileEntity);
		when(mockSynapse.createEntity(any(FileEntity.class))).thenThrow(
				new AssertionError("No need to create a new entity!"));
		when(mockSynapse.putEntity(any(FileEntity.class))).thenReturn(
				testFileEntity);
		synapseClient.setFileEntityFileHandle(null, entityId, "parentEntityId");

		// it should have tried to find the entity
		verify(mockSynapse).getEntityById(anyString());
		// update the data file handle id
		verify(mockSynapse, Mockito.times(1)).putEntity(any(FileEntity.class));
	}

	@Test
	public void testGetChunkedFileToken() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		String fileName = "test file.zip";
		String contentType = "application/test";
		String md5 = "0123456789abcdef";
		ChunkedFileToken testToken = new ChunkedFileToken();
		testToken.setFileName(fileName);
		testToken.setKey("a key 42");
		testToken.setUploadId("upload ID 123");
		testToken.setContentMD5(md5);
		testToken.setStorageLocationId(storageLocationId);
		when(
				mockSynapse
						.createChunkedFileUploadToken(any(CreateChunkedFileTokenRequest.class)))
				.thenReturn(testToken);

		ChunkedFileToken token = synapseClient.getChunkedFileToken(fileName,
				contentType, md5, storageLocationId);
		verify(mockSynapse).createChunkedFileUploadToken(
				any(CreateChunkedFileTokenRequest.class));
		assertEquals(testToken, token);
	}

	@Test
	public void testGetChunkedPresignedUrl() throws SynapseException,
			RestServiceException, MalformedURLException,
			JSONObjectAdapterException {
		URL testUrl = new URL("http://test.presignedurl.com/foo");
		when(mockSynapse.createChunkedPresignedUrl(any(ChunkRequest.class)))
				.thenReturn(testUrl);
		String presignedUrl = synapseClient
				.getChunkedPresignedUrl(getTestChunkRequestJson().get(0));
		verify(mockSynapse).createChunkedPresignedUrl(any(ChunkRequest.class));
		assertEquals(testUrl.toString(), presignedUrl);
	}

	@Test
	public void testInviteMemberOpenInvitations() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		membershipStatus.setHasOpenInvitation(true);
		// verify it does not create a new invitation since one is already open
		synapseClient.inviteMember("123", "a team", "", "");
		verify(mockSynapse, Mockito.times(0)).addTeamMember(anyString(),
				anyString(), anyString(), anyString());
		verify(mockSynapse, Mockito.times(0)).createMembershipInvitation(
				any(MembershipInvtnSubmission.class), anyString(), anyString());

	}

	@Test
	public void testRequestMemberOpenRequests() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		membershipStatus.setHasOpenRequest(true);
		// verify it does not create a new request since one is already open
		synapseClient.requestMembership("123", "a team", "", TEST_HOME_PAGE_BASE);
		verify(mockSynapse, Mockito.times(0)).addTeamMember(anyString(),
				anyString(), eq(TEST_HOME_PAGE_BASE+"#!Team:"), eq(TEST_HOME_PAGE_BASE+"#!SignedToken:Settings/"));
		verify(mockSynapse, Mockito.times(0)).createMembershipRequest(
				any(MembershipRqstSubmission.class), anyString(), anyString());
	}

	@Test
	public void testInviteMemberCanJoin() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		membershipStatus.setCanJoin(true);
		synapseClient.inviteMember("123", "a team", "", TEST_HOME_PAGE_BASE);
		verify(mockSynapse).addTeamMember(anyString(), anyString(), eq(TEST_HOME_PAGE_BASE+"#!Team:"), eq(TEST_HOME_PAGE_BASE+"#!SignedToken:Settings/"));
	}

	@Test
	public void testRequestMembershipCanJoin() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		membershipStatus.setCanJoin(true);
		synapseClient.requestMembership("123", "a team", "", TEST_HOME_PAGE_BASE);
		verify(mockSynapse).addTeamMember(anyString(), anyString(), eq(TEST_HOME_PAGE_BASE+"#!Team:"), eq(TEST_HOME_PAGE_BASE+"#!SignedToken:Settings/"));
	}

	@Test
	public void testInviteMember() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		synapseClient.inviteMember("123", "a team", "", TEST_HOME_PAGE_BASE);
		verify(mockSynapse).createMembershipInvitation(
				any(MembershipInvtnSubmission.class), eq(TEST_HOME_PAGE_BASE+"#!SignedToken:JoinTeam/"), eq(TEST_HOME_PAGE_BASE+"#!SignedToken:Settings/"));
	}

	@Test
	public void testRequestMembership() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		synapseClient.requestMembership("123", "a team", "", TEST_HOME_PAGE_BASE);
		verify(mockSynapse).createMembershipRequest(
				any(MembershipRqstSubmission.class), eq(TEST_HOME_PAGE_BASE+"#!SignedToken:JoinTeam/"), eq(TEST_HOME_PAGE_BASE+"#!SignedToken:Settings/"));
	}

	@Test
	public void testGetOpenRequestCountUnauthorized() throws SynapseException,
			RestServiceException {
		// is not an admin
		TeamMember testTeamMember = new TeamMember();
		testTeamMember.setIsAdmin(false);
		when(mockSynapse.getTeamMember(anyString(), anyString())).thenReturn(
				testTeamMember);

		Long count = synapseClient.getOpenRequestCount("myUserId", "myTeamId");
		// should never ask for open request count
		verify(mockSynapse, Mockito.never()).getOpenMembershipRequests(
				anyString(), anyString(), anyLong(), anyLong());
		assertNull(count);
	}

	@Test
	public void testGetOpenRequestCount() throws SynapseException,
			RestServiceException, MalformedURLException,
			JSONObjectAdapterException {
		// is admin
		TeamMember testTeamMember = new TeamMember();
		testTeamMember.setIsAdmin(true);
		when(mockSynapse.getTeamMember(anyString(), anyString())).thenReturn(
				testTeamMember);

		Long testCount = 42L;
		PaginatedResults<MembershipRequest> testOpenRequests = new PaginatedResults<MembershipRequest>();
		testOpenRequests.setTotalNumberOfResults(testCount);
		when(
				mockSynapse.getOpenMembershipRequests(anyString(), anyString(),
						anyLong(), anyLong())).thenReturn(testOpenRequests);

		Long count = synapseClient.getOpenRequestCount("myUserId", "myTeamId");

		verify(mockSynapse, Mockito.times(1)).getOpenMembershipRequests(
				anyString(), anyString(), anyLong(), anyLong());
		assertEquals(testCount, count);
	}

	@Test
	public void testGetOpenTeamInvitations() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		setupTeamInvitations();
		int limit = 55;
		int offset = 2;
		String teamId = "132";
		List<OpenTeamInvitationBundle> invitationBundles = synapseClient
				.getOpenTeamInvitations(teamId, limit, offset);
		verify(mockSynapse).getOpenMembershipInvitationSubmissions(eq(teamId),
				anyString(), eq((long) limit), eq((long) offset));
		// we set this up so that a single invite would be returned. Verify that
		// it is the one we're looking for
		assertEquals(1, invitationBundles.size());
		OpenTeamInvitationBundle invitationBundle = invitationBundles.get(0);
		assertEquals(inviteeUserProfile, invitationBundle.getUserProfile());
		assertEquals(testInvitation, invitationBundle.getMembershipInvtnSubmission());
	}

	@Test
	public void testGetTeamBundle() throws SynapseException,
			RestServiceException, MalformedURLException,
			JSONObjectAdapterException {
		// set team member count
		Long testMemberCount = 111L;
		PaginatedResults<TeamMember> allMembers = new PaginatedResults<TeamMember>();
		allMembers.setTotalNumberOfResults(testMemberCount);
		when(
				mockSynapse.getTeamMembers(anyString(), anyString(), anyLong(),
						anyLong())).thenReturn(allMembers);

		// set team
		Team team = new Team();
		team.setId("test team id");
		when(mockSynapse.getTeam(anyString())).thenReturn(team);
		
		// is member
		TeamMembershipStatus membershipStatus = new TeamMembershipStatus();
		membershipStatus.setIsMember(true);
		when(mockSynapse.getTeamMembershipStatus(anyString(), anyString()))
				.thenReturn(membershipStatus);
		// is admin
		TeamMember testTeamMember = new TeamMember();
		boolean isAdmin = true;
		testTeamMember.setIsAdmin(isAdmin);
		when(mockSynapse.getTeamMember(anyString(), anyString())).thenReturn(
				testTeamMember);

		// make the call
		TeamBundle bundle = synapseClient.getTeamBundle("myUserId", "myTeamId",
				true);

		// now verify round all values were returned in the bundle (based on the
		// mocked service calls)
		assertEquals(team, bundle.getTeam());
		assertEquals(membershipStatus, bundle.getTeamMembershipStatus());
		assertEquals(isAdmin, bundle.isUserAdmin());
		assertEquals(testMemberCount, bundle.getTotalMemberCount());
	}

	@Test
	public void testGetTeamMembers() throws SynapseException,
			RestServiceException, MalformedURLException,
			JSONObjectAdapterException {
		// set team member count
		Long testMemberCount = 111L;
		PaginatedResults<TeamMember> allMembers = new PaginatedResults<TeamMember>();
		allMembers.setTotalNumberOfResults(testMemberCount);
		List<TeamMember> members = new ArrayList<TeamMember>();

		TeamMember member1 = new TeamMember();
		member1.setIsAdmin(true);
		UserGroupHeader header1 = new UserGroupHeader();
		Long member1Id = 123L;
		header1.setOwnerId(member1Id + "");
		member1.setMember(header1);
		members.add(member1);

		TeamMember member2 = new TeamMember();
		member2.setIsAdmin(false);
		UserGroupHeader header2 = new UserGroupHeader();
		Long member2Id = 456L;
		header2.setOwnerId(member2Id + "");
		member2.setMember(header2);
		members.add(member2);

		allMembers.setResults(members);
		when(
				mockSynapse.getTeamMembers(anyString(), anyString(), anyLong(),
						anyLong())).thenReturn(allMembers);

		List<UserProfile> profiles = new ArrayList<UserProfile>();
		UserProfile profile1 = new UserProfile();
		profile1.setOwnerId(member1Id + "");
		UserProfile profile2 = new UserProfile();
		profile2.setOwnerId(member2Id + "");
		profiles.add(profile1);
		profiles.add(profile2);
		when(mockSynapse.listUserProfiles(anyList())).thenReturn(profiles);

		// make the call
		TeamMemberPagedResults results = synapseClient.getTeamMembers(
				"myTeamId", "search term", 100, 0);

		// verify it results in the two team member bundles that we expect
		List<TeamMemberBundle> memberBundles = results.getResults();
		assertEquals(2, memberBundles.size());
		TeamMemberBundle bundle1 = memberBundles.get(0);
		assertTrue(bundle1.getIsTeamAdmin());
		assertEquals(profile1, bundle1.getUserProfile());
		TeamMemberBundle bundle2 = memberBundles.get(1);
		assertFalse(bundle2.getIsTeamAdmin());
		assertEquals(profile2, bundle2.getUserProfile());
	}

	@Test
	public void testGetEntityHeaderBatch() throws SynapseException,
			RestServiceException, MalformedURLException,
			JSONObjectAdapterException {
		List<EntityHeader> headers = synapseClient
				.getEntityHeaderBatch(new ArrayList());
		// in the setup, we told the mockSynapse.getEntityHeaderBatch to return
		// batchHeaderResults
		for (int i = 0; i < batchHeaderResults.size(); i++) {
			assertEquals(batchHeaderResults.get(i), headers.get(i));
		}
	}

	@Test
	public void testSendMessage() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		ArgumentCaptor<MessageToUser> arg = ArgumentCaptor
				.forClass(MessageToUser.class);
		Set<String> recipients = new HashSet<String>();
		recipients.add("333");
		String subject = "The Mathematics of Quantum Neutrino Fields";
		String messageBody = "Atoms are not to be trusted, they make up everything";
		String hostPageBaseURL = "http://localhost/Portal.html";
		synapseClient.sendMessage(recipients, subject, messageBody, hostPageBaseURL);
		verify(mockSynapse).uploadToFileHandle(any(byte[].class), eq(SynapseClientImpl.HTML_MESSAGE_CONTENT_TYPE));
		verify(mockSynapse).sendMessage(arg.capture());
		MessageToUser toSendMessage = arg.getValue();
		assertEquals(subject, toSendMessage.getSubject());
		assertEquals(recipients, toSendMessage.getRecipients());
		assertTrue(toSendMessage.getNotificationUnsubscribeEndpoint().startsWith(hostPageBaseURL));
	}

	@Test
	public void testGetCertifiedUserPassingRecord()
			throws RestServiceException, SynapseException,
			JSONObjectAdapterException {
		PassingRecord passingRecord = new PassingRecord();
		passingRecord.setPassed(true);
		passingRecord.setQuizId(1238L);
		String passingRecordJson = passingRecord.writeToJSONObject(
				adapterFactory.createNew()).toJSONString();
		when(mockSynapse.getCertifiedUserPassingRecord(anyString()))
				.thenReturn(passingRecord);
		String returnedPassingRecordJson = synapseClient
				.getCertifiedUserPassingRecord("123");
		verify(mockSynapse).getCertifiedUserPassingRecord(anyString());
		assertEquals(passingRecordJson, returnedPassingRecordJson);
	}

	@Test(expected = NotFoundException.class)
	public void testUserNeverAttemptedCertification()
			throws RestServiceException, SynapseException {
		when(mockSynapse.getCertifiedUserPassingRecord(anyString())).thenThrow(
				new SynapseNotFoundException("PassingRecord not found"));
		synapseClient.getCertifiedUserPassingRecord("123");
	}

	@Test(expected = NotFoundException.class)
	public void testUserFailedCertification() throws RestServiceException,
			SynapseException {
		PassingRecord passingRecord = new PassingRecord();
		passingRecord.setPassed(false);
		passingRecord.setQuizId(1238L);
		when(mockSynapse.getCertifiedUserPassingRecord(anyString()))
				.thenReturn(passingRecord);
		synapseClient.getCertifiedUserPassingRecord("123");
	}

	@Test
	public void testGetCertificationQuiz() throws RestServiceException,
			SynapseException {
		when(mockSynapse.getCertifiedUserTest()).thenReturn(new Quiz());
		synapseClient.getCertificationQuiz();
		verify(mockSynapse).getCertifiedUserTest();
	}

	@Test
	public void testSubmitCertificationQuizResponse()
			throws RestServiceException, SynapseException,
			JSONObjectAdapterException {
		PassingRecord mockPassingRecord = new PassingRecord();
		when(
				mockSynapse
						.submitCertifiedUserTestResponse(any(QuizResponse.class)))
				.thenReturn(mockPassingRecord);
		QuizResponse myResponse = new QuizResponse();
		myResponse.setId(837L);
		synapseClient.submitCertificationQuizResponse(myResponse);
		verify(mockSynapse).submitCertifiedUserTestResponse(eq(myResponse));
	}

	@Test
	public void testMarkdownCache() throws Exception {
		Cache<MarkdownCacheRequest, WikiPage> mockCache = Mockito
				.mock(Cache.class);
		synapseClient.setMarkdownCache(mockCache);
		WikiPage page = new WikiPage();
		when(mockCache.get(any(MarkdownCacheRequest.class))).thenReturn(page);
		Mockito.when(
				mockSynapse
						.getV2WikiPage(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class)))
				.thenReturn(v2Page);
		WikiPage actualResult = synapseClient
				.getV2WikiPageAsV1(new WikiPageKey(entity.getId(),
						ObjectType.ENTITY.toString(), "12"));
		assertEquals(page, actualResult);
		verify(mockCache).get(any(MarkdownCacheRequest.class));
	}

	@Test
	public void testMarkdownCacheWithVersion() throws Exception {
		Cache<MarkdownCacheRequest, WikiPage> mockCache = Mockito
				.mock(Cache.class);
		synapseClient.setMarkdownCache(mockCache);
		WikiPage page = new WikiPage();
		when(mockCache.get(any(MarkdownCacheRequest.class))).thenReturn(page);
		Mockito.when(
				mockSynapse
						.getVersionOfV2WikiPage(
								any(org.sagebionetworks.repo.model.dao.WikiPageKey.class),
								anyLong())).thenReturn(v2Page);
		WikiPage actualResult = synapseClient.getVersionOfV2WikiPageAsV1(
				new WikiPageKey(entity.getId(), ObjectType.ENTITY.toString(),
						"12"), 5L);
		assertEquals(page, actualResult);
		verify(mockCache).get(any(MarkdownCacheRequest.class));
	}

	@Test
	public void testFilterAccessRequirements() throws Exception {
		List<AccessRequirement> unfilteredAccessRequirements = new ArrayList<AccessRequirement>();
		List<AccessRequirement> filteredAccessRequirements;
		// filter empty list should not result in failure
		filteredAccessRequirements = AccessRequirementUtils
				.filterAccessRequirements(unfilteredAccessRequirements,
						ACCESS_TYPE.UPDATE);
		assertTrue(filteredAccessRequirements.isEmpty());

		unfilteredAccessRequirements
				.add(createAccessRequirement(ACCESS_TYPE.DOWNLOAD));
		unfilteredAccessRequirements
				.add(createAccessRequirement(ACCESS_TYPE.SUBMIT));
		unfilteredAccessRequirements
				.add(createAccessRequirement(ACCESS_TYPE.SUBMIT));
		// no requirements of type UPDATE
		filteredAccessRequirements = AccessRequirementUtils
				.filterAccessRequirements(unfilteredAccessRequirements,
						ACCESS_TYPE.UPDATE);
		assertTrue(filteredAccessRequirements.isEmpty());
		// 1 download
		filteredAccessRequirements = AccessRequirementUtils
				.filterAccessRequirements(unfilteredAccessRequirements,
						ACCESS_TYPE.DOWNLOAD);
		assertEquals(1, filteredAccessRequirements.size());
		// 2 submit
		filteredAccessRequirements = AccessRequirementUtils
				.filterAccessRequirements(unfilteredAccessRequirements,
						ACCESS_TYPE.SUBMIT);
		assertEquals(2, filteredAccessRequirements.size());

		// finally, filter null list - result will be an empty list
		filteredAccessRequirements = AccessRequirementUtils
				.filterAccessRequirements(null, ACCESS_TYPE.SUBMIT);
		assertNotNull(filteredAccessRequirements);
		assertTrue(filteredAccessRequirements.isEmpty());
	}

	@Test
	public void testGetEntityUnmetAccessRequirements() throws Exception {
		// verify it calls getUnmetAccessRequirements when unmet is true
		synapseClient.getEntityAccessRequirements(entityId, true, null);
		verify(mockSynapse)
				.getUnmetAccessRequirements(
						any(RestrictableObjectDescriptor.class),
						any(ACCESS_TYPE.class));
	}

	@Test
	public void testGetAllEntityAccessRequirements() throws Exception {
		// verify it calls getAccessRequirements when unmet is false
		synapseClient.getEntityAccessRequirements(entityId, false, null);
		verify(mockSynapse).getAccessRequirements(
				any(RestrictableObjectDescriptor.class));
	}

	// pass through tests for email validation

	@Test
	public void testAdditionalEmailValidation() throws Exception {
		Long userId = 992843l;
		String emailAddress = "test@test.com";
		String callbackUrl = "http://www.synapse.org/#!Account:";
		synapseClient.additionalEmailValidation(userId.toString(),
				emailAddress, callbackUrl);
		verify(mockSynapse).additionalEmailValidation(eq(userId),
				eq(emailAddress), eq(callbackUrl));
	}

	@Test
	public void testAddEmail() throws Exception {
		String emailAddressToken = "long synapse email token";
		synapseClient.addEmail(emailAddressToken);
		verify(mockSynapse).addEmail(any(AddEmailInfo.class), anyBoolean());
	}

	@Test
	public void testGetNotificationEmail() throws Exception {
		synapseClient.getNotificationEmail();
		verify(mockSynapse).getNotificationEmail();
	}

	@Test
	public void testSetNotificationEmail() throws Exception {
		String emailAddress = "test@test.com";
		synapseClient.setNotificationEmail(emailAddress);
		verify(mockSynapse).setNotificationEmail(eq(emailAddress));
	}

	@Test
	public void testLogErrorToRepositoryServices() throws SynapseException,
			RestServiceException, JSONObjectAdapterException {
		String errorMessage = "error has occurred";
		String permutationStrongName="Chrome";
		synapseClient.logErrorToRepositoryServices(errorMessage, null, null, null, permutationStrongName);
		verify(mockSynapse).getMyProfile();
		verify(mockSynapse).logError(any(LogEntry.class));
	}

	@Test
	public void testLogErrorToRepositoryServicesTruncation()
			throws SynapseException, RestServiceException,
			JSONObjectAdapterException, ServletException {
		String exceptionMessage = "This exception brought to you by Sage Bionetworks";
		Exception e = new Exception(exceptionMessage, new IllegalArgumentException(new NullPointerException()));
		ServletContext mockServletContext = Mockito.mock(ServletContext.class);
		ServletConfig mockServletConfig = Mockito.mock(ServletConfig.class);
		when(mockServletConfig.getServletContext()).thenReturn(mockServletContext);
		synapseClient.init(mockServletConfig);
		String errorMessage = "error has occurred";
		String permutationStrongName="FF";
		synapseClient.logErrorToRepositoryServices(errorMessage, e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace(), permutationStrongName);
		ArgumentCaptor<LogEntry> captor = ArgumentCaptor
				.forClass(LogEntry.class);
		verify(mockSynapse).logError(captor.capture());
		LogEntry logEntry = captor.getValue();
		assertTrue(logEntry.getLabel().length() < SynapseClientImpl.MAX_LOG_ENTRY_LABEL_SIZE + 100);
		assertTrue(logEntry.getMessage().contains(errorMessage));
		assertTrue(logEntry.getMessage().contains(MY_USER_PROFILE_OWNER_ID));
		assertTrue(logEntry.getMessage().contains(e.getClass().getSimpleName()));
		assertTrue(logEntry.getMessage().contains(exceptionMessage));
	}

	@Test
	public void testGetMyProjects() throws Exception {
		int limit = 11;
		int offset = 20;
		ProjectPagedResults results = synapseClient.getMyProjects(ProjectListType.MY_PROJECTS, limit, offset,
				ProjectListSortColumn.LAST_ACTIVITY, SortDirection.DESC);
		verify(mockSynapse).getMyProjects(eq(ProjectListType.MY_PROJECTS),
				eq(ProjectListSortColumn.LAST_ACTIVITY),
				eq(SortDirection.DESC), eq(limit), eq(offset));
		verify(mockSynapse).listUserProfiles(anyList());
	}

	@Test
	public void testGetUserProjects() throws Exception {
		int limit = 11;
		int offset = 20;
		Long userId = 133l;
		String userIdString = userId.toString();
		synapseClient.getUserProjects(userIdString, limit, offset,
				ProjectListSortColumn.LAST_ACTIVITY, SortDirection.DESC);
		verify(mockSynapse).getProjectsFromUser(eq(userId),
				eq(ProjectListSortColumn.LAST_ACTIVITY),
				eq(SortDirection.DESC), eq(limit), eq(offset));
		verify(mockSynapse).listUserProfiles(anyList());
	}

	@Test
	public void testGetProjectsForTeam() throws Exception {
		int limit = 13;
		int offset = 40;
		Long teamId = 144l;
		String teamIdString = teamId.toString();
		synapseClient.getProjectsForTeam(teamIdString, limit, offset,
				ProjectListSortColumn.LAST_ACTIVITY, SortDirection.DESC);
		verify(mockSynapse).getProjectsForTeam(eq(teamId),
				eq(ProjectListSortColumn.LAST_ACTIVITY),
				eq(SortDirection.DESC), eq(limit), eq(offset));
		verify(mockSynapse).listUserProfiles(anyList());
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

	@Test
	public void testGetHost() throws RestServiceException {
		assertEquals("mydomain.com",
				synapseClient.getHost("sfTp://mydomain.com/foo/bar"));
		assertEquals("mydomain.com",
				synapseClient.getHost("http://mydomain.com/foo/bar"));
		assertEquals("mydomain.com",
				synapseClient.getHost("http://mydomain.com"));
		assertEquals("mydomain.com",
				synapseClient.getHost("sftp://mydomain.com:22/foo/bar"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetHostNull() throws RestServiceException {
		synapseClient.getHost(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetHostEmpty() throws RestServiceException {
		synapseClient.getHost("");
	}

	@Test(expected = BadRequestException.class)
	public void testGetHostBadUrl() throws RestServiceException {
		synapseClient.getHost("foobar");
	}

	@Test
	public void testGetRootWikiId() throws JSONObjectAdapterException,
			SynapseException, RestServiceException {
		org.sagebionetworks.repo.model.dao.WikiPageKey key = new org.sagebionetworks.repo.model.dao.WikiPageKey();
		key.setOwnerObjectId("1");
		key.setOwnerObjectType(ObjectType.ENTITY);
		String expectedId = "123";
		key.setWikiPageId(expectedId);
		when(mockSynapse.getRootWikiPageKey(anyString(), any(ObjectType.class)))
				.thenReturn(key);

		String actualId = synapseClient.getRootWikiId("1",
				ObjectType.ENTITY.toString());
		assertEquals(expectedId, actualId);
	}

	@Test
	public void testGetFavorites() throws JSONObjectAdapterException,
			SynapseException, RestServiceException {
		PaginatedResults<EntityHeader> pagedResults = new PaginatedResults<EntityHeader>();
		List<EntityHeader> unsortedResults = new ArrayList<EntityHeader>();
		pagedResults.setResults(unsortedResults);
		when(mockSynapse.getFavorites(anyInt(), anyInt())).thenReturn(
				pagedResults);

		// test empty favorites
		List<EntityHeader> actualList = synapseClient.getFavorites();
		assertTrue(actualList.isEmpty());

		// test a few unsorted favorites
		EntityHeader favZ = new EntityHeader();
		favZ.setName("Z");
		unsortedResults.add(favZ);
		EntityHeader favA = new EntityHeader();
		favA.setName("A");
		unsortedResults.add(favA);
		EntityHeader favQ = new EntityHeader();
		favQ.setName("q");
		unsortedResults.add(favQ);

		actualList = synapseClient.getFavorites();
		assertEquals(3, actualList.size());
		assertEquals(favA, actualList.get(0));
		assertEquals(favQ, actualList.get(1));
		assertEquals(favZ, actualList.get(2));
	}

	@Test
	public void testGetTeamBundlesNotOwner() throws RestServiceException, SynapseException {
		// the paginated results were set up to return {teamZ, teamA}, but
		// servlet side we sort by name.
		List<TeamRequestBundle> results = synapseClient.getTeamsForUser("abba", false);
		verify(mockSynapse).getTeamsForUser(eq("abba"), anyInt(), anyInt());
		assertEquals(2, results.size());
		assertEquals(teamA, results.get(0).getTeam());
		assertEquals(teamZ, results.get(1).getTeam());
		verify(mockSynapse, Mockito.never()).getOpenMembershipRequests(anyString(), anyString(),
				anyLong(), anyLong());
	}
	
	@Test
	public void testGetTeamBundlesOwner() throws RestServiceException, SynapseException {
		TeamMember testTeamMember = new TeamMember();
		testTeamMember.setIsAdmin(true);
		when(mockSynapse.getTeamMember(anyString(), anyString())).thenReturn(
				testTeamMember);
		when(mockSynapse.getOpenMembershipRequests(anyString(), anyString(), anyLong(), anyLong())).thenReturn(mockPaginatedMembershipRequest);
		
		List<TeamRequestBundle> results = synapseClient.getTeamsForUser("abba", true);
		verify(mockSynapse).getTeamsForUser(eq("abba"), anyInt(), anyInt());
		assertEquals(2, results.size());
		assertEquals(teamA, results.get(0).getTeam());
		assertEquals(teamZ, results.get(1).getTeam());
		Long reqCount1 = results.get(0).getRequestCount();
		Long reqCount2 = results.get(1).getRequestCount();
		assertEquals(new Long(3L), results.get(0).getRequestCount());
		assertEquals(new Long(3L), results.get(1).getRequestCount());

	}
	
	@Test
	public void testGetEntityInfo() throws RestServiceException,
	JSONObjectAdapterException, SynapseException{
		EntityBundlePlus entityBundlePlus = synapseClient.getEntityInfo(entityId);
		assertEquals(entity, entityBundlePlus.getEntityBundle().getEntity());
		assertEquals(annos, entityBundlePlus.getEntityBundle().getAnnotations());
		assertEquals(testUserProfile, entityBundlePlus.getProfile());
	}
	
	@Test(expected = BadRequestException.class)
	public void testHandleSignedTokenNull() throws RestServiceException, SynapseException{
		String tokenTypeName = null;
		synapseClient.hexDecodeAndSerialize(tokenTypeName, encodedJoinTeamToken);
	}
	
	@Test(expected = BadRequestException.class)
	public void testHandleSignedTokenEmpty() throws RestServiceException, SynapseException{
		String tokenTypeName = "";
		synapseClient.hexDecodeAndSerialize(tokenTypeName, encodedJoinTeamToken);
	}
	
	@Test(expected = BadRequestException.class)
	public void testHandleSignedTokenUnrecognized() throws RestServiceException, SynapseException{
		String tokenTypeName = "InvalidTokenType";
		synapseClient.hexDecodeAndSerialize(tokenTypeName, encodedJoinTeamToken);
	}
	
	@Test
	public void testHandleSignedTokenJoinTeam() throws RestServiceException, SynapseException{
		String tokenTypeName = NotificationTokenType.JoinTeam.name();
		SignedTokenInterface token = synapseClient.hexDecodeAndSerialize(tokenTypeName, encodedJoinTeamToken);
		synapseClient.handleSignedToken(token,TEST_HOME_PAGE_BASE);
		verify(mockSynapse).addTeamMember(joinTeamToken, TEST_HOME_PAGE_BASE+"#!Team:", TEST_HOME_PAGE_BASE+"#!SignedToken:Settings/");
	}
	
	@Test(expected = BadRequestException.class)
	public void testHandleSignedTokenInvalidJoinTeam() throws RestServiceException, SynapseException{
		String tokenTypeName = NotificationTokenType.JoinTeam.name();
		SignedTokenInterface token = synapseClient.hexDecodeAndSerialize(tokenTypeName, "invalid token");
	}
	@Test(expected = BadRequestException.class)
	public void testHandleSignedTokenJoinTeamWrongToken() throws RestServiceException, SynapseException{
		String tokenTypeName = NotificationTokenType.JoinTeam.name();
		SignedTokenInterface token = synapseClient.hexDecodeAndSerialize(tokenTypeName, encodedNotificationSettingsToken);
	}
	
	@Test
	public void testHandleSignedTokenNotificationSettings() throws RestServiceException, SynapseException{
		String tokenTypeName = NotificationTokenType.Settings.name();
		SignedTokenInterface token = synapseClient.hexDecodeAndSerialize(tokenTypeName, encodedNotificationSettingsToken);
		synapseClient.handleSignedToken(token, TEST_HOME_PAGE_BASE);
		verify(mockSynapse).updateNotificationSettings(notificationSettingsToken);
	}
	
	@Test(expected = BadRequestException.class)
	public void testHandleSignedTokenInvalidNotificationSettings() throws RestServiceException, SynapseException{
		String tokenTypeName = NotificationTokenType.Settings.name();
		SignedTokenInterface token = synapseClient.hexDecodeAndSerialize(tokenTypeName, "invalid token");
	}
	@Test(expected = BadRequestException.class)
	public void testHandleSignedTokenNotificationSettingsWrongToken() throws RestServiceException, SynapseException{
		String tokenTypeName = NotificationTokenType.Settings.name();
		SignedTokenInterface token = synapseClient.hexDecodeAndSerialize(tokenTypeName, encodedJoinTeamToken);
	}
	
	@Test
	public void testGetOrCreateActivityForEntityVersionGet() throws SynapseException, RestServiceException {
		when(mockSynapse.getActivityForEntityVersion(anyString(), anyLong())).thenReturn(new Activity());
		synapseClient.getOrCreateActivityForEntityVersion(entityId, version);
		verify(mockSynapse).getActivityForEntityVersion(entityId, version);
	}
	
	@Test
	public void testGetOrCreateActivityForEntityVersionCreate() throws SynapseException, RestServiceException {
		when(mockSynapse.getActivityForEntityVersion(anyString(), anyLong())).thenThrow(new SynapseNotFoundException());
		when(mockSynapse.createActivity(any(Activity.class))).thenReturn(mockActivity);
		synapseClient.getOrCreateActivityForEntityVersion(entityId, version);
		verify(mockSynapse).getActivityForEntityVersion(entityId, version);
		verify(mockSynapse).createActivity(any(Activity.class));
		verify(mockSynapse).putEntity(mockSynapse.getEntityById(entityId), mockActivity.getId());
	}
	
	@Test(expected = Exception.class)
	public void testGetOrCreateActivityForEntityVersionFailure() throws SynapseException, RestServiceException {
		when(mockSynapse.getActivityForEntityVersion(anyString(), anyLong())).thenThrow(new Exception());
		synapseClient.getOrCreateActivityForEntityVersion(entityId, version);
	}
	
	private void setupGetMyLocationSettings() throws SynapseException, RestServiceException{
		List<StorageLocationSetting> existingStorageLocations = new ArrayList<StorageLocationSetting>();
		StorageLocationSetting storageLocation = new ExternalS3StorageLocationSetting();
		storageLocation.setStorageLocationId(1L);
		storageLocation.setBanner("Banner 1");
		existingStorageLocations.add(storageLocation);
		
		storageLocation = new ExternalStorageLocationSetting();
		storageLocation.setStorageLocationId(2L);
		storageLocation.setBanner("Another Banner");
		((ExternalStorageLocationSetting)storageLocation).setUrl("sftp://www.jayhodgson.com");
		existingStorageLocations.add(storageLocation);
		
		storageLocation = new ExternalStorageLocationSetting();
		storageLocation.setStorageLocationId(3L);
		storageLocation.setBanner("Banner 1");
		existingStorageLocations.add(storageLocation);
		
		when(mockSynapse.getMyStorageLocationSettings()).thenReturn(existingStorageLocations);
	}
	
	@Test
	public void testGetMyLocationSettingBanners() throws SynapseException, RestServiceException {
		setupGetMyLocationSettings();
		List<String> banners = synapseClient.getMyLocationSettingBanners();
		verify(mockSynapse).getMyStorageLocationSettings();
		//should be 2 (only returns unique values)
		assertEquals(2, banners.size());
		//and alphabetically sorted
		assertEquals(Arrays.asList("Another Banner", "Banner 1"), banners);
	}
	
	@Test(expected = Exception.class)
	public void testGetMyLocationSettingBannersFailure() throws SynapseException, RestServiceException {
		when(mockSynapse.getMyStorageLocationSettings()).thenThrow(new Exception());
		synapseClient.getMyLocationSettingBanners();
	}
	
	@Test
	public void testGetStorageLocationSettingNullSetting() throws SynapseException, RestServiceException {
		when(mockSynapse.getProjectSetting(entityId, ProjectSettingsType.upload)).thenReturn(null);
		assertNull(synapseClient.getStorageLocationSetting(entityId));
	}
	
	@Test
	public void testGetStorageLocationSettingEmptyLocations() throws SynapseException, RestServiceException {
		UploadDestinationListSetting setting = new UploadDestinationListSetting();
		setting.setLocations(Collections.EMPTY_LIST);
		when(mockSynapse.getProjectSetting(entityId, ProjectSettingsType.upload)).thenReturn(setting);
		assertNull(synapseClient.getStorageLocationSetting(entityId));
	}
	
	@Test
	public void testGetStorageLocationSetting() throws SynapseException, RestServiceException {
		UploadDestinationListSetting setting = new UploadDestinationListSetting();
		setting.setLocations(Collections.singletonList(42L));
		when(mockSynapse.getProjectSetting(entityId, ProjectSettingsType.upload)).thenReturn(setting);
		StorageLocationSetting mockStorageLocationSetting = Mockito.mock(StorageLocationSetting.class);
		when(mockSynapse.getMyStorageLocationSetting(anyLong())).thenReturn(mockStorageLocationSetting);
		assertEquals(mockStorageLocationSetting, synapseClient.getStorageLocationSetting(entityId));
	}
	
	@Test(expected = Exception.class)
	public void testGetStorageLocationSettingFailure() throws SynapseException, RestServiceException {
		when(mockSynapse.getMyStorageLocationSetting(anyLong())).thenThrow(new Exception());
		synapseClient.getStorageLocationSetting(entityId);
	}
	
	@Test
	public void testCreateStorageLocationSettingFoundStorageAndProjectSetting() throws SynapseException, RestServiceException {
		setupGetMyLocationSettings();
		
		UploadDestinationListSetting projectSetting = new UploadDestinationListSetting();
		projectSetting.setLocations(Collections.EMPTY_LIST);
		when(mockSynapse.getProjectSetting(entityId, ProjectSettingsType.upload)).thenReturn(projectSetting);
		
		//test the case when it finds a duplicate storage location.
		ExternalStorageLocationSetting setting = new ExternalStorageLocationSetting();
		setting.setBanner("Another Banner");
		setting.setUrl("sftp://www.jayhodgson.com");
		
		synapseClient.createStorageLocationSetting(entityId, setting);
		//should have found the duplicate storage location, so this is never called
		verify(mockSynapse, Mockito.never()).createStorageLocationSetting(any(StorageLocationSetting.class));
		//verify updates project setting, and the new location list is a single value (id of existing storage location)
		ArgumentCaptor<ProjectSetting> captor = ArgumentCaptor.forClass(ProjectSetting.class);
		verify(mockSynapse).updateProjectSetting(captor.capture());
		UploadDestinationListSetting updatedProjectSetting = (UploadDestinationListSetting)captor.getValue();
		List<Long> locations = updatedProjectSetting.getLocations();
		assertEquals(new Long(2), locations.get(0));
	}
	

	@Test
	public void testCreateStorageLocationSettingNewStorageAndProjectSetting() throws SynapseException, RestServiceException {
		setupGetMyLocationSettings();
		when(mockSynapse.getProjectSetting(entityId, ProjectSettingsType.upload)).thenReturn(null);
		
		//test the case when it does not find duplicate storage location setting.
		ExternalStorageLocationSetting setting = new ExternalStorageLocationSetting();
		setting.setBanner("Another Banner");
		setting.setUrl("sftp://www.google.com");
		
		Long newStorageLocationId = 1007L;
		ExternalStorageLocationSetting createdSetting = new ExternalStorageLocationSetting();
		createdSetting.setStorageLocationId(newStorageLocationId);
		
		when(mockSynapse.createStorageLocationSetting(any(StorageLocationSetting.class))).thenReturn(createdSetting);
		
		synapseClient.createStorageLocationSetting(entityId, setting);
		//should not have found a duplicate storage location, so this should be called
		verify(mockSynapse).createStorageLocationSetting(any(StorageLocationSetting.class));
		//verify creates new project setting, and the new location list is a single value (id of the new storage location)
		ArgumentCaptor<ProjectSetting> captor = ArgumentCaptor.forClass(ProjectSetting.class);
		verify(mockSynapse).createProjectSetting(captor.capture());
		UploadDestinationListSetting updatedProjectSetting = (UploadDestinationListSetting)captor.getValue();
		List<Long> locations = updatedProjectSetting.getLocations();
		assertEquals(newStorageLocationId, locations.get(0));
		assertEquals(ProjectSettingsType.upload, updatedProjectSetting.getSettingsType());
		assertEquals(entityId, updatedProjectSetting.getProjectId());
	}
	
	@Test(expected = Exception.class)
	public void testCreateStorageLocationSettingFailure() throws SynapseException, RestServiceException {
		when(mockSynapse.getMyStorageLocationSetting(anyLong())).thenThrow(new Exception());
		synapseClient.createStorageLocationSetting(entityId, new ExternalStorageLocationSetting());
	}
}
