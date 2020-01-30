package org.sagebionetworks.web.client.widget.aws;

import static org.sagebionetworks.web.client.ClientProperties.AWS_SDK_JS;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.upload.S3DirectUploadHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Wrapper around the AWS sdk js library.
 * 
 * @author jayhodgson
 *
 */
public class AwsSdk {
	ResourceLoader resourceLoader;
	SynapseJSNIUtils jsniUtils;

	@Inject
	public AwsSdk(ResourceLoader resourceLoader, SynapseJSNIUtils jsniUtils) {
		this.resourceLoader = resourceLoader;
		this.jsniUtils = jsniUtils;
	}

	public void init(final AsyncCallback<Void> callback) {
		ClientProperties.fixResourceToCdnEndpoint(AWS_SDK_JS, jsniUtils.getCdnEndpoint());
		if (!resourceLoader.isLoaded(AWS_SDK_JS)) {
			resourceLoader.requires(AWS_SDK_JS, callback);
		} else {
			callback.onSuccess(null);
		}
	}

	/**
	 * 
	 * @param key target object key (in bucket)
	 * @param file object data (Buffer, Typed Array, Blob, String, ReadableStream)
	 * @param contentType
	 * @param accessKeyId
	 * @param secretAccessKey
	 * @param bucketName
	 * @param endpoint
	 */
	public void upload(final String key, JavaScriptObject file, String contentType, JavaScriptObject s3, S3DirectUploadHandler callback) {
		_upload(key, file, contentType, s3, callback);
	}

	private static native void _upload(String key, JavaScriptObject file, String contentType, JavaScriptObject s3, S3DirectUploadHandler callback) /*-{
		var parameters = {
			Key : key,
			ContentType : contentType,
			Body : file
		};
		var upload = new $wnd.AWS.S3.ManagedUpload({
			//			partSize: 10 * 1024 * 1024,  //default 5MB
			//			queueSize: 1, // default 4
			leavePartsOnError : true,
			params : parameters,
			service : s3
		});
		upload
				.on(
						'httpUploadProgress',
						function(evt) {
							// report progress
							callback.@org.sagebionetworks.web.client.widget.upload.S3DirectUploadHandler::updateProgress(D)(evt.loaded/evt.total);
						});
		var listener = function(err, data) {
			if (err) {
				//error
				console.log("Upload error, retrying:", err.code, err.message);
				upload.abort();
				setTimeout(function() {
					upload.send(listener);
				}, 5000);
			} else {
				//success
				callback.@org.sagebionetworks.web.client.widget.upload.S3DirectUploadHandler::uploadSuccess()();
			}
		};
		upload.send(listener);
	}-*/;

	public String getPresignedURL(String key, String bucketName, String fileName, JavaScriptObject s3) {
		return _getPresignedURL(key, bucketName, fileName, s3);
	}

	private static native String _getPresignedURL(String key, String bucketName, String fileName, JavaScriptObject s3)
	/*-{
		var params = {
			Bucket : bucketName,
			Key : key,
			Expires : 20,
			ResponseContentDisposition : 'attachment; filename="' + fileName
					+ '"'
		};
		return s3.getSignedUrl('getObject', params);
	}-*/;


	public void getS3(final String accessKeyId, final String secretAccessKey, final String bucketName, final String endpoint, final CallbackP<JavaScriptObject> s3Callback) {
		init(new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				JavaScriptObject s3 = _getS3(accessKeyId, secretAccessKey, bucketName, endpoint);
				s3Callback.invoke(s3);
			}

			@Override
			public void onFailure(Throwable caught) {

			}
		});
	}

	private static native JavaScriptObject _getS3(String accessKeyId, String secretAccessKey, String bucketName, String endpoint) /*-{
		var creds = new $wnd.AWS.Credentials(accessKeyId, secretAccessKey);
		var ep = new $wnd.AWS.Endpoint(endpoint);
		var s3 = new $wnd.AWS.S3({
			endpoint : ep,
			params : {
				Bucket : bucketName
			},
			credentials : creds,
			s3ForcePathStyle : true
		});
		return s3;
	}-*/;

}
