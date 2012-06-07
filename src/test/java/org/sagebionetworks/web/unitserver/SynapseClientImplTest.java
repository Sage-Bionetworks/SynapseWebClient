package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ANNOTATIONS;
import static org.sagebionetworks.web.shared.EntityBundleTransport.CHILD_COUNT;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;
import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY_PATH;
import static org.sagebionetworks.web.shared.EntityBundleTransport.PERMISSIONS;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.server.servlet.ServiceUrlProvider;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl;
import org.sagebionetworks.web.server.servlet.SynapseProvider;
import org.sagebionetworks.web.server.servlet.TokenProvider;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

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
	}
	
	@Test
	public void testHandleEntity() throws SynapseException, JSONObjectAdapterException{
		// we want the entity
		int mask = ENTITY;
		EntityBundleTransport transprot =new EntityBundleTransport();
		// Make the call
		synapseClient.handleEntity(entity.getId(), mask, transprot, mockSynapse);
		assertNotNull(transprot.getEntityJson());
		ExampleEntity clone = EntityFactory.createEntityFromJSONString(transprot.getEntityJson(), ExampleEntity.class);
		assertEquals(entity, clone);
	}
	
	@Test
	public void testHandleAnnotations() throws SynapseException, JSONObjectAdapterException{
		// we want the entity
		int mask = ANNOTATIONS;
		EntityBundleTransport transprot =new EntityBundleTransport();
		// Make the call
		synapseClient.handleAnnotations(annos.getId(), mask, transprot, mockSynapse);
		assertNotNull(transprot.getAnnotationsJson());
		Annotations clone = EntityFactory.createEntityFromJSONString(transprot.getAnnotationsJson(), Annotations.class);
		assertEquals(annos, clone);
	}
	
	@Test
	public void testHandlePermissions() throws SynapseException, JSONObjectAdapterException{
		// we want the entity
		int mask = PERMISSIONS;
		EntityBundleTransport transprot =new EntityBundleTransport();
		// Make the call
		synapseClient.handlePermissions(entityId, mask, transprot, mockSynapse);
		assertNotNull(transprot.getPermissionsJson());
		UserEntityPermissions clone = EntityFactory.createEntityFromJSONString(transprot.getPermissionsJson(), UserEntityPermissions.class);
		assertEquals(eup, clone);
	}
	
	@Test
	public void testHandlePath() throws SynapseException, JSONObjectAdapterException{
		// we want the entity
		int mask = ENTITY_PATH;
		EntityBundleTransport transprot =new EntityBundleTransport();
		// Make the call
		synapseClient.handleEntityPath(entityId, mask, transprot, mockSynapse);
		assertNotNull(transprot.getEntityPathJson());
		EntityPath clone = EntityFactory.createEntityFromJSONString(transprot.getEntityPathJson(), EntityPath.class);
		assertEquals(path, clone);
	}
	
	@Test
	public void testHandleUsers() {
		fail("NYI");
	}
	
	@Test
	public void testHandleGroups() {
		fail("NYI");
	}
	
	@Test
	public void testHandleACL() {
		fail("NYI");
	}
	
	@Test
	public void testHandle() {
		fail("NYI");
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
		System.out.println(json);
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
		fail("NYI");
	}

	@Test
	public void testCreateAcl() throws Exception {
		fail("NYI");
	}

	@Test
	public void testUpdateAcl() throws Exception {
		fail("NYI");
	}

	@Test
	public void testDeleteAcl() throws Exception {
		fail("NYI");
	}

	@Test
	public void testHasAccess() throws Exception {
		fail("NYI");
	}


	@Test
	public void testGetAllUsers() throws Exception {
		fail("NYI");
	}


	@Test
	public void testGetAllGroups() throws Exception {
		fail("NYI");
	}
}
