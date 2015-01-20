package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GlobalApplicationStateImpl;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GlobalApplicationStateImplTest {
	
	SynapseClientAsync mockSynapseClient;
	CookieProvider mockCookieProvider;
	PlaceController mockPlaceController;
	AuthenticationController mockAuthenticationController;
	ActivityMapper mockMapper;
	EventBus mockEventBus;
	GlobalApplicationStateImpl globalApplicationState;
	JiraURLHelper mockJiraURLHelper;
	
	@Before
	public void before(){
		mockCookieProvider = Mockito.mock(CookieProvider.class);
		mockPlaceController = Mockito.mock(PlaceController.class);
		mockMapper = Mockito.mock(ActivityMapper.class);
		mockEventBus = Mockito.mock(EventBus.class);
		mockJiraURLHelper = Mockito.mock(JiraURLHelper.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		AsyncMockStubber.callSuccessWith("v1").when(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		globalApplicationState = new GlobalApplicationStateImpl(mockCookieProvider,mockJiraURLHelper, mockEventBus, mockSynapseClient, mockAuthenticationController);
		globalApplicationState.setPlaceController(mockPlaceController);
		globalApplicationState.setActivityMapper(mockMapper);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
	}
	
	/**
	 * 
	 */
	@Test
	public void testGoToNewPlace(){
		Synapse currenPlace = new Synapse("syn123");
		// Start with the current place 
		when(mockPlaceController.getWhere()).thenReturn(currenPlace);

		PlaceChanger changer = globalApplicationState.getPlaceChanger();
		assertNotNull(changer);
		// 
		Synapse newPlace = new Synapse("syn456");
		when(mockMapper.getActivity(newPlace)).thenThrow(new RuntimeException("For this the controller.gotPlace should have been called"));
		changer.goTo(newPlace);
		// Since this is not the current place it should actaully go there.
		verify(mockPlaceController).goTo(newPlace);
		
	}
	
	@Test
	public void testGoToCurrent(){
		Synapse currenPlace = new Synapse("syn123");
		// Start with the current place 
		when(mockPlaceController.getWhere()).thenReturn(currenPlace);
		PlaceChanger changer = globalApplicationState.getPlaceChanger();
		assertNotNull(changer);
		changer.goTo(currenPlace);
		// Since we are already there then just reload the page by firing an event
		verify(mockEventBus).fireEvent(any(PlaceChangeEvent.class));
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCheckVersionCompatibility() {
		SynapseView mockView = mock(SynapseView.class);
		globalApplicationState.checkVersionCompatibility(mockView);
		verify(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView, never()).showErrorMessage(anyString());
		
		// simulate change repo version
		reset(mockSynapseClient);
		AsyncMockStubber.callSuccessWith("v2").when(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		globalApplicationState.checkVersionCompatibility(mockView);
		verify(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
		
	}
	
	@Test
	public void testInitSynapseProperties() {
		HashMap<String, String> testProps = new HashMap<String, String>();
		String key = "k1";
		String value = "v1";
		testProps.put(key, value);
		AsyncMockStubber.callSuccessWith(testProps).when(mockSynapseClient).getSynapseProperties(any(AsyncCallback.class));
		Callback mockCallback = mock(Callback.class);
		globalApplicationState.initSynapseProperties(mockCallback);
		
		verify(mockSynapseClient).getSynapseProperties(any(AsyncCallback.class));
		assertEquals(value, globalApplicationState.getSynapseProperty(key));
		assertNull(globalApplicationState.getSynapseProperty("foo"));
		verify(mockCallback).invoke();
	}
	
	
	@Test 
	public void testGetHomePlaceNotLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(null);
		Place result = globalApplicationState.getHomePlace();
		
		//should be sent to Home in this case
		assertTrue(result instanceof Home);
	}
	
	@Test 
	public void testGetHomePlaceLoggedIn() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		String userId = "8787878";
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		
		Place result = globalApplicationState.getHomePlace();
		
		//should be sent to Profile in this case
		assertTrue(result instanceof Profile);
		assertEquals(userId, ((Profile)result).getUserId());
	}
	
}
