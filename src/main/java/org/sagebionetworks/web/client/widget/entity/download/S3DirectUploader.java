package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.CallbackP;

import static org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl.*;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.inject.Inject;

/**
 * Class responsible for direct upload to s3
 * @author jayhodgson
 *
 */
public class S3DirectUploader {
	AwsSdk awsSdk;
	SynapseJSNIUtils synapseJsniUtils;
	String accessKeyId;
	String secretAccessKey;
	String bucketName;
	String endpoint;
	JavaScriptObject s3;
	GWTWrapper gwt;
	
	@Inject
	public S3DirectUploader(AwsSdk awsSdk, 
			SynapseJSNIUtils synapseJsniUtils,
			GWTWrapper gwt) {
		this.awsSdk = awsSdk;
		this.synapseJsniUtils = synapseJsniUtils;
		this.gwt = gwt;
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
			final String fileName, 
			final String contentType, 
			final JavaScriptObject blob, 
			final ProgressingFileUploadHandler handler, 
			final Long storageLocationId, 
			final HasAttachHandlers view) {
		// on final success, create new file handle and send the id back (via the handler)
		awsSdk.getS3(accessKeyId, secretAccessKey, bucketName, endpoint, new CallbackP<JavaScriptObject>() {
			@Override
			public void invoke(JavaScriptObject s3) {
				// attempt the upload
				awsSdk.upload(gwt.getUniqueElementId() + "/" + fileName, blob, contentType, s3);
			}
		});
		handler.uploadSuccess(newFileHandleId);
		
	}
}
