package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
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
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	String testFileName ="a file";
	String testFileId = "13";
	@Before
	public void before() throws JSONObjectAdapterException{
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockNodeModelCreator = Mockito.mock(NodeModelCreator.class);
		mockView = Mockito.mock(WikiAttachmentsView.class);
		
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).getV2WikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith("").when(mockSynapseClient).updateV2WikiPage(anyString(), anyString(), anyString(), any(AsyncCallback.class));
		
		FileHandleResults testResults = new FileHandleResults();
		FileHandle testHandle = new S3FileHandle();
		testHandle.setFileName("testfilename.jpg");
		testHandle.setId("12");
		List<FileHandle> handles = new ArrayList<FileHandle>();
		handles.add(testHandle);
		FileHandle testHandle2 = new S3FileHandle();
		testHandle2.setFileName(testFileName);
		testHandle2.setId(testFileId);
		handles.add(testHandle2);
		testResults.setList(handles);
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(FileHandleResults.class))).thenReturn(testResults);
		
		WikiPage testPage = new WikiPage();
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(WikiPage.class))).thenReturn(testPage);
		// setup the entity editor with 
		presenter = new WikiAttachments(mockView, mockSynapseClient, mockNodeModelCreator);
	}

	@Test
	public void testConfigure() {
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""), new WikiPage(), null);
		verify(mockView).configure(any(WikiPageKey.class), any(List.class));
	}
	
	@Test
	public void testConfigureFail() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getV2WikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""), new WikiPage(), null);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testDelete(){
		WikiAttachments.Callback callback = Mockito.mock(WikiAttachments.Callback.class);
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""), new WikiPage(), callback);
		presenter.deleteAttachment(testFileName);
		verify(callback).attachmentsToDelete(eq(testFileName), eq(Arrays.asList(testFileId)));
	}
}
