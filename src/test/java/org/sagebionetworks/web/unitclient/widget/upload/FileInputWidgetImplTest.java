package org.sagebionetworks.web.unitclient.widget.upload;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
		widget = new FileInputWidgetImpl(mockView, multipartUploaderStub);
	}
	
	@Test 
	public void testConfigure(){
		widget.configure(mockHandler);
		verify(mockView).resetForm();
		verify(mockView).showProgress(false);
		verify(mockView).setInputEnabled(true);
	}
	
	public void testUploadSelectedFile(){
		widget.configure(mockHandler);
		String fileHandleId = "123";
		multipartUploaderStub.setFileHandle(fileHandleId);
		multipartUploaderStub.setProgressText("one","two", "three");
		widget.uploadSelectedFile();
	}
}
