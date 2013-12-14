package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.MarkdownEditorWidget;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class MarkdownEditorWidgetTest {
	NodeModelCreator mockNodeModelCreator;
	SynapseClientAsync mockSynapseClient; 
	SynapseJSNIUtils mockSynapseJSNIUtils; 
	WidgetRegistrar mockWidgetRegistrar;
	MarkdownEditorWidget presenter;
	IconsImageBundle mockIcons;
	SageImageBundle mockSageIcons;
	CookieProvider mockCookies;
	BaseEditWidgetDescriptorPresenter mockBaseEditWidgetPresenter;
	ResourceLoader mockResourceLoader;
	GWTWrapper mockGwt;
	
	@Before
	@Ignore
	public void before() throws JSONObjectAdapterException {
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockIcons = mock(IconsImageBundle.class);
		mockSageIcons = mock(SageImageBundle.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		mockBaseEditWidgetPresenter = mock(BaseEditWidgetDescriptorPresenter.class);
		mockResourceLoader = mock(ResourceLoader.class);
		mockCookies = mock(CookieProvider.class);
		mockGwt = mock(GWTWrapper.class);
		presenter = new MarkdownEditorWidget(mockSynapseClient, mockSynapseJSNIUtils, mockWidgetRegistrar, mockIcons, mockBaseEditWidgetPresenter, mockCookies, mockResourceLoader, mockGwt, mockSageIcons);
	}
	
	@Test
	@Ignore
	public void testPreview() throws Exception {
		final String testHtml = "<h1>HTML Returns</h1>";
		final String testMarkdown = "HTML Returns\n----------";
		AsyncMockStubber
				.callFailureWith(new Exception())
				.when(mockSynapseClient)
				.markdown2Html(anyString(), anyBoolean(), anyBoolean(),anyString(),
						any(AsyncCallback.class));
		
		presenter.showPreview(testMarkdown, true);
		verify(mockSynapseClient).markdown2Html(anyString(), anyBoolean(), anyBoolean(), anyString(), any(AsyncCallback.class));
	}
}
