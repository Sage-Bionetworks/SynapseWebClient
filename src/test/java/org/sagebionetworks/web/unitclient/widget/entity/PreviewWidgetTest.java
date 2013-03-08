package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;


import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.DisplayUtils;
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
	Entity testEntity;
	List<FileHandle> testFileHandleList;
	Response mockResponse;
	
	@Before
	public void before() throws Exception{
		mockView = mock(PreviewWidgetView.class);
		mockRequestBuilder = mock(RequestBuilderWrapper.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		previewWidget = new PreviewWidget(mockView, mockRequestBuilder, mockSynapseJSNIUtils);
		testEntity = new FileEntity();
		testFileHandleList = new ArrayList<FileHandle>();
		testBundle = new EntityBundle(testEntity, null,null,null,null,null,null,testFileHandleList);
		when(mockSynapseJSNIUtils.getBaseFileHandleUrl()).thenReturn("http://fakebaseurl/");
		mockResponse = mock(Response.class);
		when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
		when(mockResponse.getText()).thenReturn("Some test response text");
		
		RequestBuilderMockStubber.callOnResponseReceived(null, mockResponse).when(mockRequestBuilder).sendRequest(anyString(), any(RequestCallback.class));
	}
	
	public void testNoPreviewFileHandleAvailable(){
		S3FileHandle fh = new S3FileHandle();
		fh.setContentType("image/png");
		testFileHandleList.add(fh);
		previewWidget.asWidget(testBundle);
		verify(mockView, times(0)).setImagePreview(anyString(), anyString());
	}
	
	@Test
	public void testPreviewImageContentType(){
		PreviewFileHandle fh = new PreviewFileHandle();
		fh.setContentType("image/png");
		testFileHandleList.add(fh);
		previewWidget.asWidget(testBundle);
		verify(mockView).setImagePreview(anyString(), anyString());
	}
	
	@Test
	public void testPreviewCodeContentType(){
		PreviewFileHandle fh = new PreviewFileHandle();
		String aCodeContentType = DisplayUtils.CODE_EXTENSIONS[0];
		fh.setContentType(aCodeContentType);
		testFileHandleList.add(fh);
		previewWidget.asWidget(testBundle);
		verify(mockView).setCodePreview(anyString());
	}

	@Test
	public void testPreviewOtherTextContentType(){
		PreviewFileHandle fh = new PreviewFileHandle();
		String invalidContentType = "invalid/content-type";
		fh.setContentType(invalidContentType);
		testFileHandleList.add(fh);
		previewWidget.asWidget(testBundle);
		verify(mockView).setBlockQuotePreview(anyString());
	}
}
