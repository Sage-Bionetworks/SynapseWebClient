package org.sagebionetworks.web.unitclient.widget.upload;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidgetImpl;
import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.UploadedFile;

public class FileHandleUploadWidgetImplTest {
	
	FileHandleUploadView mockView;
	MultipartUploader mockMultipartUploader;
	CallbackP<UploadedFile> mockCallback;
	FileHandleUploadWidgetImpl widget;
	String inputId;
	
	
	@Before
	public void before(){
		mockView = Mockito.mock(FileHandleUploadView.class);
		mockMultipartUploader = Mockito.mock(MultipartUploader.class);
		mockCallback = Mockito.mock(CallbackP.class);
		widget = new FileHandleUploadWidgetImpl(mockView, mockMultipartUploader);
		inputId = "987";
		when(mockView.getInputId()).thenReturn(inputId);
	}
	
	@Test
	public void testConfigure(){
		String text = "button text";
		widget.configure(text, mockCallback);
		verify(mockView).setButtonText(text);
		verify(mockView).hideError();
	}
	
	@Test
	public void testFileSelected(){
		final String successFileHandle = "123";
		final UploadedFile uploadedFile = new UploadedFile(null, successFileHandle);
		// Stub a some progress then success.
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				ProgressingFileUploadHandler handler = (ProgressingFileUploadHandler) invocation.getArguments()[1];
				handler.updateProgress(0.1, "10%");
				handler.updateProgress(0.9, "90%");
				handler.uploadSuccess(uploadedFile);
				return null;
			}
		}).when(mockMultipartUploader).uploadSelectedFile(anyString(), any(ProgressingFileUploadHandler.class), any(Long.class));
		// Configure before the test
		widget.configure("button text", mockCallback);
		reset(mockView);
		// method under test.
		widget.onFileSelected();
		verify(mockView).updateProgress(1, "1%");
		verify(mockView).showProgress(true);
		verify(mockView).setInputEnabled(false);
		verify(mockView).hideError();
		// The progress should be updated 
		verify(mockView).updateProgress(10, "10%");
		verify(mockView).updateProgress(90, "90%");
		// Success should trigger the following:
		verify(mockView).updateProgress(100, "100%");
		verify(mockView).setInputEnabled(true);
		verify(mockView).showProgress(false);
		verify(mockCallback).invoke(uploadedFile);
	}
	
	@Test
	public void testFileSelectedFailed(){
		final String error = "An error";
		// Stub a some progress then success.
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				ProgressingFileUploadHandler handler = (ProgressingFileUploadHandler) invocation.getArguments()[1];
				handler.updateProgress(0.1, "10%");
				handler.updateProgress(0.9, "90%");
				handler.uploadFailed(error);
				return null;
			}
		}).when(mockMultipartUploader).uploadSelectedFile(anyString(), any(ProgressingFileUploadHandler.class), any(Long.class));
		// Configure before the test
		widget.configure("button text", mockCallback);
		reset(mockView);
		// method under test.
		widget.onFileSelected();
		verify(mockView).updateProgress(1, "1%");
		verify(mockView).showProgress(true);
		verify(mockView).setInputEnabled(false);
		verify(mockView).hideError();
		// The progress should be updated 
		verify(mockView).updateProgress(10, "10%");
		verify(mockView).updateProgress(90, "90%");
		// Failure should trigger the following:
		verify(mockView).setInputEnabled(true);
		verify(mockView).showProgress(false);
		verify(mockCallback, never()).invoke(any(UploadedFile.class));
		verify(mockView).showError(error);
	}

}
