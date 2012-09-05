package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ANNOTATIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.CHILD_COUNT;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY_PATH;
import static org.sagebionetworks.web.shared.EntityBundleTransport.PERMISSIONS;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.PresignedUrl;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
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
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
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
	Annotations annos;
	UserEntityPermissions eup;
	EntityPath path;
	org.sagebionetworks.repo.model.PaginatedResults<UserGroup> pgugs;
	org.sagebionetworks.repo.model.PaginatedResults<UserProfile> pgups;
	AccessControlList acl;
	
	
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl();
	private static JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
	private static NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(jsonEntityFactory, jsonObjectAdapter);

	@Before
	public void before() throws SynapseException{
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
		
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | CHILD_COUNT;
		int emptyMask = 0;
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(entity);
		bundle.setAnnotations(annos);
		bundle.setPermissions(eup);
		bundle.setPath(path);
		bundle.setChildCount(0L);
		when(mockSynapse.getEntityBundle(anyString(),Matchers.eq(mask))).thenReturn(bundle);
		
		EntityBundle emptyBundle = new EntityBundle();
		when(mockSynapse.getEntityBundle(anyString(),Matchers.eq(emptyMask))).thenReturn(emptyBundle);
		
		when(mockSynapse.canAccess("syn101", ACCESS_TYPE.READ)).thenReturn(true);
	}
	
	@Test
	public void testGetEntityBundleAll() throws RestServiceException{
		// Make sure we can get all parts of the bundel
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | CHILD_COUNT;
		EntityBundleTransport bundle = synapseClient.getEntityBundle(entityId, mask);
		assertNotNull(bundle);
		// We should have all of the strings
		assertNotNull(bundle.getEntityJson());
		assertNotNull(bundle.getAnnotationsJson());
		assertNotNull(bundle.getEntityPathJson());
		assertNotNull(bundle.getPermissionsJson());
		assertNotNull(bundle.getChildCount());
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
		assertNull(bundle.getChildCount());
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
}
