package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
	
	EntityPageTop pageTop;
	@Before
	public void before() throws JSONObjectAdapterException{
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
		pageTop = new EntityPageTop(mockView, mockSynapseClient, mockNodeModelCreator, mockAuthenticationController,
				mockGlobalApplicationState,
				mockSchemaCache,
				mockJsonObjectAdapter,
				mockEntityTypeProvider,
				mockIconsImageBundle,
				mockJiraURLHelper,
				mockEventBus);
	}
		
	@Test
	public void testMarkdownToHtmlWiring(){
		final String testHtml = "<h1>HTML Returns</h1>";
		final String testMarkdown = "HTML Returns\n----------";
		AsyncMockStubber.callSuccessWith(testHtml).when(mockSynapseClient).markdown2Html(any(String.class), any(AsyncCallback.class));
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				assertEquals("unexpected markdown to html conversion", testHtml, result);
			}
			@Override
			public void onFailure(Throwable caught) {
				throw new IllegalArgumentException(caught);
			}
		};
		pageTop.getHtmlFromMarkdown(testMarkdown, callback);
		
		verify(mockSynapseClient).markdown2Html(any(String.class), any(AsyncCallback.class));
	}
	
	}
