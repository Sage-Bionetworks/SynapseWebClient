package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.JSONEntityFactory;
import org.sagebionetworks.web.client.transform.JSONEntityFactoryImpl;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.transform.NodeModelCreatorImpl;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditorView;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.users.AclPrincipal;
import org.sagebionetworks.web.shared.users.AclUtils;
import org.sagebionetworks.web.shared.users.PermissionLevel;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccessControlListEditorTest {
	private static final long PRINCIPAL_ID = 1L;
	private static final long PRINCIPAL_ID2 = 2L;
	private static final String ACL_ID = "syn101";
	private static JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	private static AdapterFactory adapterFactory = new AdapterFactoryImpl(); // alt: GwtAdapterFactory
	private static JSONEntityFactory jsonEntityFactory = new JSONEntityFactoryImpl(adapterFactory);
	private static NodeModelCreator nodeModelCreator = new NodeModelCreatorImpl(jsonEntityFactory, jsonObjectAdapter);
	private SynapseClientAsync mockSynapseClient;
	private NodeModelCreator mockNodeModelCreator;
	private static org.sagebionetworks.web.shared.PaginatedResults<JSONEntity> pgGpsJson;
	private static UserEntityPermissions permissions;
	
	@Before
	public void setup() {
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		pgGpsJson = createUserGroupsJSON();
	}
	
	private AccessControlListEditor createACLE() {
		AccessControlListEditorView view = mock(AccessControlListEditorView.class); 
		AccessControlListEditor acle = new AccessControlListEditor(view,
				mockSynapseClient,
				nodeModelCreator,
				null,
				null, 
				jsonObjectAdapter);
		return acle;
	}
	
	private AccessControlListEditor createACLEWithMocks() {
		AccessControlListEditorView view = mock(AccessControlListEditorView.class); 
		AccessControlListEditor acle = new AccessControlListEditor(view,
				mockSynapseClient,
				mockNodeModelCreator,
				null,
				null, 
				jsonObjectAdapter);
		return acle;
	}
	
	private static EntityBundleTransport createEBT() {
		try {
			EntityBundleTransport ebt = new EntityBundleTransport();
			// fill in ACL
			AccessControlList acl = createACL();
			ebt.setAclJson(EntityFactory.createJSONStringForEntity(acl));
			// fill in groups
			List<UserGroup> gps = createUserGroups();
			PaginatedResults<UserGroup> pgGps = new PaginatedResults<UserGroup>();
			pgGps .setResults(gps);
			ebt.setGroupsJson(EntityFactory.createJSONStringForEntity(pgGps));
			// fill in UserEntityPermissions
			permissions = new UserEntityPermissions();
			permissions.setCanChangePermissions(true);
			permissions.setCanEnableInheritance(true);
			permissions.setOwnerPrincipalId(PRINCIPAL_ID);
			ebt.setPermissionsJson(EntityFactory.createJSONStringForEntity(permissions));
			return ebt;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void createAclTest() throws Exception {
		final EntityBundleTransport ebt = createEBT();
		AccessControlList acl = AccessControlListEditor.newACLforEntity(ACL_ID, PRINCIPAL_ID);
		EntityWrapper expectedEntityWrapper = new EntityWrapper(acl.writeToJSONObject(adapterFactory.createNew()).toJSONString(), AccessControlList.class.getName(), null);
		when(mockNodeModelCreator.createEntity(any(EntityWrapper.class), eq(AccessControlList.class))).thenReturn(acl);
		
		AsyncMockStubber.callSuccessWith(ebt).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(mockSynapseClient).createAcl(any(EntityWrapper.class), any(AsyncCallback.class));		
				
		AccessControlListEditor acle = createACLE();
		acle.setResource(ACL_ID);
		acle.asWidget();
		acle.createAcl();
		
		verify(mockSynapseClient).createAcl(eq(expectedEntityWrapper), any(AsyncCallback.class));
	}
	
	@Ignore
	@Test
	public void addAccessTest() throws Exception {
		final EntityBundleTransport ebt = createEBT();
		AccessControlList acl = AccessControlListEditor.newACLforEntity(ACL_ID, PRINCIPAL_ID);
		EntityWrapper expectedEntityWrapper = new EntityWrapper(acl.writeToJSONObject(adapterFactory.createNew()).toJSONString(), AccessControlList.class.getName(), null);
		when(mockNodeModelCreator.createEntity(anyString(), eq(AccessControlList.class))).thenReturn(acl);
		when(mockNodeModelCreator.createEntity(anyString(), eq(UserEntityPermissions.class))).thenReturn(permissions);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), eq(UserGroup.class))).thenReturn(pgGpsJson);
		
		AsyncMockStubber.callSuccessWith(ebt).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(mockSynapseClient).createAcl(any(EntityWrapper.class), any(AsyncCallback.class));		
				
		AccessControlListEditor acle = createACLEWithMocks();
		acle.setResource(ACL_ID);
		acle.asWidget();
		acle.addAccess(PRINCIPAL_ID2, PermissionLevel.CAN_EDIT);
		
		boolean foundIt=false;
		for (ResourceAccess ra : acl.getResourceAccess()) {
			if (PRINCIPAL_ID2 == ra.getPrincipalId()) {
				foundIt=true;
				Set<ACCESS_TYPE> ats = ra.getAccessType();
				assertEquals(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_EDIT), ats);
			}
		}
		assertTrue(foundIt);
	}
	
	@Test
	public void changeAccessTest() throws Exception {
		final EntityBundleTransport ebt = createEBT();
		// make a synapseClient that can return the ACL, users, groups, and permissions
		// and capture the ACL it updates
		SynapseClientAsync synapseClient = (SynapseClientAsync)Proxy.newProxyInstance(SynapseClientAsync.class.getClassLoader(),
                new Class<?>[]{SynapseClientAsync.class},
                new InvocationHandler() {
					@Override
					public Object invoke(Object synapseClient, Method method, Object[] args)
							throws Throwable {
						if (method.equals(SynapseClientAsync.class.getMethod("getEntityBundle", String.class, Integer.TYPE, AsyncCallback.class))) {
							((AsyncCallback<EntityBundleTransport>)args[2]).onSuccess(ebt);
						} else if (method.equals(SynapseClientAsync.class.getMethod("updateAcl", EntityWrapper.class, AsyncCallback.class))) {
							EntityWrapper ew = (EntityWrapper)args[0];
							AccessControlList acl = jsonEntityFactory.createEntity(ew.getEntityJson(), AccessControlList.class);
							assertEquals(ACL_ID, acl.getId());
							// check that it has admin access for owner
							boolean foundIt=false;
							for (ResourceAccess ra : acl.getResourceAccess()) {
								if (1L == ra.getPrincipalId()) {
									foundIt=true;
									Set<ACCESS_TYPE> ats = ra.getAccessType();
									assertEquals(AclUtils.getACCESS_TYPEs(PermissionLevel.CAN_EDIT), ats);
								}
							}
							assertTrue(foundIt);

						} else {
							throw new IllegalArgumentException(method.getName());
						}
						return null;
					}
		});
		AccessControlListEditor acle = createACLE();
		Long principalId = 1L;
		PermissionLevel level = PermissionLevel.CAN_EDIT;
		acle.setResource(ACL_ID);
		acle.asWidget();
		acle.changeAccess(principalId, level);
	}
	
	@Test
	public void removeAccessTest() throws Exception {
		final EntityBundleTransport ebt = createEBT();
		// make a synapseClient that can return the ACL, users, groups, and permissions
		// and capture the ACL it updates
		SynapseClientAsync synapseClient = (SynapseClientAsync)Proxy.newProxyInstance(SynapseClientAsync.class.getClassLoader(),
                new Class<?>[]{SynapseClientAsync.class},
                new InvocationHandler() {
					@Override
					public Object invoke(Object synapseClient, Method method, Object[] args)
							throws Throwable {
						if (method.equals(SynapseClientAsync.class.getMethod("getEntityBundle", String.class, Integer.TYPE, AsyncCallback.class))) {
							((AsyncCallback<EntityBundleTransport>)args[2]).onSuccess(ebt);
						} else if (method.equals(SynapseClientAsync.class.getMethod("updateAcl", EntityWrapper.class, AsyncCallback.class))) {
							EntityWrapper ew = (EntityWrapper)args[0];
							AccessControlList acl = jsonEntityFactory.createEntity(ew.getEntityJson(), AccessControlList.class);
							assertEquals(ACL_ID, acl.getId());
							// check that it has admin access for owner
							boolean foundIt=false;
							for (ResourceAccess ra : acl.getResourceAccess()) {
								if (1L == ra.getPrincipalId()) {
									foundIt=true;
								}
							}
							assertFalse(foundIt); // access has been removed!
						} else {
							throw new IllegalArgumentException(method.getName());
						}
						return null;
					}
		});
		AccessControlListEditor acle = createACLE();
		Long principalId = 1L;
		acle.setResource(ACL_ID);
		acle.asWidget();
		acle.removeAccess(principalId);
	}
	
	@Test
	@Ignore
	public void deleteAclTest() throws Exception {
		final EntityBundleTransport ebt = createEBT();
		AccessControlList acl = AccessControlListEditor.newACLforEntity(ACL_ID, PRINCIPAL_ID);
		EntityWrapper expectedEntityWrapper = new EntityWrapper(acl.writeToJSONObject(adapterFactory.createNew()).toJSONString(), AccessControlList.class.getName(), null);
		AsyncMockStubber.callSuccessWith(ebt).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));		
		AsyncMockStubber.callSuccessWith(expectedEntityWrapper).when(mockSynapseClient).createAcl(any(EntityWrapper.class), any(AsyncCallback.class));		
			
		when(mockNodeModelCreator.createEntity(anyString(), eq(AccessControlList.class))).thenReturn(acl);
		when(mockNodeModelCreator.createEntity(anyString(), eq(UserEntityPermissions.class))).thenReturn(permissions);
		when(mockNodeModelCreator.createPaginatedResults(anyString(), eq(UserGroup.class))).thenReturn(pgGpsJson);
		
		AccessControlListEditor acle = createACLEWithMocks();
		acle.setResource(ACL_ID);
		acle.asWidget();
		acle.deleteAcl();
		
		verify(mockSynapseClient).deleteAcl(eq(ACL_ID), Matchers.<AsyncCallback<EntityWrapper>>any());
	}
	
	// tests of utility functions
	
	
	@Test
	public void isInheritedTest() throws Exception {
		AccessControlList acl = createACL();
		assertFalse(AccessControlListEditor.isInherited(acl, ACL_ID));
		assertTrue(AccessControlListEditor.isInherited(acl, /*entityId*/"syn999"));
		}
	
	private static List<UserGroup> createUserGroups() {
		List<UserGroup> groups = new ArrayList<UserGroup>();
		UserGroup g = new UserGroup();
		g.setId("2");
		g.setName("bar");
		groups.add(g);
		return groups;
	}
	
	private static org.sagebionetworks.web.shared.PaginatedResults<JSONEntity> createUserGroupsJSON() {
		List<JSONEntity> groups = new ArrayList<JSONEntity>();
		UserGroup g = new UserGroup();
		g.setId("2");
		g.setName("bar");
		groups.add(g);
		org.sagebionetworks.web.shared.PaginatedResults<JSONEntity> pr = new org.sagebionetworks.web.shared.PaginatedResults<JSONEntity>();
		pr.setResults(groups);
		return pr;
	}

	private static AccessControlList createACL() {
		AccessControlList acl = new AccessControlList();
		acl.setId(ACL_ID);
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
	public void newACLforEntityTest() throws Exception {
		String entityId = ACL_ID;
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
