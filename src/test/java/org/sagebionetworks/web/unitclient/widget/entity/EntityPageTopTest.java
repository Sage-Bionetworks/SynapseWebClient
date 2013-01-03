package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;

public class EntityPageTopTest {

	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	GlobalApplicationState mockGlobalApplicationState;
	EntityPageTopView mockView;
	EntitySchemaCache mockSchemaCache;
	JSONObjectAdapter mockJsonObjectAdapter;
	EntityTypeProvider mockEntityTypeProvider;
	IconsImageBundle mockIconsImageBundle;
	EventBus mockEventBus;
	JiraURLHelper mockJiraURLHelper;
	WidgetRegistrar mockWidgetRegistrar;
	EntityPageTop pageTop;
	ExampleEntity entity;
	AttachmentData attachment1;
	WidgetRendererPresenter testWidgetRenderer;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(EntityPageTopView.class);
		mockSchemaCache = mock(EntitySchemaCache.class);
		mockJsonObjectAdapter = mock(JSONObjectAdapter.class);
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockEventBus = mock(EventBus.class);
		mockJiraURLHelper = mock(JiraURLHelper.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		pageTop = new EntityPageTop(mockView, mockSynapseClient,
				mockNodeModelCreator, mockAuthenticationController,
				mockSchemaCache,
				mockEntityTypeProvider,
				mockIconsImageBundle, 
				mockWidgetRegistrar, 
				mockEventBus);
		
		// Setup the the entity
		String entityId = "123";
		entity = new ExampleEntity();
		entity.setId(entityId);
		entity.setEntityType(ExampleEntity.class.getName());
		List<AttachmentData> entityAttachments = new ArrayList<AttachmentData>();
		String attachment1Name = "attachment1";
		attachment1 = new AttachmentData();
		attachment1.setName(attachment1Name);
		attachment1.setTokenId("token1");
		attachment1.setContentType(WidgetConstants.YOUTUBE_CONTENT_TYPE);
		entityAttachments.add(attachment1);
		entity.setAttachments(entityAttachments);
		when(mockJsonObjectAdapter.createNew()).thenReturn(new JSONObjectAdapterImpl());
		when(mockWidgetRegistrar.isWidgetContentType(anyString())).thenReturn(true);
		testWidgetRenderer = new YouTubeWidget(mock(YouTubeWidgetView.class));
		when(mockWidgetRegistrar.getWidgetRendererForWidgetDescriptor(anyString(), anyString(), any(WidgetDescriptor.class))).thenReturn(testWidgetRenderer);
	}

	@Test
	public void testMarkdownToHtmlWiring() {
		final String testHtml = "<h1>HTML Returns</h1>";
		final String testMarkdown = "HTML Returns\n----------";
		AsyncMockStubber
				.callSuccessWith(testHtml)
				.when(mockSynapseClient)
				.markdown2Html(any(String.class), any(String.class), any(Boolean.class),
						any(AsyncCallback.class));
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				assertEquals("unexpected markdown to html conversion",
						testHtml, result);
			}

			@Override
			public void onFailure(Throwable caught) {
				fail("unexpected failure in test: " + caught.getMessage());
			}
		};
		pageTop.getHtmlFromMarkdown(testMarkdown, "", callback);

		verify(mockSynapseClient).markdown2Html(any(String.class),
				any(String.class), any(Boolean.class), any(AsyncCallback.class));
	}
	
	@Test
	@Ignore //ignoring due to GWT.create() failure when mocking HTMLPanel
	public void testLoadWidgets() throws Exception{
		HTMLPanel htmlPanel = Mockito.mock(HTMLPanel.class);
		Element testElement = DOM.createDiv();
		when(htmlPanel.getElementById(anyString())).thenReturn(testElement);
		EntityBundle bundle = new EntityBundle(entity, null, null, null, null, null, null);
		ExampleEntity entity = new ExampleEntity();
		bundle.setEntity(entity);
		pageTop.setBundle(bundle, false);
		AsyncMockStubber
			.callSuccessWith("")
			.when(mockSynapseClient)
			.getWidgetDescriptorJson(anyString(), anyString(), any(AsyncCallback.class));
		pageTop.loadWidgets(htmlPanel);
	}

}
