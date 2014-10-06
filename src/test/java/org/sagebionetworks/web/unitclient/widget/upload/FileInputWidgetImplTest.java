package org.sagebionetworks.web.unitclient.widget.upload;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.upload.FileInputView;
import org.sagebionetworks.web.client.widget.upload.FileInputWidgetImpl;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;

public class FileInputWidgetImplTest {

	FileInputView mockView;
	MultipartUploaderStub multipartUploaderStub;
	FileUploadHandler mockHandler;
	FileInputWidgetImpl widget;
	
	@Before
	public void before(){
		mockView = mock(FileInputView.class);
		mockHandler = mock(FileUploadHandler.class);
		multipartUploaderStub = new MultipartUploaderStub();
		widget = new FileInputWidgetImpl(mockView, multipartUploaderStub);
	}
	
	@Test 
	public void testConfigure(){
		widget.configure(mockHandler);
		verify(mockView).resetForm();
		verify(mockView).showProgress(false);
		verify(mockView).setInputEnabled(true);
	}
	
	@Test
	public void testUploadSelectedFile(){
		widget.configure(mockHandler);
		String fileHandleId = "123";
		multipartUploaderStub.setFileHandle(fileHandleId);
		String[] progress = new String[]{"one","two", "three"};
		multipartUploaderStub.setProgressText(progress);
		widget.uploadSelectedFile();
		// update at the start and end, plus each actual update
		verify(mockView, times(progress.length+2)).updateProgress(anyDouble(), anyString());
		verify(mockHandler).uploadSuccess(fileHandleId);
		verify(mockHandler, never()).uploadFailed(anyString());
	}
	
	@Test
	public void testUploadSelectedFailure(){
		widget.configure(mockHandler);
		String fileHandleId = "123";
		multipartUploaderStub.setFileHandle(fileHandleId);
		String[] progress = new String[]{"one","two", "three"};
		multipartUploaderStub.setProgressText(progress);
		String error = "an error";
		multipartUploaderStub.setError(error);
		widget.uploadSelectedFile();
		// update at the start but not the end plus the progress that was made
		verify(mockView, times(progress.length+1)).updateProgress(anyDouble(), anyString());
		verify(mockHandler, never()).uploadSuccess(anyString());
		verify(mockHandler).uploadFailed(error);
	}
}
