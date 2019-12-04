package org.sagebionetworks.web.unitclient.widget.upload;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.ImageUploadView;
import org.sagebionetworks.web.client.widget.upload.ImageUploadWidget;
import org.sagebionetworks.web.client.widget.upload.JavaScriptObjectWrapper;
import org.sagebionetworks.web.client.widget.upload.MultipartUploader;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import com.google.gwt.core.client.JavaScriptObject;

public class ImageUploadWidgetImplTest {
	@Mock
	SynapseJSNIUtils mockJSNIUtils;
	@Mock
	ImageUploadView mockView;
	@Mock
	MultipartUploader mockMultipartUploader;
	@Mock
	CallbackP<FileUpload> mockCallback;

	ImageUploadWidget widget;
	String inputId;
	@Mock
	FileMetadata mockMetadata;
	@Mock
	PortalGinInjector mockPortalGinInjector;

	@Captor
	ArgumentCaptor<ProgressingFileUploadHandler> handleCaptor;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	JavaScriptObjectWrapper mockBlob;

	String fileHandleId = "222";
	String testFileName = "testing.txt";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new ImageUploadWidget(mockMultipartUploader, mockJSNIUtils, mockSynAlert, mockPortalGinInjector);
		inputId = "987";

		// The metadata returned should correspond to testFileName
		when(mockView.getInputId()).thenReturn(inputId);
		when(mockJSNIUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(new String[] {"testName.png"});
		when(mockJSNIUtils.getContentType(any(JavaScriptObject.class), anyInt())).thenReturn("image/png");
		when(mockPortalGinInjector.getImageUploadView()).thenReturn(mockView);
	}

	@Test
	public void testConfigure() {
		widget.configure(mockCallback);
		verify(mockView).showProgress(false);
		verify(mockView).setInputEnabled(true);
		verify(mockSynAlert).clear();
		verify(mockView).resetForm();
	}

	@Test
	public void testFileSelected() {
		final String successFileHandle = "123";

		// Configure before the test
		widget.configure(mockCallback);

		// method under test.
		widget.onFileProcessed(mockBlob, null);
		verify(mockView).updateProgress(1, "1%");
		verify(mockView).showProgress(true);
		verify(mockView).setInputEnabled(false);
		verify(mockSynAlert, atLeastOnce()).clear();

		verify(mockMultipartUploader).uploadFile(anyString(), eq("image/png"), any(JavaScriptObject.class), handleCaptor.capture(), any(Long.class), eq(mockView));
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
		verify(mockView, times(2)).setInputEnabled(true);
		verify(mockView, times(2)).showProgress(false);
	}

	@Test
	public void testFileSelectedForceContentType() {
		widget.configure(mockCallback);
		widget.onFileProcessed(mockBlob, "image/jpeg");
		verify(mockMultipartUploader).uploadFile(anyString(), eq("image/jpeg"), any(JavaScriptObject.class), handleCaptor.capture(), any(Long.class), eq(mockView));
	}

	@Test
	public void testFileSelectedSuccess() {
		// Configure before the test
		widget.configure(mockCallback);

		// method under test.
		widget.onFileSelected();
		verify(mockView).processFile();
	}

	@Test
	public void testFileSelectedFailed() {
		when(mockJSNIUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))).thenReturn(new String[] {"testName.raw"});
		when(mockJSNIUtils.getContentType(any(JavaScriptObject.class), anyInt())).thenReturn("notanimage/raw");

		// Configure before the test
		widget.configure(mockCallback);

		// method under test.
		widget.onFileSelected();

		verify(mockView, never()).updateProgress(1, "1%");
		verify(mockView, never()).showProgress(true);
		verify(mockView, never()).setInputEnabled(false);
		verify(mockView, never()).updateProgress(10, "10%");
		verify(mockView, never()).updateProgress(90, "90%");

		// Failure should trigger the following:
		verify(mockSynAlert).showError(anyString());
	}

}
