package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.MarkdownIt;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.SessionStorage;
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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class MarkdownWidgetTest {

	MarkdownWidget presenter;
	CookieProvider mockCookies;
	PortalGinInjector mockInjector;
	GWTWrapper mockGwt;
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
	@Mock
	SessionStorage mockSessionStorage;
	@Mock
	MarkdownIt mockMarkdownIt;
	@Mock
	RuntimeException mockJsException;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
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
		when(mockElementWrapper.getAttribute("data-widgetParams")).thenReturn(elementContentType);
		presenter = new MarkdownWidget(mockSynapseJavascriptClient, mockSynapseJSNIUtils, mockWidgetRegistrar, mockCookies, mockResourceLoader, mockGwt, mockInjector, mockView, mockSynAlert, mockMarkdownIt);
	}
	
	@Test
	public void testConfigureSuccess() {
		String sampleHTML = "<h1>heading</h1><p>foo baz bar</p>";
		
		when(mockMarkdownIt.markdown2Html(anyString(), anyString())).thenReturn(sampleHTML);
		//only the first getElementById called by each getElementById finds its target so it doesn't look forever but still can be verified
		when(mockView.getElementById(WidgetConstants.MARKDOWN_TABLE_ID_PREFIX + "0")).thenReturn(mockElementWrapper);
		when(mockView.getElementById(Mockito.contains(WidgetConstants.DIV_ID_MATHJAX_PREFIX + "0"))).thenReturn(mockElementWrapper);
		when(mockView.getElementById(Mockito.contains(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0"))).thenReturn(mockElementWrapper);
		when(mockResourceLoader.isLoaded(any(WebResource.class))).thenReturn(true);
		presenter.configure(testMarkdown, mockWikiPageKey, null);
		ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).callbackWhenAttached(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		verify(mockMarkdownIt).markdown2Html(anyString(),anyString());
		verify(mockView).setMarkdown(sampleHTML);
		
		// Called by loadMath and loadWidgets, 
		verify(mockView, Mockito.times(4)).getElementById(anyString());
		
		//verify tablesorter applied
		verify(mockSynapseJSNIUtils).loadTableSorters();
		
		//verify summary/details tag shim run
		verify(mockSynapseJSNIUtils).loadSummaryDetailsShim();
		
		//verify loadMath
		verify(mockSynapseJSNIUtils).processWithMathJax(mockElementWrapper.getElement());
		
		//verify highlight code blocks applied
		verify(mockSynapseJSNIUtils).highlightCodeBlocks();
				
		//verify loadWidgets
		verify(mockWidgetRegistrar).getWidgetContentType(elementContentType);
		verify(mockWidgetRegistrar).getWidgetDescriptor(elementContentType);
		verify(mockWidgetRegistrar).getWidgetRendererForWidgetDescriptor(Mockito.eq(mockWikiPageKey), anyString(), anyMap(), any(Callback.class), any(Long.class));
		verify(mockView).addWidget(any(Widget.class), Mockito.contains(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0"));
		//removes text inserted by markdown processor (usually "<Synapse widget>" text node, but is username in the case of @username mentions). 
		verify(mockElementWrapper).removeAllChildren();
	}
	
	@Test
	public void testLoadMarkdownFromWikiPageSuccess() {
		String sampleHTML = "<h1>heading</h1><p>foo baz bar</p>";
		AsyncMockStubber.callSuccessWith(mockWikiPage).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		when(mockMarkdownIt.markdown2Html(anyString(), anyString())).thenReturn(sampleHTML);
		//only the first getElementById called by each getElementById finds its target so it doesn't look forever but still can be verified
		when(mockView.getElementById(WidgetConstants.MARKDOWN_TABLE_ID_PREFIX + "0")).thenReturn(mockElementWrapper);
		when(mockView.getElementById(Mockito.contains(WidgetConstants.DIV_ID_MATHJAX_PREFIX + "0"))).thenReturn(mockElementWrapper);
		when(mockView.getElementById(Mockito.contains(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0"))).thenReturn(mockElementWrapper);
		when(mockResourceLoader.isLoaded(any(WebResource.class))).thenReturn(true);
		
		presenter.loadMarkdownFromWikiPage(mockWikiPageKey, true);
		ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).callbackWhenAttached(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		verify(mockWikiPageKey).setWikiPageId(anyString());
		
		verify(mockMarkdownIt).markdown2Html(anyString(),anyString());
		verify(mockView, Mockito.times(2)).setEmptyVisible(false);
		verify(mockView).clearMarkdown();
		verify(mockView).setMarkdown(sampleHTML);
		// Called by loadMath, and loadWidgets, 
		verify(mockView, Mockito.times(4)).getElementById(anyString());
		
		//verify tablesorter applied
		verify(mockSynapseJSNIUtils).loadTableSorters();
		
		//verify loadMath
		verify(mockSynapseJSNIUtils).processWithMathJax(mockElementWrapper.getElement());
		
		//verify loadWidgets
		verify(mockWidgetRegistrar).getWidgetContentType(elementContentType);
		verify(mockWidgetRegistrar).getWidgetDescriptor(elementContentType);
		verify(mockWidgetRegistrar).getWidgetRendererForWidgetDescriptor(any(WikiPageKey.class), anyString(), anyMap(), any(Callback.class), any(Long.class));
		verify(mockView).addWidget(any(Widget.class), Mockito.contains(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0"));
	}
	
	@Test
	public void testMarkdownIt2Html() {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		String sampleHTML = "<h1>heading</h1><p>foo baz bar</p>";
		when(mockMarkdownIt.markdown2Html(anyString(), anyString())).thenReturn(sampleHTML);
		String markdown="input markdown that is transformed";
		presenter.configure(markdown, mockWikiPageKey, 1L);
		
		ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).callbackWhenAttached(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		
		verify(mockMarkdownIt).markdown2Html(anyString(),anyString());
		verify(mockView).setMarkdown(sampleHTML);
	}
	
	@Test
	public void testMarkdownIt2HtmlError() {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		String errorMessage = "a js exception";
		when(mockJsException.getMessage()).thenReturn(errorMessage);
		when(mockMarkdownIt.markdown2Html(anyString(), anyString())).thenThrow(mockJsException);
		
		String markdown="input markdown that is transformed";
		presenter.configure(markdown, mockWikiPageKey, 1L);
		
		ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).callbackWhenAttached(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		
		verify(mockMarkdownIt).markdown2Html(anyString(),anyString());
		verify(mockSynAlert).showError(errorMessage);
	}
	
	
	@Test
	public void testLoadMarkdownFromWikiPageFailure() {
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.loadMarkdownFromWikiPage(mockWikiPageKey, false);
		verify(mockSynAlert).showError(anyString());
	}
}
