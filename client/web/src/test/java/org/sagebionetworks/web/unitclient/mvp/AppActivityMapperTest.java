package org.sagebionetworks.web.unitclient.mvp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.Place;

/**
 * Test the activity mapper.
 * 
 * @author jmhill
 *
 */
public class AppActivityMapperTest {
	
	PortalGinInjector mockInjector;
	AuthenticationController mockController;
	HomePresenter mockAll;
	GlobalApplicationState mockGlobalApplicationState;
	
	@Before
	public void before(){
		// Mock the views
		mockInjector = Mockito.mock(PortalGinInjector.class);
		// Controller
		mockController = Mockito.mock(AuthenticationController.class);		
		
		// WHENs
		when(mockController.isLoggedIn()).thenReturn(true);
		when(mockInjector.getAuthenticationController()).thenReturn(mockController);
		// Home
		mockAll = Mockito.mock(HomePresenter.class);
		when(mockInjector.getHomePresenter()).thenReturn(mockAll);
		// Global App State
		mockGlobalApplicationState = Mockito.mock(GlobalApplicationState.class);
		when(mockInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
		
	}
	
	

	
	
	@Test
	public void testUnknown(){

		// This is the place we will pass in
		Place unknownPlace = new Place(){}; 

		// Create the mapper
		AppActivityMapper mapper = new AppActivityMapper(mockInjector);

		Activity object = mapper.getActivity(unknownPlace);
		assertNotNull(object);
		assertTrue(object instanceof HomePresenter);
		// Validate that the place was set.
		verify(mockAll).setPlace((Home) anyObject());
	}

}
