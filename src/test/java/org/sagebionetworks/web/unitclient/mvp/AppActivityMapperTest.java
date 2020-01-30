package org.sagebionetworks.web.unitclient.mvp;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.BulkPresenterProxy;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.Place;

/**
 * Test the activity mapper.
 * 
 * @author jmhill
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AppActivityMapperTest {
	@Mock
	PortalGinInjector mockInjector;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	AppActivityMapper appActivityMapper;
	String historyToken = "Home:0";
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	BulkPresenterProxy mockBulkPresenterProxy;
	@Mock
	CookieProvider mockCookies;
	@Mock
	LoginPresenter mockLoginPresenter;
	@Mock
	SynapseJavascriptClient mockJsClient;

	@Before
	public void before() {
		when(mockInjector.getBulkPresenterProxy()).thenReturn(mockBulkPresenterProxy);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockSynapseJSNIUtils.getCurrentHistoryToken()).thenReturn(historyToken);
		when(mockInjector.getCookieProvider()).thenReturn(mockCookies);
		when(mockInjector.getGlobalApplicationState()).thenReturn(mockGlobalApplicationState);
		when(mockInjector.getAuthenticationController()).thenReturn(mockAuthenticationController);
		when(mockInjector.getLoginPresenter()).thenReturn(mockLoginPresenter);
		when(mockInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);

		appActivityMapper = new AppActivityMapper(mockInjector, mockSynapseJSNIUtils, null);
	}

	@Test
	public void testUnknown() {

		// This is the place we will pass in
		Home unknownPlace = new Home("");

		// Create the mapper
		Activity object = appActivityMapper.getActivity(unknownPlace);
		assertNotNull(object);

		// Validate that the place was set.
		verify(mockBulkPresenterProxy).setPlace(unknownPlace);

		// validate that the place change was recorded
		verify(mockGlobalApplicationState).recordPlaceVisit(unknownPlace);
	}

	/*
	 * Regression Test for SWC-484
	 * 
	 * Have the currently stored place be a valid history place (like an Synapse entity page). Part 1:
	 * Then go to the login page and verify that the entity place is stored. Part 2: Then go back to the
	 * login page (SSO simulation) and assure that last place is still the entity place and not the
	 * first login page visit
	 */
	@Test
	public void testSetLastPlace() {
		// Part 1
		Place entityPlace = new Synapse("token");
		Place loginPlace1 = new LoginPlace("0");
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(entityPlace);

		appActivityMapper.getActivity(loginPlace1);
		verify(mockJsClient).cancelAllPendingRequests();
		verify(mockGlobalApplicationState).setLastPlace(entityPlace);

		// Part 2
		reset(mockGlobalApplicationState);
		when(mockGlobalApplicationState.getCurrentPlace()).thenReturn(loginPlace1);
		when(mockGlobalApplicationState.getLastPlace()).thenReturn(entityPlace);
		Place loginPlace2 = new LoginPlace("EXAMPLE_SSO_TOKEN");
		appActivityMapper.getActivity(loginPlace2);
		verify(mockGlobalApplicationState, times(0)).setLastPlace(any(Place.class));
	}
}
