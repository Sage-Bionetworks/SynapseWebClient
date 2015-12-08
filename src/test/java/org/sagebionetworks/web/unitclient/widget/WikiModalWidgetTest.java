package org.sagebionetworks.web.unitclient.widget;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.WikiModalWidget;
import org.sagebionetworks.web.client.widget.WikiModalWidgetView;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class WikiModalWidgetTest {
	@Mock
	WikiModalWidgetView mockView;
	@Mock
	MarkdownWidget mockMarkdownWidget;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynapseAlert;
	
	WikiModalWidget widget;
	HashMap<String,WikiPageKey> pageNameToWikiPageKey;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		widget = new WikiModalWidget(mockView, mockMarkdownWidget, mockSynapseClient, mockSynapseAlert);
		
		pageNameToWikiPageKey = new HashMap<String, WikiPageKey>();
		AsyncMockStubber.callSuccessWith(pageNameToWikiPageKey).when(mockSynapseClient).getPageNameToWikiKeyMap(any(AsyncCallback.class));
	}

	
	@Test
	public void testShowFromPagename() {
		WikiModalWidget.pageNameToWikiKeyMap = null;
		String pageName = "page1";
		WikiPageKey pageKey = new WikiPageKey();
		pageNameToWikiPageKey.put(pageName, pageKey);
		
		widget.show(pageName);
		
		//view components cleared
		verify(mockMarkdownWidget).clear();
		verify(mockView).clear();
		verify(mockSynapseAlert).clear();
		verify(mockMarkdownWidget).loadMarkdownFromWikiPage(pageKey, false);
		verify(mockView).show();
	}
	@Test
	public void testFailureToLoadMap() {
		WikiModalWidget.pageNameToWikiKeyMap = null;
		String error = "something went wrong";
		Exception ex =new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getPageNameToWikiKeyMap(any(AsyncCallback.class));
		widget.show("anypage");
		verify(mockSynapseAlert).showError(error);
	}
}
