package org.sagebionetworks.web.unitclient.widget.upload;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import org.mockito.internal.verification.VerificationModeFactory;
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
import org.sagebionetworks.web.client.widget.upload.FileValidator;
import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.shared.WebConstants;
import com.google.gwt.core.client.JavaScriptObject;

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
	public void before() {
		MockitoAnnotations.initMocks(this);
		jsniUtils = mock(SynapseJSNIUtils.class);
		mockView = mock(FileHandleUploadView.class);
		mockMultipartUploader = mock(MultipartUploader.class);
		mockCallback = mock(CallbackP.class);
		mockFailedValidationCallback = mock(Callback.class);
		widget = new FileHandleUploadWidgetImpl(mockView, mockMultipartUploader, jsniUtils);
		inputId = "987";

		// The metadata returned should correspond to testFileName
		when(mockView.getInputId()).thenReturn(inputId);
		when(jsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(new String[] {"testName"});
		when(jsniUtils.getContentType(any(JavaScriptObject.class), anyInt())).thenReturn(null);

	}

	@Test
	public void testConfigure() {
		String text = "button text";
		widget.configure(text, mockCallback);
		verify(mockView).setButtonText(text);
		verify(mockView).hideError();
	}

	@Test
	public void testFileSelected() {
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

		verify(mockMultipartUploader).uploadFile(anyString(), anyString(), any(JavaScriptObject.class), handleCaptor.capture(), any(Long.class), eq(mockView));
		handleCaptor.getValue().updateProgress(0.1, "10%", "100 KB/s");
		handleCaptor.getValue().updateProgress(0.9, "90%", "10 MB/s");
		handleCaptor.getValue().uploadSuccess(successFileHandle);

		// The progress should be updated
		verify(mockView).updateProgress(10, "10%");
		verify(mockView).updateProgress(90, "90%");
		verify(mockView).showProgress(true);
		verify(mockView).setInputEnabled(false);
		verify(mockCallback).invoke(any(FileUpload.class));
		// Success should trigger the following:
		verify(mockView).updateProgress(100, "100%");
		verify(mockView).setInputEnabled(true);
		verify(mockView).showProgress(false);
	}

	@Test
	public void testSelectInvalidFileName() {
		FileValidator mockFileValidator = mock(FileValidator.class);
		when(mockFileValidator.getInvalidFileCallback()).thenReturn(mockFailedValidationCallback);
		when(jsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(new String[] {"testName#($*#.jpg"});
		widget.configure("button text", mockCallback);
		widget.setValidation(mockFileValidator);

		widget.onFileSelected();

		verify(mockView).showError(WebConstants.INVALID_ENTITY_NAME_MESSAGE);
		verify(mockFailedValidationCallback).invoke();
	}

	@Test
	public void testSelectInvalidFileNameNoValidator() {
		when(jsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(new String[] {"testName#($*#.jpg"});
		widget.configure("button text", mockCallback);

		widget.onFileSelected();

		verify(mockView).showError(WebConstants.INVALID_ENTITY_NAME_MESSAGE);
	}

	@Test
	public void testMultiFileSelected() {
		final String successFileHandle = "123";
		when(jsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(new String[] {"testName", "testName2"});

		// Configure before the test
		widget.configure("button text", mockCallback);
		reset(mockView);
		// method under test.
		widget.onFileSelected();
		verify(mockView).updateProgress(1, "1%");
		verify(mockView).showProgress(true);
		verify(mockView).setInputEnabled(false);
		verify(mockView).hideError();

		// The progress should be updated with scaled values based on the presence of two files to upload
		verify(mockMultipartUploader).uploadFile(anyString(), anyString(), any(JavaScriptObject.class), handleCaptor.capture(), any(Long.class), eq(mockView));
		verify(mockView).showProgress(true);
		verify(mockView).setInputEnabled(false);
		handleCaptor.getValue().updateProgress(0.1, "10%", "100 KB/s");
		verify(mockView).updateProgress(5, "5%");
		handleCaptor.getValue().updateProgress(0.9, "90%", "10 MB/s");
		verify(mockView).updateProgress(45, "45%");
		handleCaptor.getValue().uploadSuccess(successFileHandle);
		verify(mockView).updateProgress(50, "50%");
		handleCaptor.getValue().updateProgress(0.2, "20%", "10 MB/s");
		verify(mockView).updateProgress(60, "60%");
		handleCaptor.getValue().uploadSuccess(successFileHandle);
		verify(mockView).updateProgress(100, "100%");

		// Success should trigger the following:
		verify(mockView).setInputEnabled(true);
		verify(mockView).showProgress(false);
		verify(mockCallback, VerificationModeFactory.times(2)).invoke(any(FileUpload.class));
	}

	@Test
	public void testFileSelectedFailedWithValidationCallback() {
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
		}).when(mockMultipartUploader).uploadFile(anyString(), anyString(), any(JavaScriptObject.class), handleCaptor.capture(), any(Long.class), eq(mockView));

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
	public void testFileSelectedFailedNoValidationCallback() {
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
		}).when(mockMultipartUploader).uploadFile(anyString(), anyString(), any(JavaScriptObject.class), handleCaptor.capture(), any(Long.class), eq(mockView));
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
