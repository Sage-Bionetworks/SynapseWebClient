package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.CallbackP;

import static org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl.*;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.S3DirectUploadHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.inject.Inject;

/**
 * Class responsible for direct upload to s3
 * @author jayhodgson
 *
 */
public class S3DirectUploader implements S3DirectUploadHandler {
	AwsSdk awsSdk;
	SynapseJSNIUtils synapseJsniUtils;
	String accessKeyId;
	String secretAccessKey;
	String bucketName;
	String endpoint;
	JavaScriptObject s3;
	String fileName; 
	String contentType; 
	JavaScriptObject blob; 
	ProgressingFileUploadHandler handler; 
	Long storageLocationId;
	HasAttachHandlers view;
	GWTWrapper gwt;
	NumberFormat percentFormat;
	SynapseClientAsync synapseClient;
	
	@Inject
	public S3DirectUploader(AwsSdk awsSdk, 
			SynapseJSNIUtils synapseJsniUtils,
			GWTWrapper gwt,
			SynapseClientAsync synapseClient) {
		this.awsSdk = awsSdk;
		this.synapseJsniUtils = synapseJsniUtils;
		this.gwt = gwt;
		this.synapseClient = synapseClient;
		this.percentFormat = gwt.getNumberFormat("##");
	}
	
	@Override
	public void updateProgress(double currentProgress) {
		if (handler != null && view.isAttached()) {
			String progressText = percentFormat.format(currentProgress) + "%";
			handler.updateProgress(currentProgress, progressText, null);
		}
	}
	
	@Override
	public void uploadFailed(String error) {
		if (handler != null && view.isAttached()) {
			handler.uploadFailed(error);
		}
	}
	
	@Override
	public void uploadSuccess() {
		if (handler != null && view.isAttached()) {
			//success!
			//TODO: create a new file handle and tell the handler about the new handle
//			synapseClient.createS3FileHandle();
//			handler.uploadSuccess(fileHandleId);
		}
	}
	
	public void configure(
			String accessKeyId, 
			String secretAccessKey,
			String bucketName,
			String endpoint) {
		this.accessKeyId = accessKeyId;
		this.secretAccessKey = secretAccessKey;
		this.bucketName = bucketName;
		this.endpoint = endpoint;
	}
	
	public void uploadFile(String fileInputId, int fileIndex, ProgressingFileUploadHandler handler, Long storageLocationId, HasAttachHandlers view) {
		String[] names = synapseJsniUtils.getMultipleUploadFileNames(fileInputId);
		if(names == null || names.length < 1){
			handler.uploadFailed(PLEASE_SELECT_A_FILE);
			return;
		}
		String fileName = names[fileIndex];

		JavaScriptObject blob = synapseJsniUtils.getFileBlob(fileIndex, fileInputId);
		String contentType = fixDefaultContentType(synapseJsniUtils.getContentType(fileInputId, fileIndex), fileName);
		
		uploadFile(fileName, contentType, blob, handler, storageLocationId, view);
	}
	
	public void uploadFile(
			String fileName, 
			String contentType, 
			JavaScriptObject blob, 
			ProgressingFileUploadHandler handler, 
			Long storageLocationId, 
			HasAttachHandlers view) {
		this.fileName = fileName;
		this.contentType = contentType;
		this.blob = blob;
		this.handler = handler;
		this.storageLocationId = storageLocationId;
		this.view = view;
		// on final success, create new file handle and send the id back (via the handler)
		awsSdk.getS3(accessKeyId, secretAccessKey, bucketName, endpoint, new CallbackP<JavaScriptObject>() {
			@Override
			public void invoke(JavaScriptObject s3) {
				// attempt the upload
				upload();
			}
		});
	}
	
	private void upload() {
		String key = fileName + "-" + gwt.getUniqueElementId();
		awsSdk.upload(key, blob, contentType, s3, this);
	}
}
