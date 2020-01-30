package org.sagebionetworks.web.client.widget.entity.download;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.aws.AwsSdk;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.S3DirectUploadHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Class responsible for direct upload to s3
 * 
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
	String fileName;
	String contentType;
	String md5;
	JavaScriptObject blob;
	ProgressingFileUploadHandler handler;
	Long storageLocationId;
	HasAttachHandlers view;
	NumberFormat percentFormat;
	SynapseClientAsync synapseClient;
	String keyPrefixUUID;

	@Inject
	public S3DirectUploader(AwsSdk awsSdk, SynapseJSNIUtils synapseJsniUtils, GWTWrapper gwt, SynapseClientAsync synapseClient) {
		this.awsSdk = awsSdk;
		this.synapseJsniUtils = synapseJsniUtils;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
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
			// success!
			// create a new file handle and tell the handler about the new handle
			ExternalObjectStoreFileHandle fileHandle = new ExternalObjectStoreFileHandle();
			fileHandle.setContentMd5(md5);
			Double fileSize = synapseJsniUtils.getFileSize(blob);
			fileHandle.setContentSize(fileSize.longValue());
			fileHandle.setContentType(contentType);
			fileHandle.setFileKey(keyPrefixUUID + "/" + fileName);
			fileHandle.setFileName(fileName);
			fileHandle.setStorageLocationId(storageLocationId);
			synapseClient.createExternalObjectStoreFileHandle(fileHandle, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String fileHandleId) {
					handler.uploadSuccess(fileHandleId);
				}

				@Override
				public void onFailure(Throwable caught) {
					handler.uploadFailed(caught.getMessage());
				}
			});
		}
	}

	public void configure(String accessKeyId, String secretAccessKey, String bucketName, String endpoint) {
		this.accessKeyId = accessKeyId;
		this.secretAccessKey = secretAccessKey;
		this.bucketName = bucketName;
		this.endpoint = endpoint;
	}

	public void uploadFile(String fileName, String contentType, JavaScriptObject blob, final ProgressingFileUploadHandler handler, final String keyPrefixUUID, final Long storageLocationId, final HasAttachHandlers view) {
		this.handler = handler;
		this.storageLocationId = storageLocationId;
		this.keyPrefixUUID = keyPrefixUUID;
		this.fileName = fileName;
		this.contentType = contentType;
		this.view = view;
		this.blob = blob;
		synapseJsniUtils.getFileMd5(blob, new MD5Callback() {
			@Override
			public void setMD5(String hexValue) {
				md5 = hexValue;
				if (md5 == null) {
					handler.uploadFailed(DisplayConstants.MD5_CALCULATION_ERROR);
					return;
				}
				awsSdk.getS3(accessKeyId, secretAccessKey, bucketName, endpoint, new CallbackP<JavaScriptObject>() {
					@Override
					public void invoke(JavaScriptObject s3) {
						// attempt the upload
						upload(s3);
					}
				});
			}
		});
	}

	private void upload(JavaScriptObject s3) {
		awsSdk.upload(keyPrefixUUID + "/" + fileName, blob, contentType, s3, this);
	}
}
