package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.presenter.PresenterProxy;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Test for the PresenterProxy
 * @author jmhill
 *
 */
public class PresenterProxyTest {

	Home mockPlace;
	HomePresenter mockPresenter;
	AsyncProvider<HomePresenter> mockProvider;
	PresenterProxy<HomePresenter, Home> presenterProxy;
	AcceptsOneWidget mockPanel;
	EventBus mockEventBus;
	GWTWrapper mockGWT;
	SynapseJSNIUtils mockJsniUtils;
	
	@Before
	public void before(){
		mockPlace = Mockito.mock(Home.class);
		mockPresenter = Mockito.mock(HomePresenter.class);
		mockProvider = Mockito.mock(AsyncProvider.class);
		mockPanel = Mockito.mock(AcceptsOneWidget.class);
		mockEventBus = Mockito.mock(EventBus.class);
		mockGWT = Mockito.mock(GWTWrapper.class);
		mockJsniUtils = Mockito.mock(SynapseJSNIUtils.class);
		presenterProxy = new PresenterProxy<HomePresenter, Home>(mockProvider, mockGWT, mockJsniUtils);

	}
	
	@Test
	public void testSuccess(){
		// Test success.
		AsyncMockStubber.callSuccessWith(mockPresenter).when(mockProvider).get(any(AsyncCallback.class));
		// First set the place
		Home place = new Home("token");
		presenterProxy.setPlace(place);
		// The place should not be sent to to the provider yet
		verify(mockPresenter, times(0)).setPlace(any(Home.class));
		// Now set the place on the proxy
		presenterProxy.start(mockPanel, mockEventBus);
		// Verify everything was passed along
		verify(mockPresenter, times(1)).setPlace(place);
		verify(mockPresenter, times(1)).start(mockPanel, mockEventBus);
	}
}
