package org.sagebionetworks.web.client.widget.aws;

import static org.sagebionetworks.web.client.ClientProperties.AWS_SDK_JS;

import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.upload.S3DirectUploadHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Wrapper around the AWS sdk js library.
 * @author jayhodgson
 *
 */
public class AwsSdk {
	ResourceLoader resourceLoader;
	@Inject
	public AwsSdk(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void init(final AsyncCallback<Void> callback) {

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
	public void upload(final String key,
			JavaScriptObject file,
			String contentType,
			JavaScriptObject s3,
			S3DirectUploadHandler callback
			) {
		_upload(key, file, contentType, s3, callback);
	}

	private static native void _upload(
			String key,
			JavaScriptObject file,
			String contentType,
			JavaScriptObject s3,
			S3DirectUploadHandler callback) /*-{
		var params = {
	        Key: key,
	        ContentType: contentType,
	        Body: file
	    };

		s3.upload(params).on('httpUploadProgress', function(evt) {
				// report progress
				callback.@org.sagebionetworks.web.client.widget.upload.S3DirectUploadHandler::updateProgress(D)(evt.loaded/evt.total);
			}).send(function(err, data) {
				if (err) {
					//error
					callback.@org.sagebionetworks.web.client.widget.upload.S3DirectUploadHandler::uploadFailed(Ljava/lang/String;)(err);
				} else {
					//success
					callback.@org.sagebionetworks.web.client.widget.upload.S3DirectUploadHandler::uploadSuccess()();
				}
			});
	}-*/;

	public String getPresignedURL(String key,
			String bucketName,
			String fileName,
			JavaScriptObject s3
			) {
		return _getPresignedURL(key, bucketName, fileName, s3);
	}

	private static native String _getPresignedURL(
			String key,
			String bucketName,
			String fileName,
			JavaScriptObject s3) 
	/*-{
		var params = {
			Bucket: bucketName,
	        Key: key,
	        Expires: 20,
	        ResponseContentDisposition: 'attachment; filename="' + fileName + '"' 
	    };
		return s3.getSignedUrl('getObject', params);
	}-*/;
	
	public String deleteObject(
			String key,
			String bucketName,
			JavaScriptObject s3
			) {
		return _deleteObject(key, bucketName, s3);
	}

	private static native String _deleteObject(
			String key,
			String bucketName,
			JavaScriptObject s3) 
	/*-{
		var params = {
			Bucket: bucketName,
			Key: key
	    };
		return s3.deleteObject(params);
	}-*/;


	public void getS3(
			final String accessKeyId, 
			final String secretAccessKey,
			final String bucketName,
			final String endpoint,
			final CallbackP<JavaScriptObject> s3Callback) {
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

	private static native JavaScriptObject _getS3(
			String accessKeyId, 
			String secretAccessKey,
			String bucketName,
			String endpoint) /*-{
		var creds = new $wnd.AWS.Credentials(accessKeyId, secretAccessKey);
        var ep = new $wnd.AWS.Endpoint(endpoint);
        var s3 = new $wnd.AWS.S3({
            endpoint: ep,
            params: {
                Bucket: bucketName
            },
            credentials: creds
        });
        return s3;
	}-*/;

}
