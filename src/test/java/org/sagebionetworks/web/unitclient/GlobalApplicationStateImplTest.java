package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.sagebionetworks.web.client.GlobalApplicationStateImpl;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GlobalApplicationStateImplTest {
	
	CookieProvider mockCookieProvider;
	PlaceController mockPlaceController;
	ActivityMapper mockMapper;
	GlobalApplicationStateImpl globalApplicationState;
	
	@Before
	public void before(){
		mockCookieProvider = Mockito.mock(CookieProvider.class);
		mockPlaceController = Mockito.mock(PlaceController.class);
		mockMapper = Mockito.mock(ActivityMapper.class);
		globalApplicationState = new GlobalApplicationStateImpl(mockCookieProvider);
		globalApplicationState.setPlaceController(mockPlaceController);
		globalApplicationState.setActivityMapper(mockMapper);
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
		// Since we are already there then just reload the page by calling getAtiviity.
		verify(mockMapper).getActivity(currenPlace);
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCheckVersionCompatibility() {
		SynapseClientAsync mockSynapseClient = mock(SynapseClientAsync.class);
		SynapseView mockView = mock(SynapseView.class);

		AsyncMockStubber.callSuccessWith("v1").when(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		globalApplicationState.checkVersionCompatibility(mockSynapseClient, mockView);
		verify(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView, never()).showErrorMessage(anyString());
		
		// simulate change repo version
		reset(mockSynapseClient);
		AsyncMockStubber.callSuccessWith("v2").when(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		globalApplicationState.checkVersionCompatibility(mockSynapseClient, mockView);
		verify(mockSynapseClient).getSynapseVersions(any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
		
	}
	
}
