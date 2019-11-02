package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiFilesPreviewWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class WikiFilesPreviewWidgetTest {

	WikiFilesPreviewWidget widget;
	WikiFilesPreviewWidgetView mockView;
	SynapseClientAsync mockSynapseClient;

	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);

	@Before
	public void setup() throws JSONObjectAdapterException {
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(WikiFilesPreviewWidgetView.class);

		FileHandleResults testResults = new FileHandleResults();
		AsyncMockStubber.callSuccessWith(testResults).when(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		widget = new WikiFilesPreviewWidget(mockView, mockSynapseClient);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).configure(any(WikiPageKey.class), any(List.class));
	}

	@Test
	public void testConfigureFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		Map<String, String> descriptor = new HashMap<String, String>();
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).showErrorMessage(anyString());
	}

}
