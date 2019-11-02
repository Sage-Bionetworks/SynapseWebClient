package org.sagebionetworks.web.unitclient.widget.googlemap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMap;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMapView;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class GoogleMapTest {
	GoogleMap map;
	@Mock
	GoogleMapView mockView;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	SynapseJavascriptClient mockJsClient;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		GoogleMap.isLoaded = true;
		map = new GoogleMap(mockView, mockSynapseJSNIUtils, mockJsClient, mockSynapseAlert, mockPortalGinInjector, mockLazyLoadHelper);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockView).setPresenter(map);

	}

	private void simulateInView() {
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLazyLoadHelper).configure(captor.capture(), eq(mockView));
		captor.getValue().invoke();
	}

	@Test
	public void testConfigureNotLoaded() throws RequestException {
		GoogleMap.isLoaded = false;
		map.configure();
		verify(mockLazyLoadHelper).setIsConfigured();
		verifyZeroInteractions(mockJsClient);
		simulateInView();
		// verify attempt to load
		verify(mockJsClient).doGetString(eq(GoogleMap.GOOGLE_MAP_URL), eq(true), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() throws RequestException {
		map.configure();

		String data = "map data";
		AsyncMockStubber.callSuccessWith(data).when(mockJsClient).doGetString(anyString(), eq(true), any(AsyncCallback.class));

		simulateInView();
		// verify attempt to load all data
		verify(mockView).showMap(data);
	}

	@Test
	public void testConfigureTeam() throws RequestException {
		String teamId = "1234987";
		map.configure(teamId);

		String data = "map data";
		AsyncMockStubber.callSuccessWith(data).when(mockJsClient).doGetString(anyString(), eq(true), any(AsyncCallback.class));

		simulateInView();
		// verify attempt to load team data
		verify(mockView).showMap(data);
	}

	@Test
	public void testFailedRequest() throws RequestException {
		Exception ex = new Exception("failure to load");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).doGetString(anyString(), eq(true), any(AsyncCallback.class));
		map.configure();
		simulateInView();
		verify(mockSynapseAlert).handleException(ex);
	}
}
