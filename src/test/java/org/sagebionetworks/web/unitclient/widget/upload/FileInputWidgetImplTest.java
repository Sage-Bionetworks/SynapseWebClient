package org.sagebionetworks.web.unitclient.widget.upload;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.upload.FileInputView;
import org.sagebionetworks.web.client.widget.upload.FileInputWidgetImpl;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
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
	public void testUploadSelectedFile(){
		reset(mockView);
		String fileHandleId = "123";
		FileUpload uploadedFile = new FileUpload(null, fileHandleId);
		multipartUploaderStub.setFileHandle(fileHandleId);
		String[] progress = new String[]{"one","two", "three"};
		multipartUploaderStub.setProgressText(progress);
		String[] uploadSpeed = new String[]{"1 MB/s","2 MB/s", "3 MB/s"};
		multipartUploaderStub.setUploadSpeed(uploadSpeed);
		widget.uploadSelectedFile(mockHandler);
		verify(mockView).setInputEnabled(false);
		// update at the start and end, plus each actual update
		verify(mockView, times(progress.length+2)).updateProgress(anyDouble(), anyString());
		verify(mockHandler).uploadSuccess(anyString());
		verify(mockHandler, never()).uploadFailed(anyString());
	}
	
	@Test
	public void testUploadSelectedFailure(){
		String fileHandleId = "123";
		FileUpload uploadedFile = new FileUpload(null, fileHandleId);
		multipartUploaderStub.setFileHandle(fileHandleId);
		String[] progress = new String[]{"one","two", "three"};
		multipartUploaderStub.setProgressText(progress);
		String[] uploadSpeed = new String[]{"1 MB/s","2 MB/s", "3 MB/s"};
		multipartUploaderStub.setUploadSpeed(uploadSpeed);
		String error = "an error";
		multipartUploaderStub.setError(error);
		widget.uploadSelectedFile(mockHandler);
		verify(mockView).setInputEnabled(false);
		// update at the start but not the end plus the progress that was made
		verify(mockView, times(progress.length+1)).updateProgress(anyDouble(), anyString());
		verify(mockView).setInputEnabled(true);
		verify(mockView).showProgress(false);
		verify(mockHandler, never()).uploadSuccess(anyString());
		verify(mockHandler).uploadFailed(error);
	}
	
	@Test
	public void testRest(){
		widget.reset();
		verify(mockView).resetForm();
		verify(mockView).setInputEnabled(true);
		verify(mockView).showProgress(false);
	}
}
