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
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.Widget;

public class GoogleMapTest {
	GoogleMap map;
	@Mock
	GoogleMapView mockView;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	RequestBuilderWrapper mockRequestBuilderWrapper;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		map = new GoogleMap(mockView, mockSynapseJSNIUtils, mockRequestBuilderWrapper, mockSynapseAlert, mockPortalGinInjector, mockLazyLoadHelper);
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
	public void testConfigure() throws RequestException {
		map.configure();
		verify(mockLazyLoadHelper).setIsConfigured();
		verifyZeroInteractions(mockRequestBuilderWrapper);
		simulateInView();
		//verify attempt to load script
		verify(mockRequestBuilderWrapper).configure(RequestBuilder.GET, GoogleMap.GOOGLE_MAP_URL);
		verify(mockRequestBuilderWrapper).sendRequest(anyString(), any(RequestCallback.class));
		reset(mockRequestBuilderWrapper);
	}
	
}
