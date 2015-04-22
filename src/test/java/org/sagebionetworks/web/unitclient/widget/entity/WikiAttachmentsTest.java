package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
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
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
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
	String testFileName1 = "testfilename.jpg";
	String testFileName2 ="a file";
	String testFileId = "13";
	List<FileHandle> handles;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockView = Mockito.mock(WikiAttachmentsView.class);
		

		
		FileHandleResults testResults = new FileHandleResults();
		FileHandle testHandle = new S3FileHandle();
		testHandle.setFileName(testFileName1);
		testHandle.setId("12");
		handles = new ArrayList<FileHandle>();
		handles.add(testHandle);
		FileHandle testHandle2 = new S3FileHandle();
		testHandle2.setFileName(testFileName2);
		testHandle2.setId(testFileId);
		handles.add(testHandle2);
		testResults.setList(handles);
		
		WikiPage testPage = new WikiPage();
		// setup the entity editor with 
		presenter = new WikiAttachments(mockView, mockSynapseClient);
		
		AsyncMockStubber.callSuccessWith(testResults).when(mockSynapseClient).getV2WikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(testPage).when(mockSynapseClient).updateV2WikiPage(anyString(), anyString(), any(V2WikiPage.class), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() {
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""));
		verify(mockView).reset();
		verify(mockView).addFileHandles(anyList());
		
		//first item is selected by default
		assertTrue(presenter.isValid());
		presenter.setSelectedFilename(null);
		assertFalse(presenter.isValid());
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

		//first item is selected by default
		assertTrue(presenter.isValid());

		presenter.deleteAttachment(testFileName2);
		assertEquals(Arrays.asList(testFileId), presenter.getFilesHandlesToDelete());
		
		assertTrue(presenter.isValid());
	}
	
	@Test
	public void testNoAttachments(){
		handles.clear();
		presenter.configure(new WikiPageKey("syn1234",ObjectType.ENTITY.toString(),""));
		verify(mockView).reset();
		verify(mockView).showNoAttachmentRow();
		assertNull(presenter.getSelectedFilename());
	}
}
