package org.sagebionetworks.web.unitclient.widget.upload;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.upload.AbstractFileValidator;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidgetImpl;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;

public class FileHandleUploadWidgetImplTest {
	
	SynapseJSNIUtils jsniUtils;
	FileHandleUploadView mockView;
	MultipartUploader mockMultipartUploader;
	CallbackP<FileUpload> mockCallback;
	FileHandleUploadWidgetImpl widget;
	String inputId;
	FileMetadata mockMetadata;
	Callback mockFailedValidationCallback;
	
	@Captor 
	ArgumentCaptor<ProgressingFileUploadHandler> handleCaptor;
	
	
	String fileHandleId = "222";
	String testFileName = "testing.txt";
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		jsniUtils = mock(SynapseJSNIUtils.class);
		mockView = mock(FileHandleUploadView.class);
		mockMultipartUploader = mock(MultipartUploader.class);
		mockCallback = mock(CallbackP.class);
		mockFailedValidationCallback = mock(Callback.class);
		widget = new FileHandleUploadWidgetImpl(mockView, mockMultipartUploader, jsniUtils);
		inputId = "987";

		//The metadata returned should correspond to testFileName
		when(mockView.getInputId()).thenReturn(inputId);
		when(jsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(new String[]{"testName"});
		when(jsniUtils.getContentType(anyString(), anyInt())).thenReturn(null);
		
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
		final FileUpload uploadedFile = new FileUpload(null, successFileHandle);

		// Configure before the test
		widget.configure("button text", mockCallback);
		reset(mockView);
		// method under test.
		widget.onFileSelected();
		verify(mockView).updateProgress(1, "1%");
		verify(mockView).showProgress(true);
		verify(mockView).setInputEnabled(false);
		verify(mockView).hideError();
		
		verify(mockMultipartUploader).uploadFile(anyString(), anyString(), anyInt(), handleCaptor.capture(), any(Long.class));
		handleCaptor.getValue().updateProgress(0.1, "10%", "100 KB/s");
		handleCaptor.getValue().updateProgress(0.9, "90%", "10 MB/s");
		handleCaptor.getValue().uploadSuccess(successFileHandle);

		// The progress should be updated 
		verify(mockView).updateProgress(10, "10%");
		verify(mockView).updateProgress(90, "90%");
		// Success should trigger the following:
		verify(mockCallback).invoke(any(FileUpload.class));
		verify(mockView).updateProgress(100, "100%");
		verify(mockView).setInputEnabled(true);
		verify(mockView).showProgress(false);
		
	}
	
	@Test
	public void testFileSelectedFailedWithValidationCallback(){
		final String error = "An error";
		// Stub a some progress then success.
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				ProgressingFileUploadHandler handler = (ProgressingFileUploadHandler) invocation.getArguments()[1];
				handler.updateProgress(0.1, "10%", "100 KB/s");
				handler.updateProgress(0.9, "90%", "10 MB/s");
				handler.uploadFailed(error);
				return null;
			}
		}).when(mockMultipartUploader).uploadSelectedFile(anyString(), any(ProgressingFileUploadHandler.class), any(Long.class));
		// Configure before the test
		widget.configure("button text", mockCallback);
		AbstractFileValidator validator = new AbstractFileValidator() {
			@Override
			public boolean isValid(FileMetadata file) {
				return false;
			}

			@Override
			public String getInvalidMessage() {
				return null;
			}
		};
		validator.setInvalidFileCallback(mockFailedValidationCallback);
		widget.setValidation(validator);
		reset(mockView);
		// method under test.
		widget.onFileSelected();
		verify(mockView, never()).updateProgress(1, "1%");
		verify(mockView, never()).showProgress(true);
		verify(mockView, never()).setInputEnabled(false);
		verify(mockView, never()).hideError();
		// The progress should be updated 
		verify(mockView, never()).updateProgress(10, "10%");
		verify(mockView, never()).updateProgress(90, "90%");
		// Failure should trigger the following:
		verify(mockView, never()).setInputEnabled(true);
		verify(mockView, never()).showProgress(false);
		verify(mockFailedValidationCallback).invoke();
		verify(mockView, never()).showError(error);
	}
	
	@Test
	public void testFileSelectedFailedNoValidationCallback(){
		final String error = "An error";
		// Stub a some progress then success.
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				ProgressingFileUploadHandler handler = (ProgressingFileUploadHandler) invocation.getArguments()[1];
				handler.updateProgress(0.1, "10%", "100 KB/s");
				handler.updateProgress(0.9, "90%", "10 MB/s");
				handler.uploadFailed(error);
				return null;
			}
		}).when(mockMultipartUploader).uploadSelectedFile(anyString(), any(ProgressingFileUploadHandler.class), any(Long.class));
		// Configure before the test
		widget.configure("button text", mockCallback);
		AbstractFileValidator validator = new AbstractFileValidator () {
			@Override
			public boolean isValid(FileMetadata file) {
				return false;
			}

			@Override
			public String getInvalidMessage() {
				return null;
			}
		};
		widget.setValidation(validator);
		reset(mockView);
		// method under test.
		widget.onFileSelected();
		verify(mockView, never()).updateProgress(1, "1%");
		verify(mockView, never()).showProgress(true);
		verify(mockView, never()).setInputEnabled(false);
		verify(mockView, never()).hideError();
		// The progress should be updated 
		verify(mockView, never()).updateProgress(10, "10%");
		verify(mockView, never()).updateProgress(90, "90%");
		// Failure should trigger the following:
		verify(mockView, never()).setInputEnabled(true);
		verify(mockView, never()).showProgress(false);
		verify(mockFailedValidationCallback, never()).invoke();
		verify(mockView).showError(anyString());
	}

}
