package org.sagebionetworks.web.unitclient.widget.user;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.user.UserBadgeView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the Summary widget.
 * @author dburdick
 *
 */
public class UserBadgeTest {

	private static final String DOEBOY = "doeboy";
	private static final String DOE = "Doe";
	private static final String FIRST_NAME = "John";
	
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	UserBadgeView mockView;
	UserBadge userBadge;
	UserProfile profile;
	
	ClientCache mockCache;
	String principalId = "id1";
	int max=10;
	String displayName;
	
	@Before
	public void before(){
		profile = new UserProfile();
		profile.setFirstName(FIRST_NAME);
		profile.setLastName(DOE);
		profile.setUserName(DOEBOY);
		profile.setProfilePicureFileHandleId("1234");
		displayName = DisplayUtils.getDisplayName(profile);
		profile.setOwnerId(principalId);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockView = mock(UserBadgeView.class);
		mockCache = mock(ClientCache.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		userBadge = new UserBadge(mockView, mockSynapseClient, mockGlobalApplicationState, mockSynapseJSNIUtils, adapterFactory, mockCache);
	}
	
	@Test
	public void testConfigureStatic(){
		userBadge.configure(profile);
		verify(mockView).setDisplayName(displayName, displayName);
		ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockView).showCustomUserPicture(urlCaptor.capture());
		String url = urlCaptor.getValue();
		assertFalse(url.contains(WebConstants.NOCACHE_PARAM));
		
		//simulate a load error
		reset(mockView);
		userBadge.onImageLoadError();
		verify(mockView).showCustomUserPicture(urlCaptor.capture());
		url = urlCaptor.getValue();
		assertTrue(url.contains(WebConstants.NOCACHE_PARAM));
		
		//if it fails to load again, it should not attempt a reload 
		reset(mockView);
		userBadge.onImageLoadError();
		verify(mockView, never()).showCustomUserPicture(anyString());
	}
	
	@Test
	public void testConfigureAsync() throws Exception {
		AsyncMockStubber.callSuccessWith(profile).when(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		userBadge.setMaxNameLength(max);
		userBadge.configure(principalId);
		verify(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		verify(mockView).setDisplayName(eq(displayName), anyString());
	}
	
	@Test
	public void testConfigureAsyncFail() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));		
		profile.setDisplayName("name");
		userBadge.configure(principalId);
		verify(mockView).showLoadError(principalId);
	}
	
	@Test
	public void testConfigureNullPrincipalId() throws Exception {
		userBadge.configure((String)null);
		verify(mockView).showLoadError(anyString());
	}
	
	@Test
	public void testConfigureEmptyPrincipalId() throws Exception {
		userBadge.configure("");
		verify(mockView).showLoadError(anyString());
	}
	
	@Test
	public void testBadgeClicked() {
		userBadge.configure(profile);
		userBadge.badgeClicked(null);
		verify(mockPlaceChanger).goTo(any(Profile.class));
	}
	
	@Test
	public void testBadgeClickedCustomClickHandler() {
		userBadge.configure(profile);
		ClickHandler mockClickHandler = mock(ClickHandler.class);
		userBadge.setCustomClickHandler(mockClickHandler);
		userBadge.badgeClicked(null);
		verify(mockClickHandler).onClick(any(ClickEvent.class));
	}
	
	@Test
	public void testConfigureAsyncFailFromCache() throws Exception {
		AsyncMockStubber.callSuccessWith(profile).when(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		userBadge.setMaxNameLength(max);
		when(mockCache.get(anyString())).thenReturn("invalid user profile json");
		userBadge.configure(principalId);
		verify(mockCache).get(anyString());
		verify(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
		verify(mockView).setDisplayName(eq(displayName), anyString());
	}
	
	@Test
	public void testConfigureSuccessFromCache() throws Exception {
		userBadge.setMaxNameLength(max);
		JSONObjectAdapter adapter = adapterFactory.createNew();
		profile.writeToJSONObject(adapter);
		when(mockCache.get(anyString())).thenReturn(adapter.toJSONString());
		
		userBadge.configure(principalId);
		verify(mockCache).get(anyString());
		verify(mockSynapseClient, never()).getUserProfile(eq(principalId), any(AsyncCallback.class));
		verify(mockView).setDisplayName(eq(displayName), anyString());
	}
	
	@Test
	public void testGetDefaultPictureLetter() {
		//first name is "John"
		assertEquals("J", userBadge.getDefaultPictureLetter(profile));
		profile.setFirstName(null);
		//username is "doeboy"
		assertEquals("D", userBadge.getDefaultPictureLetter(profile));
		//null profile?
		assertEquals(UserBadge.DEFAULT_LETTER, userBadge.getDefaultPictureLetter(null));
	}
	
	@Test
	public void testGetDefaultPictureColor() {
		assertEquals(UserBadge.DEFAULT_COLOR, userBadge.getDefaultPictureColor(null));
		
		//verify no errors when trying to get the color for a range of values 
		//(like an array index out of bounds, or a null color response)
		for (int i = 0; i < 100; i++) {
			assertNotNull(userBadge.getColor(i));
		}
		
		//test negative hashcode
		assertNotNull(userBadge.getColor(-418745608));
	}
	
	@Test
	public void testConfigureFromUsernameAsync() throws Exception {
		AsyncMockStubber.callSuccessWith(profile).when(mockSynapseClient).getUserProfileFromUsername(eq(DOEBOY), any(AsyncCallback.class));
		userBadge.configureWithUsername(DOEBOY);
		verify(mockSynapseClient).getUserProfileFromUsername(eq(DOEBOY), any(AsyncCallback.class));
		verify(mockView).setDisplayName(eq(displayName), anyString());
	}
	
	@Test
	public void testConfigureFromUsernameAsyncFailure() throws Exception {
		String errorMessage = "not found";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getUserProfileFromUsername(eq(DOEBOY), any(AsyncCallback.class));
		userBadge.configureWithUsername(DOEBOY);
		verify(mockSynapseClient).getUserProfileFromUsername(eq(DOEBOY), any(AsyncCallback.class));
		verify(mockView).showLoadError(errorMessage);
	}
	
	@Test
	public void testConfigureFromUsernameFromCache() throws Exception {
		userBadge.setMaxNameLength(max);
		when(mockCache.get(anyString())).thenReturn(principalId, (String)null);
		
		userBadge.configureWithUsername(DOEBOY);
		verify(mockCache, times(2)).get(anyString());
		verify(mockSynapseClient, never()).getUserProfileFromUsername(eq(DOEBOY), any(AsyncCallback.class));
		verify(mockSynapseClient).getUserProfile(eq(principalId), any(AsyncCallback.class));
	}
}
