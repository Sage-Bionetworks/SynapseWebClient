package org.sagebionetworks.web.client.widget.entity.download;

import static org.sagebionetworks.web.client.ClientProperties.AWS_SDK_JS;

import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.utils.CallbackP;

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
			JavaScriptObject s3
			) {
		_upload(key, file, contentType, s3);
	}
	
	private static native void _upload(
			String key,
			JavaScriptObject file,
			String contentType,
			JavaScriptObject s3) /*-{
		var params = {
	        Key: key,
	        ContentType: file.type,
	        Body: file,
	        ACL: 'bucket-owner-full-control'
	    };

		s3.upload(params).on('httpUploadProgress', function(evt) {
				console.log("Uploaded :: " + parseInt((evt.loaded * 100) / evt.total)+'%');
			}).send(function(err, data) {
				if (err) {
					results.innerHTML = 'ERROR: ' + err;
				} else {
					alert("File uploaded successfully.");
					listObjs();
				}
			});
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
