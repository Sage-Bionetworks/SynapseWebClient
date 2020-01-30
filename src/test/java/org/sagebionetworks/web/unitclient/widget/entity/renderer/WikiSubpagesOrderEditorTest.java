package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditorView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class WikiSubpagesOrderEditorTest {

	WikiSubpagesOrderEditor editor;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	WikiSubpagesOrderEditorView mockView;
	@Mock
	WikiSubpageOrderEditorTree mockEditorTree;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	List<V2WikiHeader> mockWikiHeaders;
	@Mock
	V2WikiOrderHint mockHint;

	public static final String OWNER_OBJECT_NAME = "project a";
	@Mock
	WikiPageKey mockPageKey;

	@Before
	public void before() {
		editor = new WikiSubpagesOrderEditor(mockView, mockEditorTree, mockSynAlert, mockJsClient);
		AsyncMockStubber.callSuccessWith(mockWikiHeaders).when(mockJsClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockHint).when(mockJsClient).getV2WikiOrderHint(any(WikiPageKey.class), any(AsyncCallback.class));

	}

	@Test
	public void testConfigure() {
		editor.configure(mockPageKey, OWNER_OBJECT_NAME);
		verify(mockSynAlert).clear();
		verify(mockView).setLoadingVisible(true);
		verify(mockJsClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockJsClient).getV2WikiOrderHint(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockEditorTree).configure(eq((String) null), eq(mockPageKey), eq(mockWikiHeaders), eq(OWNER_OBJECT_NAME), eq(mockHint), any(CallbackP.class));
		verify(mockView).setLoadingVisible(false);
	}

	@Test
	public void testConfigureGetHeaderTreeFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		editor.configure(mockPageKey, OWNER_OBJECT_NAME);
		verify(mockJsClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
		verify(mockJsClient, never()).getV2WikiOrderHint(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockView).setLoadingVisible(false);
	}

	@Test
	public void testConfigureGetHintFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).getV2WikiOrderHint(any(WikiPageKey.class), any(AsyncCallback.class));
		editor.configure(mockPageKey, OWNER_OBJECT_NAME);
		verify(mockJsClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockJsClient).getV2WikiOrderHint(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
		verify(mockView).setLoadingVisible(false);
	}

	@Test
	public void testGetTree() {
		editor.configure(mockPageKey, OWNER_OBJECT_NAME);
		assertEquals(mockEditorTree, editor.getTree());
	}

}
