package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.PreviewWidget;
import org.sagebionetworks.web.client.widget.entity.PreviewWidgetView;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

/**
 * Unit test for the preview widget.
 * @author jayhodgson
 *
 */
public class PreviewWidgetTest {
	PreviewWidget previewWidget;
	PreviewWidgetView mockView; 
	RequestBuilderWrapper mockRequestBuilder;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	EntityBundle testBundle;
	FileEntity testEntity;
	List<FileHandle> testFileHandleList;
	Response mockResponse;
	FileHandle mainFileHandle;
	String zipTestString = "base.jar\ntarget/\ntarget/directory/\ntarget/directory/test.txt\n";
	@Before
	public void before() throws Exception{
		mockView = mock(PreviewWidgetView.class);
		mockRequestBuilder = mock(RequestBuilderWrapper.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		previewWidget = new PreviewWidget(mockView, mockRequestBuilder, mockSynapseJSNIUtils);
		testEntity = new FileEntity();
		testFileHandleList = new ArrayList<FileHandle>();
		mainFileHandle = new S3FileHandle();
		String mainFileId = "MAIN_FILE";
		mainFileHandle.setId(mainFileId);
		testFileHandleList.add(mainFileHandle);
		testEntity.setDataFileHandleId(mainFileId);
		testBundle = new EntityBundle(testEntity, null,null,null,null,null,testFileHandleList, null);
		when(mockSynapseJSNIUtils.getBaseFileHandleUrl()).thenReturn("http://fakebaseurl/");
		mockResponse = mock(Response.class);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		when(mockResponse.getText()).thenReturn(zipTestString);
		
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse).when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
	}
	
	public void testNoPreviewFileHandleAvailable(){
		S3FileHandle fh = new S3FileHandle();
		fh.setId("previewFileId");
		fh.setContentType("image/png");
		fh.setFileName("preview.png");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView, times(0)).setImagePreview(anyString(), anyString());
	}
	
	@Test
	public void testPreviewImageContentType(){
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setFileName("preview.png");
		fh.setContentType("image/png");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setImagePreview(anyString(), anyString());
	}
	
	@Test
	public void testPreviewCodeContentType(){
		mainFileHandle.setFileName("codeFile.R");
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setContentType(ContentTypeUtils.PLAIN_TEXT);
		fh.setFileName("preview.txt");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setCodePreview(anyString());
	}

	@Test
	public void testPreviewOtherTextContentType(){
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		String invalidContentType = "text/other";
		fh.setContentType(invalidContentType);
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setTextPreview(anyString());
	}
	
	@Test
	public void testZipContentType(){
		mainFileHandle.setContentType(PreviewWidget.APPLICATION_ZIP);
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setId("previewFileId");
		fh.setContentType("text/csv");
		testFileHandleList.add(fh);
		previewWidget.configure(testBundle);
		previewWidget.asWidget();
		verify(mockView).setTextPreview(anyString());
	}
}
