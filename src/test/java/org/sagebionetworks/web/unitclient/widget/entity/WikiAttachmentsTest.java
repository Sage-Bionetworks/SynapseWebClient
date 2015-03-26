package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;

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
	List<FileHandle> handles;
	
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
		handles = new ArrayList<FileHandle>();
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
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""));
		verify(mockView).reset();
		verify(mockView).addFileHandles(anyList());
		
		assertFalse(presenter.isValid());
		presenter.setSelectedFilename(testFileName);
		assertTrue(presenter.isValid());
	}
	
	
	@Test
	public void testConfigureFail() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getV2WikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""));
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testDelete(){
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""));
		presenter.deleteAttachment(testFileName);
		assertEquals(Arrays.asList(testFileId), presenter.getFilesHandlesToDelete());
	}
	
	@Test
	public void testNoAttachments(){
		handles.clear();
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""));
		verify(mockView).reset();
		verify(mockView).showNoAttachmentRow();
	}
}
