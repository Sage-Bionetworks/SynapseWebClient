package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class WikiFilesPreviewWidgetTest {
		
	WikiFilesPreviewWidget widget;
	WikiFilesPreviewWidgetView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	
	@Before
	public void setup() throws JSONObjectAdapterException {
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockView = mock(WikiFilesPreviewWidgetView.class);
		AsyncMockStubber.callSuccessWith("filehandleresults").when(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		FileHandleResults testResults = new FileHandleResults();
		when(mockNodeModelCreator.createJSONEntity(anyString(), any(Class.class))).thenReturn(testResults);
		widget = new WikiFilesPreviewWidget(mockView, mockSynapseClient, mockNodeModelCreator);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		widget.configure(wikiKey, descriptor, null);
		verify(mockView).configure(any(WikiPageKey.class), any(List.class));
	}
	
	@Test
	public void testConfigureFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		Map<String, String> descriptor = new HashMap<String, String>();
		widget.configure(wikiKey, descriptor, null);
		verify(mockView).showErrorMessage(anyString());
	}

}
