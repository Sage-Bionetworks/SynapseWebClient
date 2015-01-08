package org.sagebionetworks.web.unitclient.mvp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.HomeRedirector;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.BulkPresenterProxy;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.presenter.PresenterProxy;
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

	PresenterProxy<HomePresenter, Home> mockAll;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	AppActivityMapper appActivityMapper;
	String historyToken = "Home:0";
	AuthenticationController mockAuthenticationController;
	BulkPresenterProxy mockBulkPresenterProxy;
	
	@Before
	public void before(){
		// Mock the views
		mockInjector = Mockito.mock(PortalGinInjector.class);
		// Controller
		mockBulkPresenterProxy = Mockito.mock(BulkPresenterProxy.class);
		mockSynapseJSNIUtils = Mockito.mock(SynapseJSNIUtils.class);
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		when(mockInjector.getBulkPresenterProxy()).thenReturn(mockBulkPresenterProxy);
		
		// WHENs
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockSynapseJSNIUtils.getCurrentHistoryToken()).thenReturn(historyToken);
		
		// Home
		mockAll = Mockito.mock(PresenterProxy.class);
		when(mockInjector.getHomePresenter()).thenReturn(mockAll);
		// Global App State
		mockGlobalApplicationState = Mockito.mock(GlobalApplicationState.class);
		when(mockInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
		
		when(mockInjector.getAuthenticationController()).thenReturn(mockAuthenticationController);
		
		appActivityMapper = new AppActivityMapper(mockInjector, mockSynapseJSNIUtils, null);
	}
	
	@Test
	public void testUnknown(){

		// This is the place we will pass in
		Home unknownPlace = new Home(""); 

		// Create the mapper
		Activity object = appActivityMapper.getActivity(unknownPlace);
		assertNotNull(object);
		
		// Validate that the place was set.
		verify(mockAll).setPlace((Home) anyObject());
		
		// validate that the place change was recorded
		verify(mockSynapseJSNIUtils, Mockito.times(1)).recordPageVisit(historyToken);
	}
	
	/*
	 * Regression Test for SWC-484
	 * 
	 * Have the currently stored place be a valid history place (like an Synapse entity page). 
	 * Part 1: Then go to the login page and verify that the entity place is stored.
	 * Part 2: Then go back to the login page (SSO simulation) and assure that last place is still the entity place and not the first login page visit
	 */
	@Test 
	public void testSetLastPlace() {
		// Part 1
		Place entityPlace = new Synapse("token");
		Place loginPlace1 = new LoginPlace("0");
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(entityPlace);		
		LoginPresenter mockLoginPresenter = mock(LoginPresenter.class);		
		when(mockInjector.getLoginPresenter()).thenReturn(mockLoginPresenter);
		
		appActivityMapper.getActivity(loginPlace1);
		verify(mockGlobalApplicationState).setLastPlace(entityPlace);
		verify(mockGlobalApplicationState).setCurrentPlace(loginPlace1);
		
		// Part 2
		reset(mockGlobalApplicationState);
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(loginPlace1);
		when(mockGlobalApplicationState.getLastPlace()).thenReturn(entityPlace);
		Place loginPlace2 = new LoginPlace("EXAMPLE_SSO_TOKEN");
		appActivityMapper.getActivity(loginPlace2);		
		verify(mockGlobalApplicationState, times(0)).setLastPlace(any(Place.class));
		verify(mockGlobalApplicationState).setCurrentPlace(loginPlace2);
	}

	@Test 
	public void testGetActivityHomeRedirectorNotLoggedIn() {
		Place homeRedirectorPlace = new HomeRedirector();
				
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(null);
		
		appActivityMapper.getActivity(homeRedirectorPlace);
		
		ArgumentCaptor<Place> currentPlaceCaptor = ArgumentCaptor.forClass(Place.class);
		verify(mockGlobalApplicationState).setCurrentPlace(currentPlaceCaptor.capture());
		
		//should be sent to Home in this case
		assertTrue(currentPlaceCaptor.getValue() instanceof Home);
	}
	
	@Test 
	public void testGetActivityHomeRedirectorLoggedIn() {
		Place homeRedirectorPlace = new HomeRedirector();
				
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		String userId = "8787878";
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(userId);
		
		appActivityMapper.getActivity(homeRedirectorPlace);
		
		ArgumentCaptor<Place> currentPlaceCaptor = ArgumentCaptor.forClass(Place.class);
		verify(mockGlobalApplicationState).setCurrentPlace(currentPlaceCaptor.capture());
		
		//should be sent to Profile in this case
		Place destination = currentPlaceCaptor.getValue();
		assertTrue(destination instanceof Profile);
		assertEquals(userId, ((Profile)destination).getUserId());
	}
}
