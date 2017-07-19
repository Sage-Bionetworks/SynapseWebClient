package org.sagebionetworks.web.unitclient.widget.entity.download;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl.PLEASE_SELECT_A_FILE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.aws.AwsSdk;
import org.sagebionetworks.web.client.widget.entity.download.S3DirectUploader;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;

public class S3DirectUploaderTest {
	S3DirectUploader uploader;
	@Mock
	AwsSdk mockAwsSdk;
	@Mock
	SynapseJSNIUtils mockSynapseJsniUtils;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Captor
	ArgumentCaptor<MD5Callback> md5Captor;
	@Mock
	JavaScriptObject mockBlob;
	@Mock
	JavaScriptObject mockS3;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	ProgressingFileUploadHandler mockProgressHandler;
	@Mock
	HasAttachHandlers mockView;
	
	public static final String ACCESS_KEY_ID = "1";
	public static final String SECRET_KEY_ID = "2";
	public static final String BUCKET_NAME = "abcd";
	public static final String ENDPOINT = "https://testendpoint.test";
	public static final String[] FILENAMES = new String[]{"selectedFile.txt"};
	public static final String CONTENT_TYPE = "plain/txt";
	
	public static final String FILE_INPUT_ID = "divId";
	public static final int FILE_INDEX = 0;
	public static final String KEY_PREFIX_UUID = "prefix-";
	public static final Long STORAGE_LOCATION_ID = 9L;
	
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockView.isAttached()).thenReturn(true);
		when(mockSynapseJsniUtils.getFileBlob(anyInt(), anyString())).thenReturn(mockBlob);
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(FILENAMES);
		when(mockSynapseJsniUtils.getContentType(anyString(), anyInt())).thenReturn(CONTENT_TYPE);
		uploader = new S3DirectUploader(mockAwsSdk, mockSynapseJsniUtils, mockGwt, mockSynapseClient);
		uploader.configure(ACCESS_KEY_ID, SECRET_KEY_ID, BUCKET_NAME, ENDPOINT);
	}

	@Test
	public void testUpload() {
		uploader.uploadFile(
				FILE_INPUT_ID, 
				FILE_INDEX, 
				mockProgressHandler,
				KEY_PREFIX_UUID,
				STORAGE_LOCATION_ID,
				mockView);
		verify(mockSynapseJsniUtils).getFileMd5(eq(mockBlob), md5Captor.capture());
		MD5Callback md5Callback = md5Captor.getValue();
		String md5 = "8782672c";
		md5Callback.setMD5(md5);
		
		verify(mockAwsSdk).getS3(eq(ACCESS_KEY_ID), eq(SECRET_KEY_ID), eq(BUCKET_NAME), eq(ENDPOINT), callbackPCaptor.capture());
		callbackPCaptor.getValue().invoke(mockS3);
		verify(mockAwsSdk).upload(KEY_PREFIX_UUID + "/" + FILENAMES[0], mockBlob, CONTENT_TYPE, mockS3, uploader);
	}
	
	@Test
	public void testUploadFailure() {
		uploader.uploadFile(
				FILE_INPUT_ID, 
				FILE_INDEX, 
				mockProgressHandler,
				KEY_PREFIX_UUID,
				STORAGE_LOCATION_ID,
				mockView);
		String errorMessage = "errors";
		//attempting to update progress when view is not attached is a no-op
		when(mockView.isAttached()).thenReturn(false);
		uploader.uploadFailed(errorMessage);
		verifyZeroInteractions(mockProgressHandler);
		
		//if view is attached, error is forwarded to the progress handler
		when(mockView.isAttached()).thenReturn(true);
		uploader.uploadFailed(errorMessage);
		verify(mockProgressHandler).uploadFailed(errorMessage);
	}
	

	@Test
	public void testUploadNoFilesSelected() {
		when(mockSynapseJsniUtils.getMultipleUploadFileNames(anyString())).thenReturn(new String[]{});
		uploader.uploadFile(
				FILE_INPUT_ID, 
				FILE_INDEX, 
				mockProgressHandler,
				KEY_PREFIX_UUID,
				STORAGE_LOCATION_ID,
				mockView);
		verify(mockProgressHandler).uploadFailed(PLEASE_SELECT_A_FILE);
	}

}
