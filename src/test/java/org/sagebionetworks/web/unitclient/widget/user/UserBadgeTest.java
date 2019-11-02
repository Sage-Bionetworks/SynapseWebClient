package org.sagebionetworks.web.unitclient.widget.user;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the Summary widget.
 * 
 * @author dburdick
 *
 */
public class UserBadgeTest {

	private static final String DOEBOY = "doeboy";
	private static final String DOE = "Doe";
	private static final String FIRST_NAME = "John";

	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	UserBadgeView mockView;
	UserBadge userBadge;
	UserProfile profile;
	@Mock
	ClientCache mockCache;
	String principalId = "id1", fileHandleId = "1234";
	int max = 10;
	String displayName;
	@Mock
	UserProfileAsyncHandler mockUserProfileAsyncHandler;
	public static final String PICTURE_URL = "http://url.to.profile.picture";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		profile = new UserProfile();
		profile.setFirstName(FIRST_NAME);
		profile.setLastName(DOE);
		profile.setUserName(DOEBOY);
		profile.setProfilePicureFileHandleId(fileHandleId);
		displayName = DisplayUtils.getDisplayName(profile);
		profile.setOwnerId(principalId);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		when(mockSynapseJSNIUtils.getFileHandleAssociationUrl(anyString(), any(FileHandleAssociateType.class), anyString())).thenReturn(PICTURE_URL);
		userBadge = new UserBadge(mockView, mockSynapseClient, mockGlobalApplicationState, mockSynapseJSNIUtils, mockCache, mockUserProfileAsyncHandler, adapterFactory);
	}

	@Test
	public void testConfigureAsync() throws Exception {
		AsyncMockStubber.callSuccessWith(profile).when(mockUserProfileAsyncHandler).getUserProfile(eq(principalId), any(AsyncCallback.class));
		userBadge.configure(principalId);

		verify(mockUserProfileAsyncHandler).getUserProfile(eq(principalId), any(AsyncCallback.class));
		verify(mockView).configure(profile);
	}

	@Test
	public void testConfigureAsyncFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockUserProfileAsyncHandler).getUserProfile(eq(principalId), any(AsyncCallback.class));
		profile.setDisplayName("name");
		userBadge.configure(principalId);
		verify(mockView).showLoadError(principalId);
	}

	@Test
	public void testConfigureNullPrincipalId() throws Exception {
		userBadge.configure((String) null);
		verify(mockView).showLoadError(anyString());
	}

	@Test
	public void testConfigureEmptyPrincipalId() throws Exception {
		userBadge.configure("");
		verify(mockView).showLoadError(anyString());
	}


	@Test
	public void testBadgeClickedNewWindowTrue() {
		userBadge.configure(profile);
		userBadge.setOpenInNewWindow();
		verify(mockView).setOpenInNewWindow();
	}

	@Test
	public void testBadgeClickedCustomClickHandler() {
		userBadge.configure(profile);
		ClickHandler mockClickHandler = mock(ClickHandler.class);
		userBadge.setCustomClickHandler(mockClickHandler);
		verify(mockView).setCustomClickHandler(mockClickHandler);
	}

	@Test
	public void testConfigureAsyncFailFromCache() throws Exception {
		AsyncMockStubber.callSuccessWith(profile).when(mockUserProfileAsyncHandler).getUserProfile(eq(principalId), any(AsyncCallback.class));
		when(mockCache.get(anyString())).thenReturn("invalid user profile json");
		userBadge.configure(principalId);
		verify(mockCache).get(anyString());
		verify(mockUserProfileAsyncHandler).getUserProfile(eq(principalId), any(AsyncCallback.class));
		verify(mockView).configure(profile);
	}

	@Test
	public void testConfigureSuccessFromCache() throws Exception {
		JSONObjectAdapter adapter = adapterFactory.createNew();
		profile.writeToJSONObject(adapter);
		when(mockCache.get(anyString())).thenReturn(adapter.toJSONString());

		userBadge.configure(principalId);
		verify(mockCache).get(anyString());
		verify(mockUserProfileAsyncHandler, never()).getUserProfile(eq(principalId), any(AsyncCallback.class));
		verify(mockView).configure(profile);
	}

}
