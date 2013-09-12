package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.WikiAttachments;
import org.sagebionetworks.web.client.widget.entity.WikiAttachmentsView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for wiki attachments widget
 */
public class WikiAttachmentsTest {

	WikiAttachments presenter;
	WikiAttachmentsView mockView;
	GlobalApplicationState mockGlobalAppState;
	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	JSONObjectAdapter mockJSONObjectAdapter;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockGlobalAppState = Mockito.mock(GlobalApplicationState.class);
		mockAuthenticationController = Mockito.mock(AuthenticationController.class);
		mockNodeModelCreator = Mockito.mock(NodeModelCreator.class);
		mockJSONObjectAdapter = Mockito.mock(JSONObjectAdapter.class);
		mockView = Mockito.mock(WikiAttachmentsView.class);
		when(mockJSONObjectAdapter.createNew()).thenReturn(new JSONObjectAdapterImpl());
		
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).updateWikiPage(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		
		FileHandleResults testResults = new FileHandleResults();
		FileHandle testHandle = new S3FileHandle();
		testHandle.setFileName("testfilename.jpg");
		testHandle.setId("12");
		List<FileHandle> handles = new ArrayList<FileHandle>();
		handles.add(testHandle);
		testResults.setList(handles);
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(FileHandleResults.class))).thenReturn(testResults);
		
		WikiPage testPage = new WikiPage();
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(WikiPage.class))).thenReturn(testPage);
		// setup the entity editor with 
		presenter = new WikiAttachments(mockView, mockSynapseClient, mockGlobalAppState, mockAuthenticationController, mockJSONObjectAdapter, mockNodeModelCreator);
	}

	@Test
	public void testConfigure() {
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""), new WikiPage(), true, null);
		verify(mockView).configure(any(WikiPageKey.class), any(List.class), anyBoolean());
	}
	
	@Test
	public void testConfigureFail() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""), new WikiPage(), true, null);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testDelete(){
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""), new WikiPage(), true, null);
		presenter.deleteAttachment("a file");
		verify(mockSynapseClient).updateWikiPage(anyString(), anyString(), anyString(), any(AsyncCallback.class));
	}
}
