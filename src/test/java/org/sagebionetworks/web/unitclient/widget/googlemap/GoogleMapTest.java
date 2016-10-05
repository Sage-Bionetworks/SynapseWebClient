package org.sagebionetworks.web.unitclient.widget.googlemap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMap;
import org.sagebionetworks.web.client.widget.googlemap.GoogleMapView;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Widget;

public class GoogleMapTest {
	GoogleMap map;
	@Mock
	GoogleMapView mockView;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	RequestBuilderWrapper mockRequestBuilder;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	Response mockResponse;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		GoogleMap.isLoaded = true;
		map = new GoogleMap(mockView, mockSynapseJSNIUtils, mockRequestBuilder, mockSynapseAlert, mockPortalGinInjector, mockLazyLoadHelper);
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
		verifyZeroInteractions(mockRequestBuilder);
		simulateInView();
		//verify attempt to load
		verify(mockRequestBuilder).configure(RequestBuilder.GET, GoogleMap.GOOGLE_MAP_URL);
		verify(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
	}
	
	@Test
	public void testConfigure() throws RequestException {
		map.configure();
		
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String data = "map data";
		when(mockResponse.getText()).thenReturn(data);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
			.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));

		simulateInView();
		//verify attempt to load all data
		verify(mockRequestBuilder).configure(RequestBuilder.GET, GoogleMap.ALL_POINTS_URL);
		verify(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		verify(mockView).showMap(data);
	}
	
	@Test
	public void testConfigureTeam() throws RequestException {
		String teamId = "1234987";
		map.configure(teamId);
		
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		String data = "map data";
		when(mockResponse.getText()).thenReturn(data);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse)
			.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));

		simulateInView();
		//verify attempt to load team data
		String expectedTeamMapDataUrl = GoogleMap.S3_PREFIX + teamId + ".json";
		verify(mockRequestBuilder).configure(RequestBuilder.GET, expectedTeamMapDataUrl);
		verify(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		verify(mockView).showMap(data);
	}
	
	@Test
	public void testFailedRequest() throws RequestException {
		Exception ex = new Exception("failure to load");
		RequestBuilderMockStubber.callOnError(null, ex)
			.when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		map.configure();
		simulateInView();
		verify(mockSynapseAlert).handleException(ex);
	}
}
