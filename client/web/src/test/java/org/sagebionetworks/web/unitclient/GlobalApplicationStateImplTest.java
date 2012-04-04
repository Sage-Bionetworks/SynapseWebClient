package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.sagebionetworks.web.client.GlobalApplicationStateImpl;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.Synapse;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.PlaceController;

public class GlobalApplicationStateImplTest {
	
	CookieProvider mockCookieProvider;
	PlaceController mockPlaceController;
	ActivityMapper mockMapper;
	GlobalApplicationStateImpl state;
	
	@Before
	public void before(){
		mockCookieProvider = Mockito.mock(CookieProvider.class);
		mockPlaceController = Mockito.mock(PlaceController.class);
		mockMapper = Mockito.mock(ActivityMapper.class);
		state = new GlobalApplicationStateImpl(mockCookieProvider);
		state.setPlaceController(mockPlaceController);
		state.setActivityMapper(mockMapper);
	}
	/**
	 * 
	 */
	@Test
	public void testGoToNewPlace(){
		Synapse currenPlace = new Synapse("syn123");
		// Start with the current place 
		when(mockPlaceController.getWhere()).thenReturn(currenPlace);

		PlaceChanger changer = state.getPlaceChanger();
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
		PlaceChanger changer = state.getPlaceChanger();
		assertNotNull(changer);
		changer.goTo(currenPlace);
		// Since we are already there then just reload the page by calling getAtiviity.
		verify(mockMapper).getActivity(currenPlace);
		
	}

}
