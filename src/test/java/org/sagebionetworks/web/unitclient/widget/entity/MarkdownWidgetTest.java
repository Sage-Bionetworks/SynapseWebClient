package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.ElementWrapper;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class MarkdownWidgetTest {

	MarkdownWidget presenter;
	CookieProvider mockCookies;
	PortalGinInjector mockInjector;
	GWTWrapper mockGwt;
	SynapseClientAsync mockSynapseClient;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	MarkdownWidgetView mockView;
	WidgetRegistrar mockWidgetRegistrar;
	ResourceLoader mockResourceLoader;
	SynapseAlert mockSynAlert;
	WikiPageKey mockWikiPageKey;
	WikiPage mockWikiPage;
	ElementWrapper mockElementWrapper;
	WidgetRendererPresenter mockWidgetRendererPresenter;
	
	String testMarkdown = "markdown";
	String elementContentType = "image";
	Exception caught = new Exception("test");
	
	@Before
	public void setup() {
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		mockWidgetRendererPresenter = mock(WidgetRendererPresenter.class);
		when(mockWidgetRegistrar.getWidgetRendererForWidgetDescriptor(any(WikiPageKey.class), anyString(), anyMap(), any(Callback.class), any(Long.class))).thenReturn(mockWidgetRendererPresenter);
		mockView = mock(MarkdownWidgetView.class);
		mockGwt = mock(GWTWrapper.class);
		mockCookies = mock(CookieProvider.class);
		mockInjector = mock(PortalGinInjector.class);
		mockSynAlert = mock(SynapseAlert.class);
		mockResourceLoader = mock(ResourceLoader.class);
		mockWikiPageKey = mock(WikiPageKey.class);
		mockWikiPage = mock(WikiPage.class);
		when(mockWikiPage.getMarkdown()).thenReturn(testMarkdown);
		mockElementWrapper = mock(ElementWrapper.class);
		//the mockElement to be rendered will be an image
		when(mockElementWrapper.getAttribute("widgetParams")).thenReturn(elementContentType);
		presenter = new MarkdownWidget(mockSynapseClient, mockSynapseJSNIUtils, mockWidgetRegistrar, mockCookies, mockResourceLoader, mockGwt, mockInjector, mockView, mockSynAlert);
	}
	
	@Test
	public void testConfigureSuccess() {
		boolean isPreview = true;
		String sampleHTML = "<h1>heading</h1><p>foo baz bar</p>";
		
		AsyncMockStubber.callSuccessWith(sampleHTML).when(mockSynapseClient).markdown2Html(anyString(), anyBoolean(), anyBoolean(), anyString(), any(AsyncCallback.class));
		//only the first getElementById called by each getElementById finds its target so it doesn't look forever but still can be verified
		when(mockView.getElementById(WidgetConstants.MARKDOWN_TABLE_ID_PREFIX + "0")).thenReturn(mockElementWrapper);
		when(mockView.getElementById(WidgetConstants.DIV_ID_MATHJAX_PREFIX + "0" + "-preview")).thenReturn(mockElementWrapper);
		when(mockView.getElementById(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0" + "-preview")).thenReturn(mockElementWrapper);
		when(mockResourceLoader.isLoaded(any(WebResource.class))).thenReturn(true);
		presenter.configure(testMarkdown, mockWikiPageKey, isPreview, null);
		
		verify(mockSynapseClient).markdown2Html(anyString(), Mockito.eq(isPreview), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockView).setMarkdown(sampleHTML);
		
		// Called three times between tablesorter, loadMath, and loadWidgets, 
		// then another three to determine null
		verify(mockView, Mockito.times(6)).getElementById(anyString());
		
		//verify tablesorter applied
		verify(mockSynapseJSNIUtils).tablesorter(anyString());
		
		//verify loadMath
		verify(mockSynapseJSNIUtils).processWithMathJax(mockElementWrapper.getElement());
		
		//verify loadWidgets
		verify(mockWidgetRegistrar).getWidgetContentType(elementContentType);
		verify(mockWidgetRegistrar).getWidgetDescriptor(elementContentType);
		verify(mockWidgetRegistrar).getWidgetRendererForWidgetDescriptor(Mockito.eq(mockWikiPageKey), anyString(), anyMap(), any(Callback.class), any(Long.class));
		verify(mockView).addWidget(any(Widget.class), Mockito.eq(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0" + "-preview"));
	}
	
	@Test
	public void testConfigureFailure() {
		boolean isPreview = true;
		
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).markdown2Html(anyString(), anyBoolean(), anyBoolean(), anyString(), any(AsyncCallback.class));
		presenter.configure(testMarkdown, mockWikiPageKey, isPreview, null);
		
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testLoadMarkdownFromWikiPageSuccess() {
		boolean isPreview = true;
		String sampleHTML = "<h1>heading</h1><p>foo baz bar</p>";
		AsyncMockStubber.callSuccessWith(mockWikiPage).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(sampleHTML).when(mockSynapseClient).markdown2Html(anyString(), anyBoolean(), anyBoolean(), anyString(), any(AsyncCallback.class));
		//only the first getElementById called by each getElementById finds its target so it doesn't look forever but still can be verified
		when(mockView.getElementById(WidgetConstants.MARKDOWN_TABLE_ID_PREFIX + "0")).thenReturn(mockElementWrapper);
		when(mockView.getElementById(WidgetConstants.DIV_ID_MATHJAX_PREFIX + "0" + "-preview")).thenReturn(mockElementWrapper);
		when(mockView.getElementById(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0" + "-preview")).thenReturn(mockElementWrapper);
		when(mockResourceLoader.isLoaded(any(WebResource.class))).thenReturn(true);
		
		presenter.loadMarkdownFromWikiPage(mockWikiPageKey, isPreview, true);
		verify(mockWikiPageKey).setWikiPageId(anyString());
		
		verify(mockSynapseClient).markdown2Html(anyString(), Mockito.eq(isPreview), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockView, Mockito.never()).setEmptyVisible(anyBoolean());
		verify(mockView).setMarkdown(sampleHTML);
		// Called three times between tablesorter, loadMath, and loadWidgets, 
		// then another three to determine null
		verify(mockView, Mockito.times(6)).getElementById(anyString());
		
		//verify tablesorter applied
		verify(mockSynapseJSNIUtils).tablesorter(anyString());
		
		//verify loadMath
		verify(mockSynapseJSNIUtils).processWithMathJax(mockElementWrapper.getElement());
		
		//verify loadWidgets
		verify(mockWidgetRegistrar).getWidgetContentType(elementContentType);
		verify(mockWidgetRegistrar).getWidgetDescriptor(elementContentType);
		verify(mockWidgetRegistrar).getWidgetRendererForWidgetDescriptor(any(WikiPageKey.class), anyString(), anyMap(), any(Callback.class), any(Long.class));
		verify(mockView).addWidget(any(Widget.class), Mockito.eq(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0" + "-preview"));
	}
	
	@Test
	public void testLoadMarkdownFromWikiPageFailure() {
		boolean isPreview = true;
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.loadMarkdownFromWikiPage(mockWikiPageKey, isPreview, false);
		verify(mockSynAlert).showError(anyString());
	}
}
