package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadgeView;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class PublicPrivateBadgeTest {
	
	private static final Long TEST_PUBLIC_PRINCIPAL_ID = 789l;
	private static final Long TEST_AUTHENTICATED_PRINCIPAL_ID = 123l;
	private static final Long TEST_ANONYMOUS_PRINCIPAL_ID = 422l;

	PublicPrivateBadge publicPrivateBadge;
	PublicPrivateBadgeView mockView;
	AuthenticationController mockAuthenticationController;
	UserAccountServiceAsync mockUserService;
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;	
	JSONObjectAdapter adapter = new JSONObjectAdapterImpl();
	AccessControlList acl;
	PublicPrincipalIds publicPrincipalIds;
	Set<ResourceAccess> resourceAccessSet = new HashSet<ResourceAccess>();
	Entity testEntity;
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockView = mock(PublicPrivateBadgeView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockUserService = mock(UserAccountServiceAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		
		publicPrivateBadge = new PublicPrivateBadge(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, mockUserService);
		acl = new AccessControlList();
		acl.setResourceAccess(resourceAccessSet);
		testEntity = new FileEntity();
		testEntity.setId("syn12345");
		publicPrincipalIds=new PublicPrincipalIds(TEST_PUBLIC_PRINCIPAL_ID, TEST_AUTHENTICATED_PRINCIPAL_ID, TEST_ANONYMOUS_PRINCIPAL_ID);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		EntityBundle transport = new EntityBundle();
		transport.setBenefactorAcl(acl);
		AsyncMockStubber.callSuccessWith(transport).when(mockSynapseClient).getEntityBundle(anyString(),  anyInt(),  any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(publicPrincipalIds).when(mockUserService).getPublicAndAuthenticatedGroupPrincipalIds(any(AsyncCallback.class));
		DisplayUtils.publicPrincipalIds = null;
	}
	
	@Test
	public void testConfigure() {
		publicPrivateBadge.configure(testEntity);
		verify(mockView).configure(anyBoolean());
	}
	
	@Test
	public void testGetACLFailure() {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockSynapseClient).getEntityBundle(anyString(),  anyInt(),  any(AsyncCallback.class));
		publicPrivateBadge.configure(testEntity);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testGetPublicPrincipalIdsFailure() {
		AsyncMockStubber.callFailureWith(new IllegalArgumentException()).when(mockUserService).getPublicAndAuthenticatedGroupPrincipalIds(any(AsyncCallback.class));
		publicPrivateBadge.configure(testEntity);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testIsPublic() {
		//start with an empty acl, verify is not public
		assertFalse(PublicPrivateBadge.isPublic(acl, publicPrincipalIds));
		
		//add a resourceaccess that is not a public group, and verify it's still not considered public
		ResourceAccess ra = new ResourceAccess();
		ra.setPrincipalId(9l);
		resourceAccessSet.add(ra);
		assertFalse(PublicPrivateBadge.isPublic(acl, publicPrincipalIds));

		//now add a resourceaccess that is considered a public group, and verify it now reports true
		ra = new ResourceAccess();
		ra.setPrincipalId(TEST_AUTHENTICATED_PRINCIPAL_ID);
		resourceAccessSet.add(ra);
		assertTrue(PublicPrivateBadge.isPublic(acl, publicPrincipalIds));

		//add both public groups and verify public
		ra = new ResourceAccess();
		ra.setPrincipalId(TEST_PUBLIC_PRINCIPAL_ID);
		resourceAccessSet.add(ra);
		assertTrue(PublicPrivateBadge.isPublic(acl, publicPrincipalIds));
		
		ra = new ResourceAccess();
		ra.setPrincipalId(TEST_ANONYMOUS_PRINCIPAL_ID);
		resourceAccessSet.add(ra);
		assertTrue(PublicPrivateBadge.isPublic(acl, publicPrincipalIds));
		
		//add only the other public group, and verify public
		resourceAccessSet.clear();
		ra = new ResourceAccess();
		ra.setPrincipalId(TEST_PUBLIC_PRINCIPAL_ID);
		resourceAccessSet.add(ra);
		assertTrue(PublicPrivateBadge.isPublic(acl, publicPrincipalIds));
		
		//add only the other public group, and verify public
		resourceAccessSet.clear();
		ra = new ResourceAccess();
		ra.setPrincipalId(TEST_ANONYMOUS_PRINCIPAL_ID);
		resourceAccessSet.add(ra);
		assertTrue(PublicPrivateBadge.isPublic(acl, publicPrincipalIds));

	}
	
	
	
}