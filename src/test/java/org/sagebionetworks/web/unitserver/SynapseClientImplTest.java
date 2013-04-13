package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ANNOTATIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY_PATH;
import static org.sagebionetworks.web.shared.EntityBundleTransport.HAS_CHILDREN;
import static org.sagebionetworks.web.shared.EntityBundleTransport.PERMISSIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.UNMET_ACCESS_REQUIREMENTS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.client.exceptions.SynapseNotFoundException;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LayerTypeNames;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.LocationTypeNames;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.VariableContentPaginatedResults;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.repo.model.file.ChunkRequest;
import org.sagebionetworks.repo.model.file.ChunkedFileToken;
import org.sagebionetworks.repo.model.file.CompleteChunkedFileRequest;
import org.sagebionetworks.repo.model.file.CreateChunkedFileTokenRequest;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.storage.StorageUsage;
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
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;

/**
 * Test for the SynapseClientImpl
 * @author John
 *
 */
public class SynapseClientImplTest {
	
	SynapseProvider mockSynapseProvider;
	TokenProvider mockTokenProvider;
	ServiceUrlProvider mockUrlProvider;
	Synapse mockSynapse;
	SynapseClientImpl synapseClient;
	
	String entityId = "123";
	ExampleEntity entity;
	AttachmentData attachment1, attachment2;
	Annotations annos;
	UserEntityPermissions eup;
	EntityPath path;
	org.sagebionetworks.repo.model.PaginatedResults<UserGroup> pgugs;
	org.sagebionetworks.repo.model.PaginatedResults<UserProfile> pgups;
	AccessControlList acl;
	WikiPage page;
	
	
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl();
	private static JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
	private static NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(jsonEntityFactory, jsonObjectAdapter);

	@Before
	public void before() throws SynapseException, JSONObjectAdapterException{
		mockSynapse = Mockito.mock(Synapse.class);
		mockSynapseProvider = Mockito.mock(SynapseProvider.class);
		mockUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		when(mockSynapseProvider.createNewClient()).thenReturn(mockSynapse);
		mockTokenProvider = Mockito.mock(TokenProvider.class);
		synapseClient = new SynapseClientImpl();
		synapseClient.setSynapseProvider(mockSynapseProvider);
		synapseClient.setTokenProvider(mockTokenProvider);
		synapseClient.setServiceUrlProvider(mockUrlProvider);
		
		// Setup the the entity
		entity = new ExampleEntity();
		entity.setId(entityId);
		entity.setEntityType(ExampleEntity.class.getName());
		List<AttachmentData> attachments = new ArrayList<AttachmentData>();
		attachment1 = new AttachmentData();
		attachment1.setName("attachment1");
		attachment2 = new AttachmentData();
		attachment2.setName("attachment2");
		attachments.add(attachment1);
		attachments.add(attachment2);
		entity.setAttachments(attachments);
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
		// Setup the path
		path = new EntityPath();
		path.setPath(new ArrayList<EntityHeader>());
		EntityHeader header = new EntityHeader();
		header.setId(entityId);
		header.setName("RomperRuuuu");
		path.getPath().add(header);
		// the mock synapse should return this object
		when(mockSynapse.getEntityPath(entityId)).thenReturn(path);
		
		pgugs = new org.sagebionetworks.repo.model.PaginatedResults<UserGroup>();
		List<UserGroup> ugs = new ArrayList<UserGroup>();
		ugs.add(new UserGroup());
		pgugs.setResults(ugs);
		when(mockSynapse.getGroups(anyInt(), anyInt())).thenReturn(pgugs);

		pgups = new org.sagebionetworks.repo.model.PaginatedResults<UserProfile>();
		List<UserProfile> ups = new ArrayList<UserProfile>();
		ups.add(new UserProfile());
		pgups.setResults(ups);
		when(mockSynapse.getUsers(anyInt(), anyInt())).thenReturn(pgups);
		
		acl  = new AccessControlList();
		acl.setId("sys999");
		Set<ResourceAccess> ras = new HashSet<ResourceAccess>();
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(101L);
		ra.setAccessType(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_ADMINISTER));
		acl.setResourceAccess(ras);
		when(mockSynapse.getACL(anyString())).thenReturn(acl);	
		when(mockSynapse.createACL((AccessControlList)any())).thenReturn(acl);
		when(mockSynapse.updateACL((AccessControlList)any())).thenReturn(acl);
		when(mockSynapse.updateACL((AccessControlList)any(), eq(true))).thenReturn(acl);
		when(mockSynapse.updateACL((AccessControlList)any(), eq(false))).thenReturn(acl);

		EntityHeader bene = new EntityHeader();
		bene.setId("syn999");
		when(mockSynapse.getEntityBenefactor(anyString())).thenReturn(bene);
		
		List<AccessRequirement> accessRequirements= new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirements.add(accessRequirement);
		accessRequirement.setEntityType(TermsOfUseAccessRequirement.class.getName());
		accessRequirement.setEntityIds(Arrays.asList(new String[]{"101"}));
		
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | 
		HAS_CHILDREN | ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS;
		int emptyMask = 0;
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(entity);
		bundle.setAnnotations(annos);
		bundle.setPermissions(eup);
		bundle.setPath(path);
		bundle.setHasChildren(false);
		bundle.setAccessRequirements(accessRequirements);
		bundle.setUnmetAccessRequirements(accessRequirements);
		when(mockSynapse.getEntityBundle(anyString(),Matchers.eq(mask))).thenReturn(bundle);
		
		EntityBundle emptyBundle = new EntityBundle();
		when(mockSynapse.getEntityBundle(anyString(),Matchers.eq(emptyMask))).thenReturn(emptyBundle);
		
		when(mockSynapse.canAccess("syn101", ACCESS_TYPE.READ)).thenReturn(true);
		
		page = new WikiPage();
		page.setId("testId");
		page.setMarkdown("my markdown");
		page.setParentWikiId(null);
		page.setTitle("A Title");
		S3FileHandle handle = new S3FileHandle();
		handle.setId("4422");
		when(mockSynapse.completeChunkFileUpload(any(CompleteChunkedFileRequest.class))).thenReturn(handle);
		VariableContentPaginatedResults<AccessRequirement> ars = new VariableContentPaginatedResults<AccessRequirement>();
		ars.setTotalNumberOfResults(0);
		ars.setResults(new ArrayList<AccessRequirement>());
		when(mockSynapse.getAccessRequirements(anyString())).thenReturn(ars);
	}
	
	@Test
	public void testGetEntityBundleAll() throws RestServiceException{
		// Make sure we can get all parts of the bundel
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN
		| ACCESS_REQUIREMENTS | UNMET_ACCESS_REQUIREMENTS;
		EntityBundleTransport bundle = synapseClient.getEntityBundle(entityId, mask);
		assertNotNull(bundle);
		// We should have all of the strings
		assertNotNull(bundle.getEntityJson());
		assertNotNull(bundle.getAnnotationsJson());
		assertNotNull(bundle.getEntityPathJson());
		assertNotNull(bundle.getPermissionsJson());
		assertNotNull(bundle.getHasChildren());
		assertNotNull(bundle.getAccessRequirementsJson());
		assertNotNull(bundle.getUnmetAccessRequirementsJson());
	}
	
	@Test
	public void testGetEntityBundleNone() throws RestServiceException{
		// Make sure all are null
		int mask = 0x0;
		EntityBundleTransport bundle = synapseClient.getEntityBundle(entityId, mask);
		assertNotNull(bundle);
		// We should have all of the strings
		assertNull(bundle.getEntityJson());
		assertNull(bundle.getAnnotationsJson());
		assertNull(bundle.getEntityPathJson());
		assertNull(bundle.getPermissionsJson());
		assertNull(bundle.getHasChildren());
		assertNull(bundle.getAccessRequirementsJson());
		assertNull(bundle.getUnmetAccessRequirementsJson());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testParseEntityFromJsonNoType() throws JSONObjectAdapterException{
		ExampleEntity example = new ExampleEntity();
		example.setName("some name");
		example.setDescription("some description");
		// do not set the type
		String json = EntityFactory.createJSONStringForEntity(example);
		// This will fail as the type is required
		synapseClient.parseEntityFromJson(json);
	}
	
	@Test
	public void testParseEntityFromJson() throws JSONObjectAdapterException{
		ExampleEntity example = new ExampleEntity();
		example.setName("some name");
		example.setDescription("some description");
		example.setEntityType(ExampleEntity.class.getName());
		String json = EntityFactory.createJSONStringForEntity(example);
		// System.out.println(json);
		// Now make sure this can be read back
		ExampleEntity clone = (ExampleEntity) synapseClient.parseEntityFromJson(json);
		assertEquals(example, clone);
	}
	
	@Test
	public void testCreateOrUpdateEntityFalse() throws JSONObjectAdapterException, RestServiceException, SynapseException{
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
	public void testCreateOrUpdateEntityTrue() throws JSONObjectAdapterException, RestServiceException, SynapseException{
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
	public void testCreateOrUpdateEntityTrueWithAnnos() throws JSONObjectAdapterException, RestServiceException, SynapseException{
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
	public void testGetNodeAcl() throws Exception {
		EntityWrapper ew = synapseClient.getNodeAcl("syn101");
		AccessControlList clone = EntityFactory.createEntityFromJSONString(ew.getEntityJson(), AccessControlList.class);
		assertEquals(acl, clone);
	}
	
	@Test
	public void testCreateAcl() throws Exception {
		EntityWrapper in = new EntityWrapper();
		in.setEntityJson(EntityFactory.createJSONObjectForEntity(acl).toString());
		EntityWrapper ew = synapseClient.createAcl(in);
		AccessControlList clone = EntityFactory.createEntityFromJSONString(ew.getEntityJson(), AccessControlList.class);
		assertEquals(acl, clone);
	}

	@Test
	public void testUpdateAcl() throws Exception {
		EntityWrapper in = new EntityWrapper();
		in.setEntityJson(EntityFactory.createJSONObjectForEntity(acl).toString());
		EntityWrapper ew = synapseClient.updateAcl(in);
		AccessControlList clone = EntityFactory.createEntityFromJSONString(ew.getEntityJson(), AccessControlList.class);
		assertEquals(acl, clone);
	}
	
	@Test
	public void testUpdateAclRecursive() throws Exception {
		EntityWrapper in = new EntityWrapper();
		in.setEntityJson(EntityFactory.createJSONObjectForEntity(acl).toString());
		EntityWrapper ew = synapseClient.updateAcl(in, true);
		AccessControlList clone = EntityFactory.createEntityFromJSONString(ew.getEntityJson(), AccessControlList.class);
		assertEquals(acl, clone);
		verify(mockSynapse).updateACL(any(AccessControlList.class), eq(true));
	}

	@Test
	public void testDeleteAcl() throws Exception {
		EntityWrapper ew = synapseClient.deleteAcl("syn101");
		AccessControlList clone = EntityFactory.createEntityFromJSONString(ew.getEntityJson(), AccessControlList.class);
		assertEquals(acl, clone);
	}

	@Test
	public void testHasAccess() throws Exception {
		assertTrue(synapseClient.hasAccess("syn101", "READ"));
	}


	@Test
	public void testGetAllUsers() throws Exception {
		EntityWrapper ew = synapseClient.getAllUsers();
		org.sagebionetworks.web.shared.PaginatedResults<UserProfile> clone = 
			nodeModelCreator.createPaginatedResults(ew.getEntityJson(), UserProfile.class);
		assertEquals(this.pgups.getResults(), clone.getResults());
	}


	@Test
	public void testGetAllGroups() throws Exception {
		EntityWrapper ew = synapseClient.getAllGroups();
		org.sagebionetworks.web.shared.PaginatedResults<UserGroup> clone = 
			nodeModelCreator.createPaginatedResults(ew.getEntityJson(), UserGroup.class);
		assertEquals(this.pgugs.getResults(), clone.getResults());
	}
	
	@Test
	public void testGetUserProfile() throws Exception {
		//verify call is directly calling the synapse client provider
		JSONObject testUserJSONObject = new JSONObject("{ username: \"Test User\"}");
		String testRepoUrl = "http://mytestrepourl";
		String testUserId = "myUserId";
		when(mockUrlProvider.getRepositoryServiceUrl()).thenReturn(testRepoUrl);
		when(mockSynapse.getSynapseEntity(testRepoUrl, "/userProfile/" + testUserId)).thenReturn(testUserJSONObject);
		String userProfile = synapseClient.getUserProfile(testUserId);
		assertEquals(userProfile, testUserJSONObject.toString());
	}
	
	@Test
	public void testCreateUserProfileAttachment() throws Exception {
		//verify call is directly calling the synapse client provider
		PresignedUrl testPresignedUrl = new PresignedUrl();
		testPresignedUrl.setPresignedUrl("http://mytestpresignedurl");
		String testId = "myTestId";
		String testToken = "myTestToken";
		when(mockSynapse.createUserProfileAttachmentPresignedUrl(testId, testToken)).thenReturn(testPresignedUrl);
		String presignedUrl = synapseClient.createUserProfileAttachmentPresignedUrl(testId, testToken);
		assertEquals(presignedUrl, EntityFactory.createJSONStringForEntity(testPresignedUrl));
	}
	
	@Test
	public void testUpdateLocationable() throws Exception {
		//verify call is directly calling the synapse client provider
		String testUrl = "http://mytesturl/something.jpg";
		List<LocationData> locations = new ArrayList<LocationData>();
		LocationData externalLocation = new LocationData();
		externalLocation.setPath(testUrl);
		externalLocation.setType(LocationTypeNames.external);
		locations.add(externalLocation);

		Data layer = new Data();
		layer.setType(LayerTypeNames.M);
		layer.setLocations(locations);

		String testId = "myTestId";
		when(mockSynapse.updateExternalLocationableToSynapse(layer, testUrl)).thenReturn(layer);
		when(mockSynapse.getEntityById(testId)).thenReturn(layer);
		EntityWrapper returnedLayer = synapseClient.updateExternalLocationable(testId, testUrl);
		
		assertEquals(returnedLayer.getEntityJson(), EntityFactory.createJSONStringForEntity(layer));
	}
	
	@Test
	public void testGetStorageUsage() throws Exception {
		//verify call is directly calling the synapse client provider.
		PaginatedResults<StorageUsage> testPaginatedResults = new PaginatedResults<StorageUsage>();
		StorageUsage expectedUsage = new StorageUsage();
		Long expectedSize = 12345l;
		expectedUsage.setId("usageId");
		expectedUsage.setContentSize(expectedSize);
		List<StorageUsage> list = new ArrayList<StorageUsage>();
		list.add(expectedUsage);
		testPaginatedResults.setResults(list);
		testPaginatedResults.setTotalNumberOfResults(1l);
		
		when(mockSynapse.getItemizedStorageUsageForNode(anyString(), anyInt(), anyInt())).thenReturn(testPaginatedResults);
		Long actual = synapseClient.getStorageUsage(entityId);
		assertEquals(expectedSize, actual);
	}
	
	@Test
	public void testRemoveAttachmentFromEntity() throws Exception {

		Mockito.when(mockSynapse.putEntity(any(ExampleEntity.class))).thenReturn(entity);
		
		ArgumentCaptor<ExampleEntity> arg = ArgumentCaptor.forClass(ExampleEntity.class);
		
		synapseClient.removeAttachmentFromEntity(entityId, attachment2.getName());
	    
		//test to see if attachment has been removed
		verify(mockSynapse).getEntityById(entityId);
		verify(mockSynapse).putEntity(arg.capture());
		
		 //verify that attachment2 has been removed
		ExampleEntity updatedEntity = arg.getValue();
		List<AttachmentData> attachments = updatedEntity.getAttachments();
		assertTrue(attachments.size() == 1 && attachments.get(0).equals(attachment1));
	}
	
	@Test
	public void testGetJSONEntity() throws Exception {

		JSONObject json = EntityFactory.createJSONObjectForEntity(entity);
		Mockito.when(mockSynapse.getEntity(anyString())).thenReturn(json);
		
		String testRepoUri = "/testservice";
		
		synapseClient.getJSONEntity(testRepoUri);
		//verify that this call uses Synapse.getEntity(testRepoUri)
	    verify(mockSynapse).getEntity(testRepoUri);
	}
	
	@Test
	public void testCreateWikiPage() throws Exception {
		String wikiPageJson = EntityFactory.createJSONStringForEntity(page);
		Mockito.when(mockSynapse.createWikiPage(anyString(), any(ObjectType.class), any(WikiPage.class))).thenReturn(page);
		synapseClient.createWikiPage("testId", WidgetConstants.WIKI_OWNER_ID_ENTITY, wikiPageJson);
	    verify(mockSynapse).createWikiPage(anyString(), any(ObjectType.class), any(WikiPage.class));
	}
	
	@Test
	public void testDeleteWikiPage() throws Exception {
		synapseClient.deleteWikiPage(new WikiPageKey("syn123", WidgetConstants.WIKI_OWNER_ID_ENTITY, "20"));
		verify(mockSynapse).deleteWikiPage(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));
	}
	
	@Test
	public void testGetWikiHeaderTree() throws Exception {
		PaginatedResults<WikiHeader> headerTreeResults = new PaginatedResults<WikiHeader>();
		when(mockSynapse.getWikiHeaderTree(anyString(), any(ObjectType.class))).thenReturn(headerTreeResults);
		synapseClient.getWikiHeaderTree("testId", WidgetConstants.WIKI_OWNER_ID_ENTITY);
	    verify(mockSynapse).getWikiHeaderTree(anyString(), any(ObjectType.class));
	}
	
	@Test
	public void testGetWikiPage() throws Exception {
		Mockito.when(mockSynapse.getWikiPage(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class))).thenReturn(page);
		synapseClient.getWikiPage(new WikiPageKey("syn123", WidgetConstants.WIKI_OWNER_ID_ENTITY, "20"));
	    verify(mockSynapse).getWikiPage(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));
	}
	
	@Test
	public void testUpdateWikiPage() throws Exception {
		String wikiPageJson = EntityFactory.createJSONStringForEntity(page);
		Mockito.when(mockSynapse.updateWikiPage(anyString(), any(ObjectType.class), any(WikiPage.class))).thenReturn(page);
		synapseClient.updateWikiPage("testId", WidgetConstants.WIKI_OWNER_ID_ENTITY, wikiPageJson);
		
		verify(mockSynapse).updateWikiPage(anyString(), any(ObjectType.class), any(WikiPage.class));
	}

	@Test
	public void testGetWikiAttachmentHandles() throws Exception {
		FileHandleResults testResults = new FileHandleResults();
		Mockito.when(mockSynapse.getWikiAttachmenthHandles(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class))).thenReturn(testResults);
		synapseClient.getWikiAttachmentHandles(new WikiPageKey("syn123", WidgetConstants.WIKI_OWNER_ID_ENTITY, "20"));
	    verify(mockSynapse).getWikiAttachmenthHandles(any(org.sagebionetworks.repo.model.dao.WikiPageKey.class));
	}

	@Test
	public void testUpdateExternalFileHandle() throws Exception {
		//verify call is directly calling the synapse client provider, and it tries to rename the entity to the filename
		String myFileName = "testFileName.csv";
		String testUrl = "http://mytesturl/"+myFileName;
		String testId = "myTestId";
		FileEntity file = new FileEntity();
		String originalFileEntityName = "syn1223";
		file.setName(originalFileEntityName);
		file.setId(testId);
		file.setDataFileHandleId("handle1");
		ExternalFileHandle handle = new ExternalFileHandle();
		handle.setExternalURL(testUrl);
		
		when(mockSynapse.getEntityById(testId)).thenReturn(file);
		when(mockSynapse.createExternalFileHandle(any(ExternalFileHandle.class))).thenReturn(handle);
		when(mockSynapse.putEntity(any(FileEntity.class))).thenReturn(file);
		
		ArgumentCaptor<FileEntity> arg = ArgumentCaptor.forClass(FileEntity.class);
		
		synapseClient.updateExternalFile(testId, testUrl);
		
		verify(mockSynapse).getEntityById(testId);
		verify(mockSynapse).createExternalFileHandle(any(ExternalFileHandle.class));
		verify(mockSynapse, Mockito.times(2)).putEntity(arg.capture());
		
		//verify rename
		FileEntity fileEntityArg = arg.getValue();	//last value captured
		assertEquals(myFileName, fileEntityArg.getName());
		
		//and if rename fails, verify all is well (but the FileEntity name is not updated)
		file.setName(originalFileEntityName);
		//first call should return file, second call to putEntity should throw an exception
		when(mockSynapse.putEntity(any(FileEntity.class))).thenReturn(file).thenThrow(new IllegalArgumentException("invalid name for some reason"));
		synapseClient.updateExternalFile(testId, testUrl);
		
		//second time calling createExternalFileHandle
		verify(mockSynapse, Mockito.times(2)).createExternalFileHandle(any(ExternalFileHandle.class));
		//and it should have called putEntity 2 additional times
		verify(mockSynapse, Mockito.times(4)).putEntity(arg.capture());
		fileEntityArg = arg.getValue();	//last value captured
		assertEquals(originalFileEntityName, fileEntityArg.getName());
	}

	@Test
	public void testGetEntityDoi() throws Exception {
		//wiring test
		Doi testDoi = new Doi();
		testDoi.setDoiStatus(DoiStatus.READY);
		testDoi.setId("test doi id");
		testDoi.setCreatedBy("Test User");
		testDoi.setCreatedOn(new Date());
		testDoi.setObjectId("syn1234");
		Mockito.when(mockSynapse.getEntityDoi(anyString(), anyLong())).thenReturn(testDoi);
		synapseClient.getEntityDoi("test entity id", null);
	    verify(mockSynapse).getEntityDoi(anyString(), anyLong());
	}
	
	private FileEntity getTestFileEntity() {
		FileEntity testFileEntity = new FileEntity();
		testFileEntity.setId("5544");
		testFileEntity.setName("testFileEntity.R");
		return testFileEntity;
	}
	
	@Test (expected=NotFoundException.class)
	public void testGetEntityDoiNotFound() throws Exception {
		//wiring test
		Mockito.when(mockSynapse.getEntityDoi(anyString(), anyLong())).thenThrow(new SynapseNotFoundException());
		synapseClient.getEntityDoi("test entity id", null);
	}
	
	@Test
	public void testCreateDoi() throws Exception {
		//wiring test
		synapseClient.createDoi("test entity id", null);
		verify(mockSynapse).createEntityDoi(anyString(), anyLong());
	}

	
	private String getTestChunkRequestJson() throws JSONObjectAdapterException {
		ChunkRequest chunkRequest = new ChunkRequest();
		ChunkedFileToken token = new ChunkedFileToken();
		token.setKey("test key");
		chunkRequest.setChunkedFileToken(token);
		chunkRequest.setChunkNumber(1l);
		return EntityFactory.createJSONStringForEntity(chunkRequest);
	}
	
	/**
	 * Direct upload tests.  Most of the methods are simple pass-throughs to the Java Synapse client, but completeChunkedFileUpload has
	 * additional logic
	 * @throws JSONObjectAdapterException 
	 * @throws SynapseException 
	 * @throws RestServiceException 
	 */
	@Test
	public void testCompleteChunkedFileUpload() throws JSONObjectAdapterException, SynapseException, RestServiceException {
		String chunkRequestJson = getTestChunkRequestJson();
		FileEntity testFileEntity = getTestFileEntity();
		when(mockSynapse.createEntity(any(FileEntity.class))).thenReturn(testFileEntity);
		when(mockSynapse.putEntity(any(FileEntity.class))).thenReturn(testFileEntity);
		boolean isRestricted = true;
		synapseClient.completeChunkedFileUpload(null, chunkRequestJson, "syn1", isRestricted);
		
		verify(mockSynapse).completeChunkFileUpload(any(CompleteChunkedFileRequest.class));
		//it should have tried to create a new entity (since entity id was null)
		verify(mockSynapse).createEntity(any(FileEntity.class));
		//and update the name
		verify(mockSynapse).putEntity(any(FileEntity.class));
		//and lock down
		verify(mockSynapse).createAccessRequirement(any(AccessRequirement.class));
	}
	
	@Test
	public void testCompleteChunkedFileUploadExistingEntity() throws JSONObjectAdapterException, SynapseException, RestServiceException {
		String chunkRequestJson = getTestChunkRequestJson();
		FileEntity testFileEntity = getTestFileEntity();
		when(mockSynapse.getEntityById(anyString())).thenReturn(testFileEntity);
		when(mockSynapse.createEntity(any(FileEntity.class))).thenThrow(new AssertionError("No need to create a new entity!"));
		when(mockSynapse.putEntity(any(FileEntity.class))).thenReturn(testFileEntity);
		boolean isRestricted = false;
		synapseClient.completeChunkedFileUpload(entityId, chunkRequestJson, "syn1", isRestricted);
		
		verify(mockSynapse).completeChunkFileUpload(any(CompleteChunkedFileRequest.class));
		//it should have tried to find the entity
		verify(mockSynapse).getEntityById(anyString());
		//update the data file handle id, and update the name
		verify(mockSynapse, Mockito.times(2)).putEntity(any(FileEntity.class));
		//do not lock down (restricted=false)
		verify(mockSynapse, Mockito.times(0)).createAccessRequirement(any(AccessRequirement.class));
	}

	@Test
	public void testGetChunkedFileToken() throws SynapseException, RestServiceException, JSONObjectAdapterException {
		String fileName = "test file.zip";
		Long chunkNumber = 222l;
		String contentType = "application/test";
		ChunkedFileToken testToken = new ChunkedFileToken();
		testToken.setFileName(fileName);
		testToken.setKey("a key 42");
		testToken.setUploadId("upload ID 123");
		when(mockSynapse.createChunkedFileUploadToken(any(CreateChunkedFileTokenRequest.class))).thenReturn(testToken);
		
		String requestJson = synapseClient.getChunkedFileToken(fileName, contentType, chunkNumber);
		ChunkRequest request = EntityFactory.createEntityFromJSONString(requestJson, ChunkRequest.class);
		verify(mockSynapse).createChunkedFileUploadToken(any(CreateChunkedFileTokenRequest.class));
		assertEquals(testToken, request.getChunkedFileToken());
		assertEquals(chunkNumber, request.getChunkNumber());
	}
	
	@Test
	public void testGetChunkedPresignedUrl() throws SynapseException, RestServiceException, MalformedURLException, JSONObjectAdapterException {
		URL testUrl = new URL("http://test.presignedurl.com/foo");
		when(mockSynapse.createChunkedPresignedUrl(any(ChunkRequest.class))).thenReturn(testUrl);
		String presignedUrl = synapseClient.getChunkedPresignedUrl(getTestChunkRequestJson());
		verify(mockSynapse).createChunkedPresignedUrl(any(ChunkRequest.class));
		assertEquals(testUrl.toString(), presignedUrl);
	}
}
