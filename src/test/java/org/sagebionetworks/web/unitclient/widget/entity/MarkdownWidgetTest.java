package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.cache.markdown.MarkdownCacheKey;
import org.sagebionetworks.web.client.widget.cache.markdown.MarkdownCacheValue;
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
	@Mock
	SessionStorage mockSessionStorage;
	@Mock
	MarkdownCacheKey mockMarkdownCacheKey;
	@Mock
	MarkdownCacheValue mockMarkdownCacheValue;
	@Mock
	MarkdownIt mockMarkdownIt;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
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
		when(mockInjector.getMarkdownCacheKey()).thenReturn(mockMarkdownCacheKey);
		when(mockInjector.getMarkdownCacheValue()).thenReturn(mockMarkdownCacheValue);
		presenter = new MarkdownWidget(mockSynapseClient, mockSynapseJSNIUtils, mockWidgetRegistrar, mockCookies, mockResourceLoader, mockGwt, mockInjector, mockView, mockSynAlert, mockSessionStorage, mockMarkdownIt);
	}
	
	@Test
	public void testConfigureSuccess() {
		String sampleHTML = "<h1>heading</h1><p>foo baz bar</p>";
		
		AsyncMockStubber.callSuccessWith(sampleHTML).when(mockSynapseClient).markdown2Html(anyString(), anyString(), anyBoolean(), anyString(), any(AsyncCallback.class));
		//only the first getElementById called by each getElementById finds its target so it doesn't look forever but still can be verified
		when(mockView.getElementById(WidgetConstants.MARKDOWN_TABLE_ID_PREFIX + "0")).thenReturn(mockElementWrapper);
		when(mockView.getElementById(Mockito.contains(WidgetConstants.DIV_ID_MATHJAX_PREFIX + "0"))).thenReturn(mockElementWrapper);
		when(mockView.getElementById(Mockito.contains(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0"))).thenReturn(mockElementWrapper);
		when(mockResourceLoader.isLoaded(any(WebResource.class))).thenReturn(true);
		presenter.configure(testMarkdown, mockWikiPageKey, null);
		ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).callbackWhenAttached(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		verify(mockSynapseClient).markdown2Html(anyString(), anyString(), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockView).setMarkdown(sampleHTML);
		verify(mockSessionStorage).setItem(anyString(), anyString());
		
		// Called three times between tablesorter, loadMath, and loadWidgets, 
		// then another three to determine null
		verify(mockView, Mockito.times(6)).getElementById(anyString());
		
		//verify tablesorter applied
		verify(mockSynapseJSNIUtils).tablesorter(anyString());
		
		//verify loadMath
		verify(mockSynapseJSNIUtils).processWithMathJax(mockElementWrapper.getElement());
		
		//verify highlight code blocks applied
		verify(mockSynapseJSNIUtils).highlightCodeBlocks();
				
		//verify loadWidgets
		verify(mockWidgetRegistrar).getWidgetContentType(elementContentType);
		verify(mockWidgetRegistrar).getWidgetDescriptor(elementContentType);
		verify(mockWidgetRegistrar).getWidgetRendererForWidgetDescriptor(Mockito.eq(mockWikiPageKey), anyString(), anyMap(), any(Callback.class), any(Long.class));
		verify(mockView).addWidget(any(Widget.class), Mockito.contains(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0"));
	}
	
	@Test
	public void testConfigureFailure() {
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).markdown2Html(anyString(), anyString(), anyBoolean(), anyString(), any(AsyncCallback.class));
		presenter.configure(testMarkdown, mockWikiPageKey, null);
		
		verify(mockSynAlert).handleException(caught);
	}
	
	@Test
	public void testLoadMarkdownFromWikiPageSuccess() {
		String sampleHTML = "<h1>heading</h1><p>foo baz bar</p>";
		AsyncMockStubber.callSuccessWith(mockWikiPage).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(sampleHTML).when(mockSynapseClient).markdown2Html(anyString(), anyString(), anyBoolean(), anyString(), any(AsyncCallback.class));
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
		
		verify(mockSynapseClient).markdown2Html(anyString(), anyString(), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockView, Mockito.times(2)).setEmptyVisible(false);
		verify(mockView).clearMarkdown();
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
		verify(mockView).addWidget(any(Widget.class), Mockito.contains(org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + "0"));
	}
	

	@Test
	public void testLoadMarkdownFromWikiEmpty() {
		String sampleHTML = "";
		AsyncMockStubber.callSuccessWith(sampleHTML).when(mockSynapseClient).markdown2Html(anyString(), anyString(), anyBoolean(), anyString(), any(AsyncCallback.class));
		String markdown="input markdown that is transformed into empty html";
		presenter.configure(markdown, mockWikiPageKey, 1L);
		
		ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).callbackWhenAttached(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		
		verify(mockSynapseClient).markdown2Html(anyString(), anyString(), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockView).setEmptyVisible(false);
		verify(mockView).setEmptyVisible(true);
		verify(mockView).clearMarkdown();
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
		//verify highlight code blocks never called (part of parsing)
		verify(mockSynapseJSNIUtils, never()).highlightCodeBlocks();

	}
	
	
	@Test
	public void testLoadMarkdownFromWikiPageFailure() {
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.loadMarkdownFromWikiPage(mockWikiPageKey, false);
		verify(mockSynAlert).showError(anyString());
	}
	
	@Test
	public void testMdCache() {
		//simulate value is found in the cache.
		String sampleHTML = "<h1>heading</h1><p>foo baz bar</p>";
		String uniqueSuffix = "1298375478";
		when(mockSessionStorage.getItem(anyString())).thenReturn("json representing MarkdownCacheValue");
		when(mockMarkdownCacheValue.getHtml()).thenReturn(sampleHTML);
		when(mockMarkdownCacheValue.getUniqueSuffix()).thenReturn(uniqueSuffix);
		presenter.configure(testMarkdown, mockWikiPageKey, null);
		
		ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).callbackWhenAttached(callbackCaptor.capture());
		callbackCaptor.getValue().invoke();
		verify(mockSynapseClient, never()).markdown2Html(anyString(), anyString(), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockSessionStorage, never()).setItem(anyString(), anyString());
		verify(mockView).setMarkdown(sampleHTML);
		
	}
}
