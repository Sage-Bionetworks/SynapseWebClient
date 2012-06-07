package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.users.AclEntry;
import org.sagebionetworks.web.shared.users.AclPrincipal;
import org.sagebionetworks.web.shared.users.PermissionLevel;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccessControlListEditorTest {
	
	private AccessControlListEditor createACLE(SynapseClientAsync synapseClient) {
		AccessControlListEditorView view = Mockito.mock(AccessControlListEditorView.class); 
		JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
		AdapterFactory adapterFactory = new AdapterFactoryImpl(); // alt: GwtAdapterFactory
		JSONEntityFactory factory = new JSONEntityFactoryImpl(adapterFactory);
		NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(factory, jsonObjectAdapter);
		AccessControlListEditor acle = new AccessControlListEditor(view,
				synapseClient,
				nodeModelCreator,
				null,
				null, 
				jsonObjectAdapter);
		return acle;
	}
	@Test
	public void createAclTest() throws Exception {
		SynapseClientAsync synapseClient = Mockito.mock(SynapseClientAsync.class);
		// mock getEntityBundle(entityId, partsMask, new AsyncCallback<EntityBundleTransport>()
		// mock synapseClient.createACL, to capture the created acl
		AccessControlListEditor acle = createACLE(synapseClient);
		acle.setResource("syn101");
		acle.asWidget();
		acle.createAcl();
		// TODO capture created ACL and show that it has id syn101 and admin access for owner
	}
	
	@Test
	public void addAccessTest() throws Exception {
		SynapseClientAsync synapseClient = Mockito.mock(SynapseClientAsync.class);
		AccessControlListEditor acle = createACLE(synapseClient);
		Long principalId = 1L;
		PermissionLevel level = PermissionLevel.CAN_ADMINISTER;
		acle.addAccess(principalId, level);
	}
	
	@Test
	public void changeAccessTest() throws Exception {
		SynapseClientAsync synapseClient = Mockito.mock(SynapseClientAsync.class);
		AccessControlListEditor acle = createACLE(synapseClient);
		Long principalId = 1L;
		PermissionLevel level = PermissionLevel.CAN_ADMINISTER;
		acle.changeAccess(principalId, level);
	}
	
	@Test
	public void removeAccessTest() throws Exception {
		SynapseClientAsync synapseClient = Mockito.mock(SynapseClientAsync.class);
		AccessControlListEditor acle = createACLE(synapseClient);
		Long principalId = 1L;
		acle.removeAccess(principalId);
	}
	
	@Test
	public void deleteAclTest() throws Exception {
		SynapseClientAsync synapseClient = Mockito.mock(SynapseClientAsync.class);
		AccessControlListEditor acle = createACLE(synapseClient);
		acle.createAcl();
	}
	
	// tests of utility functions
	
	
	@Test
	public void isInheritedTest() throws Exception {
		AccessControlList acl = createACL();
		assertFalse(AccessControlListEditor.isInherited(acl, /*entityId*/"syn101"));
		assertTrue(AccessControlListEditor.isInherited(acl, /*entityId*/"syn999"));
		}
	
	private static Collection<UserGroup> makeUserGroups() {
		Collection<UserGroup> groups = new ArrayList<UserGroup>();
		UserGroup g = new UserGroup();
		g.setId("2");
		g.setName("bar");
		groups.add(g);
		return groups;
	}
	
	@Test
	public void getAclPrincipalsFromGroupsTest() throws Exception {
		Map<String, AclPrincipal> map = AccessControlListEditor.getAclPrincipalsFromGroups(
				makeUserGroups());
		assertEquals("2", map.keySet().iterator().next());
		AclPrincipal p = map.get("2");
		assertTrue(p!=null);
		assertEquals("bar", p.getDisplayName());
		assertEquals((Long)2L, (Long)p.getPrincipalId());
		assertFalse(p.isIndividual());
		assertFalse(p.isOwner());
	}
	
	private static Collection<UserProfile> makeUserProfiles() {
		Collection<UserProfile> ups = new ArrayList<UserProfile>();
		UserProfile up = new UserProfile();
		up.setDisplayName("foo");
		up.setOwnerId("1");
		ups.add(up);
		return ups;
	}
	
	@Test
	public void getAclPrincipalsFromUsersTest() throws Exception {
		Map<String, AclPrincipal> map = AccessControlListEditor.getAclPrincipalsFromUsers(
				makeUserProfiles(), "1");
		assertEquals("1", map.keySet().iterator().next());
		AclPrincipal p = map.get("1");
		assertTrue(p!=null);
		assertEquals("foo", p.getDisplayName());
		assertEquals((Long)1L, (Long)p.getPrincipalId());
		assertTrue(p.isIndividual());
		assertTrue(p.isOwner());
	}
	
	@Test
	public void findAclEntryTest() throws Exception {
		AccessControlList acl = createACL();
		Map<String,AclPrincipal> allPrincipals = createAllPrincipals();
		List<AclEntry> entries = AccessControlListEditor.getAclEntries(acl, allPrincipals);
		AclEntry entry = AccessControlListEditor.findAclEntry(entries, 1L);
		assertEquals((Long)1L, (Long)entry.getPrincipal().getPrincipalId());
	}
	
	private static AccessControlList createACL() {
		AccessControlList acl = new AccessControlList();
		acl.setId("syn101");
		Set<ResourceAccess> ras = new HashSet<ResourceAccess>();
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(1L);
		Set<ACCESS_TYPE> ats = new HashSet<ACCESS_TYPE>();
		ats.add(ACCESS_TYPE.READ);
		ra.setAccessType(ats);
		ras.add(ra);
		ResourceAccess ra2 = new ResourceAccess();
		ra2.setPrincipalId(2L);
		ats = new HashSet<ACCESS_TYPE>();
		ats.add(ACCESS_TYPE.UPDATE);
		ra2.setAccessType(ats);
		ras.add(ra2);
		acl.setResourceAccess(ras);
		return acl;
	}
	
	private static Map<String,AclPrincipal> createAllPrincipals() {
		Map<String,AclPrincipal> map = new HashMap<String,AclPrincipal>();
		AclPrincipal p = new AclPrincipal();
		p.setIndividual(true);
		p.setOwner(true);
		p.setPrincipalId(1L);
		p.setDisplayName("foo");
		map.put(p.getPrincipalId().toString(), p);
		p = new AclPrincipal();
		p.setIndividual(false);
		p.setOwner(false);
		p.setPrincipalId(2L);
		p.setDisplayName("bar");
		map.put(p.getPrincipalId().toString(), p);
		return map;
	}
	
	@Test
	public void getAclEntriesTest() throws Exception {
		AccessControlList acl = createACL();
		Map<String,AclPrincipal> allPrincipals = createAllPrincipals();
		List<AclEntry> entries = AccessControlListEditor.getAclEntries(acl, allPrincipals);
		assertEquals(2, entries.size());
	}
	
	@Test
	public void newACLforEntityTest() throws Exception {
		String entityId = "syn101";
		Long creatorPrincipalId = 999L;
		AccessControlList acl = AccessControlListEditor.newACLforEntity(entityId, creatorPrincipalId);
		assertEquals(entityId, acl.getId());
		ResourceAccess ra = acl.getResourceAccess().iterator().next();
		assertEquals(creatorPrincipalId, ra.getPrincipalId());
		Set<ACCESS_TYPE> ats = ra.getAccessType();
		assertTrue(ats.contains(ACCESS_TYPE.CHANGE_PERMISSIONS));
		assertTrue(ats.contains(ACCESS_TYPE.CREATE));
		assertTrue(ats.contains(ACCESS_TYPE.DELETE));
		assertTrue(ats.contains(ACCESS_TYPE.READ));
		assertTrue(ats.contains(ACCESS_TYPE.UPDATE));
	}
	
	@Test
	public void addOwnerAdministrativeAccessTest() throws Exception {
		AccessControlList acl = new AccessControlList();
		Set<ResourceAccess> ras = new HashSet<ResourceAccess>();
		acl.setResourceAccess(ras);
		AccessControlListEditor.addOwnerAdministrativeAccess(acl, 1L);
		Set<ACCESS_TYPE> ats = acl.getResourceAccess().iterator().next().getAccessType();
		assertTrue(ats.contains(ACCESS_TYPE.CHANGE_PERMISSIONS));
		assertTrue(ats.contains(ACCESS_TYPE.CREATE));
		assertTrue(ats.contains(ACCESS_TYPE.DELETE));
		assertTrue(ats.contains(ACCESS_TYPE.READ));
		assertTrue(ats.contains(ACCESS_TYPE.UPDATE));
	}
	
	@Test
	public void findPrincipalTest() throws Exception {
		AccessControlList acl = createACL();
		ResourceAccess ra3 = AccessControlListEditor.findPrincipal(1L, acl);
		assertEquals((Long)1L, (Long)ra3.getPrincipalId());
	}
}
