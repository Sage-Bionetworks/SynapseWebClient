package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.CytoscapeView;
import org.sagebionetworks.web.client.widget.entity.renderer.CytoscapeWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class CytoscapeWidgetTest {

	CytoscapeWidget widget;

	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	@Mock
	CytoscapeView mockView;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	RequestBuilderWrapper mockRequestBuilder;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseJSNIUtils mockSynapseJsniUtils;
	@Mock
	Response mockResponse;
	private static final String CYTOSCAPE_JS_ENTITY_ID = "syn7777777";
	private static final String CYTOSCAPE_JS_JSON_TEST = "{\"contains\" : \"Valid Cytoscape JS json exported from Cytoscape to drive visualization\"}";
	private static final String CYTOSCAPE_JS_STYLE_ENTITY_ID = "syn88888888";

	@Before
	public void setup() throws RequestException {
		MockitoAnnotations.initMocks(this);
		widget = new CytoscapeWidget(mockView, mockAuthenticationController, mockRequestBuilder, mockSynAlert, mockSynapseJsniUtils);

		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		when(mockResponse.getText()).thenReturn(CYTOSCAPE_JS_JSON_TEST);
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse).when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
		verify(mockView).setPresenter(widget);
	}

	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SYNAPSE_ID_KEY, CYTOSCAPE_JS_ENTITY_ID);
		widget.configure(wikiKey, descriptor, null, null);

		verify(mockView).configure(CYTOSCAPE_JS_JSON_TEST, null, CytoscapeWidget.DEFAULT_HEIGHT);
		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), contains(CYTOSCAPE_JS_ENTITY_ID));
		verify(mockView).setGraphVisible(true);
	}

	@Test
	public void testConfigureWithStyle() {
		String height = "1234";
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SYNAPSE_ID_KEY, CYTOSCAPE_JS_ENTITY_ID);
		descriptor.put(WidgetConstants.STYLE_SYNAPSE_ID_KEY, CYTOSCAPE_JS_STYLE_ENTITY_ID);
		descriptor.put(WidgetConstants.HEIGHT_KEY, height);

		widget.configure(wikiKey, descriptor, null, null);

		verify(mockRequestBuilder, times(2)).configure(eq(RequestBuilder.GET), anyString());
		verify(mockView).configure(CYTOSCAPE_JS_JSON_TEST, CYTOSCAPE_JS_JSON_TEST, height);
		verify(mockView).setGraphVisible(true);
	}

	@Test
	public void testConfigureFailure() throws RequestException {
		Exception e = new Exception("Could not retrieve js file");
		RequestBuilderMockStubber.callOnError(null, e).when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.SYNAPSE_ID_KEY, CYTOSCAPE_JS_ENTITY_ID);

		widget.configure(wikiKey, descriptor, null, null);

		verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
		verify(mockSynAlert).handleException(e);
		verify(mockView, never()).setGraphVisible(true);
	}

}
