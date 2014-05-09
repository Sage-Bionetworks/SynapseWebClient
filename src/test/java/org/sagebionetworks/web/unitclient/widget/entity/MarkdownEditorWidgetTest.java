package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidgetView;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MarkdownEditorWidgetTest {
	NodeModelCreator mockNodeModelCreator;
	SynapseClientAsync mockSynapseClient; 
	MarkdownEditorWidgetView mockView;
	SynapseJSNIUtils mockSynapseJSNIUtils; 
	WidgetRegistrar mockWidgetRegistrar;
	MarkdownEditorWidget presenter;
	IconsImageBundle mockIcons;
	CookieProvider mockCookies;
	BaseEditWidgetDescriptorPresenter mockBaseEditWidgetPresenter;
	ResourceLoader mockResourceLoader;
	GWTWrapper mockGwt;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockIcons = mock(IconsImageBundle.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		mockBaseEditWidgetPresenter = mock(BaseEditWidgetDescriptorPresenter.class);
		mockResourceLoader = mock(ResourceLoader.class);
		mockCookies = mock(CookieProvider.class);
		mockGwt = mock(GWTWrapper.class);
		mockView = mock(MarkdownEditorWidgetView.class);
		presenter = new MarkdownEditorWidget(mockView, mockSynapseClient, mockCookies, mockGwt);
	}
	

	@Test
	public void testGetFormattingGuide() throws Exception {
		Map<String,WikiPageKey> testHelpPagesMap = new HashMap<String, WikiPageKey>();
		WikiPageKey formattingGuideWikiKey = new WikiPageKey("syn1234", ObjectType.ENTITY.toString(), null);
		testHelpPagesMap.put(WebConstants.FORMATTING_GUIDE, formattingGuideWikiKey);
		AsyncMockStubber
				.callSuccessWith(testHelpPagesMap)
				.when(mockSynapseClient)
				.getHelpPages(any(AsyncCallback.class));
		CallbackP<WikiPageKey> mockCallback = mock(CallbackP.class);
		presenter.getFormattingGuideWikiKey(mockCallback);
		//service was called
		verify(mockSynapseClient).getHelpPages(any(AsyncCallback.class));
		//and callback was invoked with the formatting guide wiki key
		ArgumentCaptor<WikiPageKey> wikiKeyCaptor = ArgumentCaptor.forClass(WikiPageKey.class);
		verify(mockCallback).invoke(wikiKeyCaptor.capture());
		assertEquals(formattingGuideWikiKey, wikiKeyCaptor.getValue());
	}
	
	@Test
	public void testGetFormattingGuideFailure() throws Exception {
		AsyncMockStubber
				.callFailureWith(new Exception())
				.when(mockSynapseClient)
				.getHelpPages(any(AsyncCallback.class));
		CallbackP<WikiPageKey> mockCallback = mock(CallbackP.class);
		presenter.getFormattingGuideWikiKey(mockCallback);
		//service was called
		verify(mockSynapseClient).getHelpPages(any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testPreview() throws Exception {
		String htmlReturned = "<h1>Html returned</h2>";
		final String testMarkdown = "HTML Returns\n----------";
		AsyncMockStubber
				.callSuccessWith(htmlReturned)
				.when(mockSynapseClient)
				.markdown2Html(anyString(), anyBoolean(), anyBoolean(),anyString(),
						any(AsyncCallback.class));
		
		presenter.showPreview(testMarkdown, true);
		verify(mockSynapseClient).markdown2Html(anyString(), anyBoolean(), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockView).showPreviewHTML(eq(htmlReturned), anyBoolean());
	}
	
	@Test
	public void testPreviewFailure() throws Exception {
		final String testMarkdown = "HTML Returns\n----------";
		AsyncMockStubber
				.callFailureWith(new Exception())
				.when(mockSynapseClient)
				.markdown2Html(anyString(), anyBoolean(), anyBoolean(),anyString(),
						any(AsyncCallback.class));
		
		presenter.showPreview(testMarkdown, true);
		verify(mockSynapseClient).markdown2Html(anyString(), anyBoolean(), anyBoolean(), anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
}
